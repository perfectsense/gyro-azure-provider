package gyro.azure.storage;

import gyro.azure.AzureResource;

import gyro.core.GyroException;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a cloud table
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-table cloud-table-example
 *         cloud-table-name: "cloudtablename"
 *         storage-account: $(azure::storage-account queue-storage-account-example)
 *     end
 */
@ResourceName("cloud-table")
public class CloudTableResource extends AzureResource {

    private String cloudTableName;
    private StorageAccountResource storageAccount;

    /**
     * The name of the table (Required)
     */
    public String getCloudTableName() {
        return cloudTableName;
    }

    public void setCloudTableName(String cloudTableName) {
        this.cloudTableName = cloudTableName;
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
            CloudTable cloudTable = cloudTable();
            if (cloudTable.exists()) {
                setCloudTableName(cloudTable.getName());
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
            CloudTable cloudTable = cloudTable();
            cloudTable.create();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {}

    @Override
    public void delete() {
        try {
            CloudTable cloudTable = cloudTable();
            cloudTable.delete();
        } catch (StorageException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public String toDisplayString() {
        return "cloud table " + getCloudTableName();
    }

    private CloudTable cloudTable() {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
            CloudTableClient tableClient = account.createCloudTableClient();
            return tableClient.getTableReference(getCloudTableName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
