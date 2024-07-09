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

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.StorageAccount;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
    private Boolean blobPublicAccess;

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

    @Updatable
    @Required
    public Boolean getBlobPublicAccess() {
        return blobPublicAccess;
    }

    public void setBlobPublicAccess(Boolean blobPublicAccess) {
        this.blobPublicAccess = blobPublicAccess;
    }

    @Override
    public void copyFrom(StorageAccount storageAccount) {
        setId(storageAccount.id());
        setName(storageAccount.name());

        setResourceGroup(findById(ResourceGroupResource.class, storageAccount.resourceGroupName()));
        setId(storageAccount.id());
        setUpgradeAccountV2(storageAccount.kind().equals(Kind.STORAGE_V2));
        setBlobPublicAccess(storageAccount.isBlobPublicAccessAllowed());

        getTags().clear();
        storageAccount.tags().forEach((key, value) -> getTags().put(key, value));
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        StorageAccount storageAccount = client.storageAccounts()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        if (storageAccount == null) {
            return false;
        }

        copyFrom(storageAccount);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws URISyntaxException, InvalidKeyException {
        AzureResourceManager client = createClient();

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

        StorageAccount.Update update = storageAccount.update();

        if (Boolean.TRUE.equals(getBlobPublicAccess())) {
            update = update.enableBlobPublicAccess();
        } else if (Boolean.FALSE.equals(getBlobPublicAccess())) {
            update = update.disableBlobPublicAccess();
        }

        update.apply();

        // TODO lifecycle and cors
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames)
        throws URISyntaxException, InvalidKeyException {
        AzureResourceManager client = createClient();

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

        if (changedFieldNames.contains("blob-public-access")) {
            if (Boolean.TRUE.equals(getBlobPublicAccess())) {
                update = update.enableBlobPublicAccess();
            } else {
                update = update.disableBlobPublicAccess();
            }
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        client.storageAccounts().deleteById(getId());
    }

    public String getConnection() {
        return String.format("DefaultEndpointsProtocol=https;"
            + "AccountName=%s;"
            + "AccountKey=%s;"
            + "EndpointSuffix=core.windows.net", getName(), keys().get("key1"));
    }

    public Map<String, String> keys() {
        Map<String, String> keys = new HashMap<>();

        if (getId() != null) {
            AzureResourceManager client = createClient();

            StorageAccount storageAccount = client.storageAccounts().getById(getId());
            storageAccount.getKeys().forEach(e -> keys.put(e.keyName(), e.value()));
        }

        return keys;
    }

    protected StorageAccount getStorageAccount() {
        AzureResourceManager client = createClient();

        return client.storageAccounts().getById(getId());
    }
}
