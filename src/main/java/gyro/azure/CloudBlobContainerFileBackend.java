package gyro.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.FileBackend;
import gyro.core.GyroException;
import gyro.core.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.stream.Stream;

@Type("cloud-blob-container")
public class CloudBlobContainerFileBackend extends FileBackend {
    private String storageAccount;
    private String cloudBlobContainer;
    private String prefix;

    public String getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(String storageAccount) {
        this.storageAccount = storageAccount;
    }

    public String getCloudBlobContainer() {
        return cloudBlobContainer;
    }

    public void setCloudBlobContainer(String cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Stream<String> list() throws Exception {
        return null;
    }

    @Override
    public InputStream openInput(String file) throws Exception {
        return container().getBlockBlobReference(prefixed(file)).openInputStream();
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {
        return new ByteArrayOutputStream() {
            public void close() {
                try {
                    container().getBlockBlobReference(prefixed(file)).uploadFromByteArray(toByteArray(), 0, toByteArray().length);
                } catch (StorageException | URISyntaxException  | IOException e) {
                    throw new GyroException(e.getMessage());
                }
            }
        };
    }

    @Override
    public void delete(String file) throws Exception {
        container().getBlockBlobReference(prefixed(file)).delete();
    }

    private CloudBlobContainer container(){
        StorageAccountResource storage = getRootScope().findResourceById(StorageAccountResource.class, getStorageAccount());
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storage.getConnection());
            CloudBlobClient blobClient = account.createCloudBlobClient();
            return blobClient.getContainerReference(getCloudBlobContainer());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private String prefixed(String file) {
        return getPrefix() != null ? getPrefix() + '/' + file : file;
    }
}
