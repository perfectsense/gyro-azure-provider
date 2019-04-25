package gyro.azure.storage;

import gyro.azure.AzureResource;

import gyro.core.GyroException;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.CorsRule;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueue;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a cloud queue
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-queue cloud-queue-example
 *         cloud-queue-name: "cloudqueuename"
 *         cors
 *             allowed-headers: ["*"]
 *             allowed-methods: ["GET"]
 *             allowed-origins: ["*"]
 *             exposed-headers: ["*"]
 *             max-age: 6
 *         end
 *         storage-connection: $(azure::storage-account queue-storage-account-example | storage-connection)
 *     end
 */
@ResourceName("cloud-queue")
public class CloudQueueResource extends AzureResource {

    private String cloudQueueName;
    private List<Cors> cors;
    private String storageConnection;

    /**
     * The name of the queue (Required)
     */
    public String getCloudQueueName() {
        return cloudQueueName;
    }

    public void setCloudQueueName(String cloudQueueName) {
        this.cloudQueueName = cloudQueueName;
    }

    /**
     * The cors rules associated with the queue. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<Cors> getCors() {
        if (cors == null) {
            cors = new ArrayList<>();
        }

        return cors;
    }

    public void setCors(List<Cors> cors) {
        this.cors = cors;
    }

    public String getStorageConnection() {
        return storageConnection;
    }

    public void setStorageConnection(String storageConnection) {
        this.storageConnection = storageConnection;
    }

    @Override
    public boolean refresh() {
        try {
            CloudQueue queue = cloudQueue();
            if (queue.exists()) {
                setCloudQueueName(queue.getName());

                for (CorsRule rule :  queue.getServiceClient().downloadServiceProperties().getCors().getCorsRules()) {
                    getCors().add(new Cors(rule));
                }

                return true;
            }
            return false;
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create() {
        try {
            CloudQueue queue = cloudQueue();
            queue.create();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {}

    @Override
    public void delete() {
        try {
            CloudQueue queue = cloudQueue();
            queue.delete();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public String toDisplayString() {
        return "queue " + getCloudQueueName();
    }

    private CloudQueue cloudQueue() {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageConnection());
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
            ServiceProperties props = new ServiceProperties();
            getCors().forEach(rule -> props.getCors().getCorsRules().add(rule.toCors()));
            queueClient.uploadServiceProperties(props);
            return queueClient.getQueueReference(getCloudQueueName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
