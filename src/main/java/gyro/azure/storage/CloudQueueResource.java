package gyro.azure.storage;

import gyro.azure.AzureResource;

import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueue;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
 *         name: "cloudqueuename"
 *         storage-account: $(azure::storage-account queue-storage-account-example)
 *     end
 */
@Type("cloud-queue")
public class CloudQueueResource extends AzureResource implements Copyable<CloudQueue> {

    private String name;
    private StorageAccountResource storageAccount;

    /**
     * The name of the Queue (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Storage Account where the queue will be created. (Required)
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(CloudQueue queue) {
        setName(queue.getName());
        setStorageAccount(findById(StorageAccountResource.class, queue.getStorageUri().getPrimaryUri().getAuthority().split(".queue.core")[0]));
    }

    @Override
    public boolean refresh() {
        try {
            CloudQueue queue = cloudQueue();
            if (!queue.exists()) {
                return false;
            }

            copyFrom(queue);

            return true;
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        try {
            CloudQueue queue = cloudQueue();
            queue.create();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        try {
            CloudQueue queue = cloudQueue();
            queue.delete();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private CloudQueue cloudQueue() {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
            return queueClient.getQueueReference(getName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
