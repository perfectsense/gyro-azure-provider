package gyro.azure.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.FileServiceProperties;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTableClient;
import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
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
 *         resource-group-name: $(azure::resource-group file-resource-group | resource-group-name)
 *         name: "storageaccount"
 *
 *         tags: {
 *             Name: "storageaccount"
 *         }
 *     end
 */
@Type("storage-account")
public class StorageAccountResource extends AzureResource {

    private List<Cors> corsRule;
    private Map<String, String> keys;
    private String resourceGroupName;
    private String id;
    private String name;
    private Map<String, String> tags;

    /**
     * The cors rules associated with the storage account. (Optional)
     */
    @Updatable
    public List<Cors> getCorsRule() {
        if (corsRule == null) {
            corsRule = new ArrayList<>();
        }

        return corsRule;
    }

    public void setCorsRule(List<Cors> corsRule) {
        this.corsRule = corsRule;
    }

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

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the storage account. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The tags for the storage account. (Optional)
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

    @Override
    public boolean refresh() {
        Azure client = createClient();

        StorageAccount storageAccount = client.storageAccounts().getById(getId());

        try {
            CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(getConnection());

            getCorsRule().clear();

            CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
            blobClient.downloadServiceProperties().getCors()
                    .getCorsRules().forEach(cors -> getCorsRule().add(new Cors(cors, "blob")));

            CloudFileClient fileClient = cloudStorageAccount.createCloudFileClient();
            fileClient.downloadServiceProperties().getCors()
                    .getCorsRules().forEach(cors -> getCorsRule().add(new Cors(cors, "file")));

            CloudQueueClient queueClient = cloudStorageAccount.createCloudQueueClient();
            queueClient.downloadServiceProperties().getCors()
                    .getCorsRules().forEach(cors -> getCorsRule().add(new Cors(cors, "queue")));

            CloudTableClient tableClient = cloudStorageAccount.createCloudTableClient();
            tableClient.downloadServiceProperties().getCors()
                    .getCorsRules().forEach(cors -> getCorsRule().add(new Cors(cors, "table")));

        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }

        setResourceGroupName(storageAccount.resourceGroupName());
        setId(storageAccount.id());
        setName(storageAccount.name());

        getKeys().clear();
        storageAccount.getKeys().stream().forEach(e -> getKeys().put(e.keyName(), e.value()));

        getTags().clear();
        storageAccount.tags().entrySet().stream().forEach(e -> getTags().put(e.getKey(), e.getValue()));

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        StorageAccount storageAccount = client.storageAccounts()
                .define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName())
                .withTags(getTags())
                .create();

        setId(storageAccount.id());

        List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();
        for (StorageAccountKey key : storageAccountKeys) {
            getKeys().put(key.keyName(), key.value());
        }

        updateCorsRules();
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        StorageAccount storageAccount = client.storageAccounts().getById(getId());
        storageAccount.update().withTags(getTags()).apply();

        updateCorsRules();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.storageAccounts().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "storage account " + getName();
    }

    private void updateCorsRules() {
        try {
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
                } else {
                    throw new GyroException("Invalid storage service. " +
                            "Valid storage service options include" +
                            "blob, file, queue, and table");
                }
            }

            blobClient.uploadServiceProperties(blobProperties);
            fileClient.uploadServiceProperties(fileProperties);
            queueClient.uploadServiceProperties(queueProperties);
            tableClient.uploadServiceProperties(tableProperties);

        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    public String getConnection() {
        return "DefaultEndpointsProtocol=https;"
                + "AccountName=" + getName() + ";"
                + "AccountKey=" + getKeys().get("key1");
    }
}
