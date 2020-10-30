/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.storage;

import com.microsoft.azure.management.storage.Kind;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.FileServiceProperties;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTableClient;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a storage account
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::storage-account file-storage-account-example
 *         resource-group: $(azure::resource-group file-resource-group)
 *         name: "storageaccount"
 *
 *         cors-rule
 *             allowed-headers: ["*"]
 *             allowed-methods: ["PUT"]
 *             allowed-origins: ["*"]
 *             exposed-headers: ["*"]
 *             max-age: 6
 *             type: "table"
 *         end
 *
 *         lifecycle
 *         rule
 *             name: "rule1"
 *             enabled: false
 *             definition
 *                 action
 *                     base-blob
 *                         delete-days: 1
 *                         tier-to-archive-days: 1
 *                         tier-to-cool-days: 1
 *                     end
 *
 *                     snapshot
 *                         delete-days: 1
 *                     end
 *                 end
 *
 *                 filter
 *                     prefix-matches: [
 *                         container/box1
 *                     ]
 *                 end
 *             end
 *         end
 *
 *         tags: {
 *             Name: "storageaccount"
 *         }
 *     end
 */
@Type("storage-account")
public class StorageAccountResource extends AzureResource implements Copyable<StorageAccount> {

    private Set<Cors> corsRule;
    private ResourceGroupResource resourceGroup;
    private String id;
    private String name;
    private Map<String, String> tags;
    private StorageLifeCycle lifecycle;
    private Boolean upgradeAccountV2;

    /**
     * The cors rules associated with the Storage Account.
     *
     * @subresource gyro.azure.storage.Cors
     */
    @Updatable
    public Set<Cors> getCorsRule() {
        if (corsRule == null) {
            corsRule = new HashSet<>();
        }

        return corsRule;
    }

    public void setCorsRule(Set<Cors> corsRule) {
        this.corsRule = corsRule;
    }

