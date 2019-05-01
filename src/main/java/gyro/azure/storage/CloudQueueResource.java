package gyro.azure.storage;

import gyro.azure.AzureResource;

import gyro.core.GyroException;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueue;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
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
 *         storage-account: $(azure::storage-account queue-storage-account-example)
 *     end
 */
@ResourceName("cloud-queue")
public class CloudQueueResource extends AzureResource {

    private String cloudQueueName;
    private StorageAccountResource storageAccount;

    /**
     * The name of the queue (Required)
     */
    public String getCloudQueueName() {
        return cloudQueueName;
    }

    public void setCloudQueueName(String cloudQueueName) {
        this.cloudQueueName = cloudQueueName;
    }

    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public boolean refresh() {
        try {
            CloudQueue queue = cloudQueue();
            if (queue.exists()) {
                setCloudQueueName(queue.getName());
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
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
            return queueClient.getQueueReference(getCloudQueueName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
