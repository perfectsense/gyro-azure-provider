package gyro.azure.storage;

import gyro.azure.AzureResource;

import gyro.core.GyroException;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.CorsRule;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
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
 *         cors
 *             allowed-headers: ["*"]
 *             allowed-methods: ["GET"]
 *             allowed-origins: ["*"]
 *             exposed-headers: ["*"]
 *             max-age: 6
 *         end
 *         storage-connection: $(azure::storage-account queue-storage-account-example | storage-connection)
 *     end
 */
@ResourceName("cloud-table")
public class CloudTableResource extends AzureResource {

    private String cloudTableName;
    private List<Cors> cors;
    private String storageConnection;

    /**
     * The name of the table (Required)
     */
    public String getCloudTableName() {
        return cloudTableName;
    }

    public void setCloudTableName(String cloudTableName) {
        this.cloudTableName = cloudTableName;
    }

    /**
     * The cors rules associated with the table. (Optional)
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

    public String getStorageConnection() {
        return storageConnection;
    }

    public void setStorageConnection(String storageConnection) {
        this.storageConnection = storageConnection;
    }

    @Override
    public boolean refresh() {

        try {
            CloudTable cloudTable = cloudTable();
            if (cloudTable.exists()) {
                setCloudTableName(cloudTable.getName());

                for (CorsRule rule :  cloudTable.getServiceClient().downloadServiceProperties().getCors().getCorsRules()) {
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
            CloudStorageAccount account = CloudStorageAccount.parse(getStorageConnection());
            CloudTableClient tableClient = account.createCloudTableClient();
            ServiceProperties props = new ServiceProperties();
            getCors().forEach(rule -> props.getCors().getCorsRules().add(rule.toCors()));
            tableClient.uploadServiceProperties(props);
            return tableClient.getTableReference(getCloudTableName());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }
}