    /**
     * The Resource Group under which the Storage Account would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The ID of the Storage Account.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Storage Account.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The tags for the Storage Account.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The lifecycle associated with the Storage Account. Only supported when 'upgrade-account-v2' set to ```true`.
     *
     * @subresource gyro.azure.storage.StorageLifeCycle
     */
    @Updatable
    public StorageLifeCycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(StorageLifeCycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * Upgrade account to General Purpose Account Kind V2. Cannot be downgraded.
     */
    @Updatable
    public Boolean getUpgradeAccountV2() {
        if (upgradeAccountV2 == null) {
            upgradeAccountV2 = false;
        }

        return upgradeAccountV2;
    }

    public void setUpgradeAccountV2(Boolean upgradeAccountV2) {
        this.upgradeAccountV2 = upgradeAccountV2;
    }

    @Override
    public void copyFrom(StorageAccount storageAccount) {
        try {
            setId(storageAccount.id());
            setName(storageAccount.name());

            CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(getConnection());

            getCorsRule().clear();
            CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
            blobClient.downloadServiceProperties().getCors()
                .getCorsRules().forEach(cors -> {
                    Cors rule = newSubresource(Cors.class);
                    rule.copyFrom(cors);
                    rule.setType("blob");
                    getCorsRule().add(rule);
            });

            CloudFileClient fileClient = cloudStorageAccount.createCloudFileClient();
            fileClient.downloadServiceProperties().getCors()
                .getCorsRules().forEach(cors -> {
                Cors rule = newSubresource(Cors.class);
                rule.copyFrom(cors);
                rule.setType("file");
                getCorsRule().add(rule);
            });

            CloudQueueClient queueClient = cloudStorageAccount.createCloudQueueClient();
            queueClient.downloadServiceProperties().getCors()
                .getCorsRules().forEach(cors -> {
                Cors rule = newSubresource(Cors.class);
                rule.copyFrom(cors);
                rule.setType("queue");
                getCorsRule().add(rule);
            });

            CloudTableClient tableClient = cloudStorageAccount.createCloudTableClient();
            tableClient.downloadServiceProperties().getCors()
                .getCorsRules().forEach(cors -> {
                Cors rule = newSubresource(Cors.class);
                rule.copyFrom(cors);
                rule.setType("table");
                getCorsRule().add(rule);
            });

        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }

        setResourceGroup(findById(ResourceGroupResource.class, storageAccount.resourceGroupName()));
        setId(storageAccount.id());
        setUpgradeAccountV2(storageAccount.kind().equals(Kind.STORAGE_V2));

        getTags().clear();
        storageAccount.tags().forEach((key, value) -> getTags().put(key, value));

        StorageLifeCycle lifeCycleManager = null;
        if (storageAccount.manager().managementPolicies().inner().get(getResourceGroup().getName(), getName()) != null) {
            lifeCycleManager = newSubresource(StorageLifeCycle.class);
            lifeCycleManager.copyFrom(
                storageAccount.manager()
                    .managementPolicies()
                    .getAsync(getResourceGroup().getName(), getName())
                    .toBlocking()
                    .single()
            );
        }
        setLifecycle(lifeCycleManager);
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        StorageAccount storageAccount = client.storageAccounts().getById(getId());

        if (storageAccount == null) {
            return false;
        }

        copyFrom(storageAccount);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        Azure client = createClient();

        StorageAccount.DefinitionStages.WithCreate withCreate = client.storageAccounts()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withTags(getTags());

        if (getUpgradeAccountV2()) {
            withCreate = withCreate.withGeneralPurposeAccountKindV2();
        }

        StorageAccount storageAccount = withCreate.create();

        setId(storageAccount.id());

        updateCorsRules();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws StorageException, URISyntaxException, InvalidKeyException {
        Azure client = createClient();

        StorageAccount storageAccount = client.storageAccounts().getById(getId());
        StorageAccount.Update update = storageAccount.update();

        update = update.withTags(getTags());

        if (changedFieldNames.contains("upgrade-account-v2")) {
            if (!getUpgradeAccountV2()) {
                throw new GyroException("Cannot downgrade from storage account V2");
            } else {
                update.upgradeToGeneralPurposeAccountKindV2();
            }
        }

        update.apply();

        updateCorsRules();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.storageAccounts().deleteById(getId());
    }

    private void updateCorsRules() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(getConnection());

        CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
        ServiceProperties blobProperties = new ServiceProperties();

        CloudFileClient fileClient = cloudStorageAccount.createCloudFileClient();
        FileServiceProperties fileProperties = new FileServiceProperties();

        CloudQueueClient queueClient = cloudStorageAccount.createCloudQueueClient();
        ServiceProperties queueProperties = new ServiceProperties();

        CloudTableClient tableClient = cloudStorageAccount.createCloudTableClient();
        ServiceProperties tableProperties = new ServiceProperties();

        for (Cors rule : getCorsRule()) {
            if (rule.getType().equalsIgnoreCase("blob")) {
                blobProperties.getCors().getCorsRules().add(rule.toCors());
            } else if (rule.getType().equalsIgnoreCase("file")) {
                fileProperties.getCors().getCorsRules().add(rule.toCors());
            } else if (rule.getType().equalsIgnoreCase("queue")) {
                queueProperties.getCors().getCorsRules().add(rule.toCors());
            } else if (rule.getType().equalsIgnoreCase("table")) {
                tableProperties.getCors().getCorsRules().add(rule.toCors());
            }
        }

        blobClient.uploadServiceProperties(blobProperties);
        fileClient.uploadServiceProperties(fileProperties);
        queueClient.uploadServiceProperties(queueProperties);
        tableClient.uploadServiceProperties(tableProperties);
    }

    public String getConnection() {
        return "DefaultEndpointsProtocol=https;"
                + "AccountName=" + getName() + ";"
                + "AccountKey=" + keys().get("key1");
    }

    public Map<String, String> keys() {
        Map<String, String> keys = new HashMap<>();

        if (getId() != null) {
            Azure client = createClient();

            StorageAccount storageAccount = client.storageAccounts().getById(getId());
            storageAccount.getKeys().forEach(e -> keys.put(e.keyName(), e.value()));
        }

        return keys;
    }
}
