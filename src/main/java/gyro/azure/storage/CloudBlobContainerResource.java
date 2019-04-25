package gyro.azure.storage;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.CorsRule;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
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
 *         cors
 *             allowed-headers: ["*"]
 *             allowed-methods: ["GET"]
 *             allowed-origins: ["*"]
 *             exposed-headers: ["*"]
 *             max-age: 6
 *         end
 *         public-access: "CONTAINER"
 *         storage-connection: $(azure::storage-account blob-storage-account-example | storage-connection)
 *     end
 */
@ResourceName("cloud-blob-container")
public class CloudBlobContainerResource extends AzureResource {

    private String containerName;
    private List<Cors> cors;
    private String publicAccess;
    private String storageConnection;

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
     * The cors rules associated with the container. (Optional)
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

    /**
     * The public access of the container. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
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
            CloudBlobContainer container = cloudBlobContainer();
            if (container.exists()) {
                setPublicAccess(container.getProperties().getPublicAccess().toString());

                for (CorsRule rule : container.getServiceClient().downloadServiceProperties().getCors().getCorsRules()) {
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
    public void update(Resource current, Set<String> changedProperties) {
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
    public void delete() {
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
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageConnection());
            CloudBlobClient blobClient = account.createCloudBlobClient();
            ServiceProperties props = new ServiceProperties();
            getCors().forEach(rule -> props.getCors().getCorsRules().add(rule.toCors()));
            blobClient.uploadServiceProperties(props);
            return blobClient.getContainerReference(getContainerName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
