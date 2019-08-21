package gyro.azure.storage;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

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
 *         name: "blobcontainer"
 *         public-access: "CONTAINER"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-blob-container")
public class CloudBlobContainerResource extends AzureResource implements Copyable<CloudBlobContainer> {

    private String name;
    private String publicAccess;
    private StorageAccountResource storageAccount;

    /**
     * The name of the container. (Required)
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
     * The public access of the container. Valid values are ``BLOB`` or ``CONTAINER`` or ``OFF`` (Required)
     */
    @Required
    @ValidStrings({"BLOB", "CONTAINER", "OFF"})
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
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(CloudBlobContainer container) {
        setStorageAccount(findById(StorageAccountResource.class, container.getStorageUri().getPrimaryUri().getAuthority().split(".blob.core")[0]));
        setPublicAccess(container.getProperties().getPublicAccess().toString());
        setName(container.getName());
    }

    @Override
    public boolean refresh() {
        try {
            CloudBlobContainer container = cloudBlobContainer();
            if (!container.exists()) {
                return false;
            }

            copyFrom(container);

            return true;
        } catch (StorageException ex) {
            return false;
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

    private CloudBlobContainer cloudBlobContainer() {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudBlobClient blobClient = account.createCloudBlobClient();
            return blobClient.getContainerReference(getName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
