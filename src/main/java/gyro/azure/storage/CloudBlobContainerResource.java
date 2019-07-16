package gyro.azure.storage;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import gyro.core.scope.State;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a blob container
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-blob-container blob-container-example
 *         container-name: "blobcontainer"
 *         public-access: "CONTAINER"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-blob-container")
public class CloudBlobContainerResource extends AzureResource {

    private String containerName;
    private String publicAccess;
    private StorageAccountResource storageAccount;

    /**
     * The name of the container. (Required)
     */
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The public access of the container. (Required)
     */
    @Updatable
    public String getPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * The storage account resource where the blob container will be created. (Required)
     */
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public boolean refresh() {
        try {
            CloudBlobContainer container = cloudBlobContainer();
            if (container.exists()) {
                setPublicAccess(container.getProperties().getPublicAccess().toString());
                return true;
            }
            return false;
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        try {
            CloudBlobContainer container = cloudBlobContainer();
            container.create();
            BlobContainerPermissions permissions = new BlobContainerPermissions();
            permissions.setPublicAccess(BlobContainerPublicAccessType.valueOf(getPublicAccess()));
            container.uploadPermissions(permissions);
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        try {
            CloudBlobContainer container = cloudBlobContainer();
            BlobContainerPermissions permissions = new BlobContainerPermissions();
            permissions.setPublicAccess(BlobContainerPublicAccessType.valueOf(getPublicAccess()));
            container.uploadPermissions(permissions);
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        try {
            CloudBlobContainer container = cloudBlobContainer();
            container.delete();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public String toDisplayString() {
        return "container " + getContainerName();
    }

    private CloudBlobContainer cloudBlobContainer() {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudBlobClient blobClient = account.createCloudBlobClient();
            return blobClient.getContainerReference(getContainerName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
