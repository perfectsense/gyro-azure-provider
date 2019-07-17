package gyro.azure.storage;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileShareProperties;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import gyro.core.scope.State;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a cloud file share
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-file-share cloud-file-share-example
 *         cloud-file-share-name: "example-cloud-file-share"
 *         share-quota: 10
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-file-share")
public class CloudFileShareResource extends AzureResource {

    private String cloudFileShareName;
    private Integer shareQuota;
    private StorageAccountResource storageAccount;

    /**
     * The name of the cloud share. (Required)
     */
    public String getCloudFileShareName() {
        return cloudFileShareName;
    }

    public void setCloudFileShareName(String cloudFileShareName) {
        this.cloudFileShareName = cloudFileShareName;
    }

    /**
     * The limit on the size of files in GB. (Optional)
     */
    @Updatable
    public Integer getShareQuota() {
        return shareQuota;
    }

    public void setShareQuota(Integer shareQuota) {
        this.shareQuota = shareQuota;
    }

    /**
     * The storage account resource where the file share will be created. (Required)
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
            CloudFileShare share = cloudFileShare();
            if (share.exists()) {
                setCloudFileShareName(share.getName());
                setShareQuota(share.getProperties().getShareQuota());
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
            CloudFileShare share = cloudFileShare();
            share.create();
            FileShareProperties fileShareProperties = new FileShareProperties();
            fileShareProperties.setShareQuota(getShareQuota());
            share.setProperties(fileShareProperties);
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        CloudFileShare share = cloudFileShare();
        FileShareProperties fileShareProperties = new FileShareProperties();
        fileShareProperties.setShareQuota(getShareQuota());
        share.setProperties(fileShareProperties);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        try {
            CloudFileShare share = cloudFileShare();
            share.delete();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private CloudFileShare cloudFileShare() {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudFileClient fileClient = storageAccount.createCloudFileClient();
            return fileClient.getShareReference(getCloudFileShareName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
