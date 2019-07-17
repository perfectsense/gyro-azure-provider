package gyro.azure.storage;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import gyro.core.resource.Output;
import gyro.core.scope.State;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.Set;

import java.io.File;

/**
 * Creates a cloud blob
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-blob blob-example
 *         blob-directory-path: "/path/to/blob"
 *         container-name: $(azure::cloud-blob-container blob-container-example | container-name)
 *         file-path: "gyro-providers/gyro-azure-provider/examples/storage/test-blob-doc.txt"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-blob")
public class CloudBlobResource extends AzureResource {

    private String blobName;
    private String blobDirectoryPath;
    private String containerName;
    private String filePath;
    private StorageAccountResource storageAccount;
    private String uri;

    /**
     * The name of the blob. (Required)
     */
    public String getBlobName() {
        return Paths.get(getBlobDirectoryPath()).getFileName().toString();
    }

    public void setBlobName(String blockBlobName) {
        this.blobName = blobName;
    }

    /**
     * The directory path of the blob. (Required)
     */
    public String getBlobDirectoryPath() {
        return blobDirectoryPath;
    }

    public void setBlobDirectoryPath(String blobDirectoryPath) {
        this.blobDirectoryPath = blobDirectoryPath;
    }

    /**
     * The container where the blob found. (Required)
     */
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The path of the file to upload. (Required)
     */
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * The storage account resource where the blob will be created. (Required)
     */
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Output
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean refresh() {
        try {
            CloudBlockBlob blob = cloudBlobBlob();
            if (blob.exists()) {
                setBlobName(blob.getName());
                setUri(blob.getUri().toString());
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
            CloudBlockBlob blob = cloudBlobBlob();
            File file = new File(getFilePath());
            blob.upload(new FileInputStream(file), file.length());
            setUri(blob.getUri().toString());
        } catch (StorageException | IOException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        try {
            CloudBlockBlob blob = cloudBlobBlob();
            blob.delete();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private CloudBlockBlob cloudBlobBlob() {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudBlobClient client = account.createCloudBlobClient();
            CloudBlobContainer container = client.getContainerReference(getContainerName());
            CloudBlockBlob blob;
            if (getBlobDirectoryPath() != null) {
                CloudBlobDirectory directory = createDirectories();
                blob = directory.getBlockBlobReference(getBlobName());
                File file = new File(getFilePath());
                blob.upload(new FileInputStream(file), file.length());
            } else {
                blob = container.getBlockBlobReference(getBlobName());
            }
            return blob;
        } catch (StorageException | URISyntaxException | InvalidKeyException | IOException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private CloudBlobDirectory createDirectories() {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudBlobClient client = account.createCloudBlobClient();
            CloudBlobContainer container = client.getContainerReference(getContainerName());
            Path directoryPath = Paths.get(getBlobDirectoryPath()).getParent();
            Iterator<Path> iter = directoryPath.iterator();
            CloudBlobDirectory directory = container.getDirectoryReference(iter.next().toString());
            while (iter.hasNext()) {
                directory = directory.getDirectoryReference(iter.next().toString());
            }
            return directory;
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
