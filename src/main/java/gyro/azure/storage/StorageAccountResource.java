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
 *         tags: {
 *             Name: "storageaccount"
 *         }
 *     end
 */
@Type("storage-account")
public class StorageAccountResource extends AzureResource implements Copyable<StorageAccount> {

    private Set<Cors> corsRule;
    private Map<String, String> keys;
    private ResourceGroupResource resourceGroup;
    private String id;
    private String name;
    private Map<String, String> tags;
    private Boolean upgradeAccountV2;

    /**
     * The cors rules associated with the Storage Account. (Optional)
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
     * The Storage Account access key.
     */
    @Output
    public Map<String, String> getKeys() {
        if (keys == null) {
            keys = new HashMap<>();
        }

        return keys;
    }

    public void setKeys(Map<String, String> keys) {
        this.keys = keys;
    }

    /**
     * The Resource Group under which the Storage Account would reside. (Required)
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
     * The name of the Storage Account. (Required)
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
     * The tags for the Storage Account. (Optional)
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
        setName(storageAccount.name());
        setUpgradeAccountV2(storageAccount.kind().equals(Kind.STORAGE_V2));

        getKeys().clear();
        storageAccount.getKeys().forEach(e -> getKeys().put(e.keyName(), e.value()));

        getTags().clear();
        storageAccount.tags().forEach((key, value) -> getTags().put(key, value));
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

        List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();
        for (StorageAccountKey key : storageAccountKeys) {
            getKeys().put(key.keyName(), key.value());
        }

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
                + "AccountKey=" + getKeys().get("key1");
    }
}
