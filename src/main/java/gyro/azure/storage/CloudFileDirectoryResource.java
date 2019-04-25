package gyro.azure.storage;

import com.microsoft.azure.storage.CorsProperties;
import com.microsoft.azure.storage.ServiceProperties;
import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;

import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Creates a cloud file directory
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-file-directory cloud-file-directory
 *         cloud-file-directory-path: "/example/directory/path"
 *         cloud-file-share-name: $(azure::cloud-file-share cloud-file-share-example | cloud-file-share-name)
 *         storage-connection: $(azure::storage-account blob-storage-account-example | storage-connection)
 *     end
 */
@ResourceName("cloud-file-directory")
public class CloudFileDirectoryResource extends AzureResource {

    private String cloudFileDirectoryPath;
    private String cloudFileDirectoryName;
    private String cloudFileShareName;
    private List<Cors> corsRule;
    private String storageConnection;

    public CloudFileDirectoryResource() {}

    public CloudFileDirectoryResource(String directoryPath, String cloudFileShareName, String storageConnection) {
        this.cloudFileDirectoryPath = directoryPath;
        this.cloudFileShareName = cloudFileShareName;
        this.storageConnection = storageConnection;
    }

    /**
     * The name of the cloud file directory. (Required)
     */
    public String getCloudFileDirectoryPath() {
        return cloudFileDirectoryPath;
    }

    public void setCloudFileDirectoryPath(String cloudFileDirectoryPath) {
        this.cloudFileDirectoryPath = cloudFileDirectoryPath;
    }

    public String getCloudFileDirectoryName() {
        return Paths.get(getCloudFileDirectoryPath()).getFileName().toString();
    }

    public void setCloudFileDirectoryName(String cloudFileDirectoryName) {
        this.cloudFileDirectoryName = cloudFileDirectoryName;
    }

    /**
     * The name of the cloud file share. (Required)
     */
    public String getCloudFileShareName() {
        return cloudFileShareName;
    }

    public void setCloudFileShareName(String cloudFileShareName) {
        this.cloudFileShareName = cloudFileShareName;
    }

    /**
     * The cors rules associated with the cloud file directory. (Required)
     */
    public List<Cors> getCorsRule() {
        if (corsRule == null) {
            corsRule = new ArrayList<>();
        }

        return corsRule;
    }

    public void setCorsRule(List<Cors> corsRule) {
        this.corsRule = corsRule;
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
            CloudFileDirectory directory = cloudFileDirectory();
            if (directory.exists()) {
                setCloudFileDirectoryName(directory.getName());
                return true;
            }
            return false;
        }  catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create() {
        try {
            CloudFileDirectory directory = cloudFileDirectory();
            directory.create();
        } catch (StorageException | URISyntaxException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {}

    @Override
    public void delete() {
        try {
            CloudFileDirectory directory = cloudFileDirectory();
            directory.delete();
        } catch (StorageException | URISyntaxException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public String toDisplayString() {
        return "cloud file directory " + Paths.get(getCloudFileDirectoryPath()).getFileName().toString();
    }

    public CloudFileDirectory cloudFileDirectory() {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageConnection());
            CloudFileClient fileClient = storageAccount.createCloudFileClient();
            CloudFileShare share = fileClient.getShareReference(getCloudFileShareName());

            CloudFileDirectory rootDirectory = share.getRootDirectoryReference();

            Path cloudFilePath = Paths.get(getCloudFileDirectoryPath());
            String directoryName = cloudFilePath.getFileName().toString();
            Iterator<Path> iter = cloudFilePath.iterator();
            while (iter.hasNext()) {
                String currentDirectory = iter.next().toString();
                if (currentDirectory != directoryName) {
                    rootDirectory = rootDirectory.getDirectoryReference(currentDirectory);
                    rootDirectory.createIfNotExists();
                }
            }
            return rootDirectory.getDirectoryReference(directoryName);
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
