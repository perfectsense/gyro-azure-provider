package gyro.azure.storage;

import com.microsoft.azure.storage.CorsRule;
import gyro.azure.AzureResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;

import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileServiceProperties;
import com.microsoft.azure.storage.file.FileShareProperties;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
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
 *         cors
 *             allowed-headers: ["*"]
 *             allowed-methods: ["GET"]
 *             allowed-origins: ["*"]
 *             exposed-headers: ["*"]
 *             max-age: 6
 *         end
 *         share-quota: 10
 *         storage-connection: $(azure::storage-account blob-storage-account-example | storage-connection)
 *     end
 */
@ResourceName("cloud-file-share")
public class CloudFileShareResource extends AzureResource {

    private String cloudFileShareName;
    private List<Cors> cors;
    private Integer shareQuota;
    private String storageConnection;

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
     * The cors rules associated with the file share. (Optional)
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
     * The limit on the size of files in GB. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getShareQuota() {
        return shareQuota;
    }

    public void setShareQuota(Integer shareQuota) {
        this.shareQuota = shareQuota;
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
            CloudFileShare share = cloudFileShare();
            if (share.exists()) {
                setCloudFileShareName(share.getName());
                setShareQuota(share.getProperties().getShareQuota());

                for (CorsRule rule : share.getServiceClient().downloadServiceProperties().getCors().getCorsRules()) {
                    getCors().add(new Cors(rule));
                }

                return true;
            }
            return false;
        } catch (StorageException ex) {
            throw new BeamException(ex.getMessage());
        }
    }

    @Override
    public void create() {
        try {
            CloudFileShare share = cloudFileShare();
            share.create();
            FileShareProperties fileShareProperties = new FileShareProperties();
            fileShareProperties.setShareQuota(getShareQuota());
            share.setProperties(fileShareProperties);
        } catch (StorageException ex) {
            throw new BeamException(ex.getMessage());
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        CloudFileShare share = cloudFileShare();
        FileShareProperties fileShareProperties = new FileShareProperties();
        fileShareProperties.setShareQuota(getShareQuota());
        share.setProperties(fileShareProperties);
    }

    @Override
    public void delete() {
        try {
            CloudFileShare share = cloudFileShare();
            share.delete();
        } catch (StorageException ex) {
            throw new BeamException(ex.getMessage());
        }
    }

    @Override
    public String toDisplayString() {
        return "cloud file share " + getCloudFileShareName();
    }

    private CloudFileShare cloudFileShare() {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageConnection());
            CloudFileClient fileClient = storageAccount.createCloudFileClient();
            FileServiceProperties props = new FileServiceProperties();
            getCors().forEach(rule -> props.getCors().getCorsRules().add(rule.toCors()));
            fileClient.uploadServiceProperties(props);
            return fileClient.getShareReference(getCloudFileShareName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new BeamException(ex.getMessage());
        }
    }
}
