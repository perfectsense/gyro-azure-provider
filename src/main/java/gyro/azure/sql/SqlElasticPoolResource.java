package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlElasticPoolBasicEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolBasicMaxEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolBasicMinEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolPremiumEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolPremiumMaxEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolPremiumMinEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolPremiumSorage;
import com.microsoft.azure.management.sql.SqlElasticPoolStandardEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolStandardMaxEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolStandardMinEDTUs;
import com.microsoft.azure.management.sql.SqlElasticPoolStandardStorage;
import com.microsoft.azure.management.sql.SqlElasticPoolOperations.DefinitionStages.WithEdition;
import com.microsoft.azure.management.sql.SqlElasticPoolOperations.DefinitionStages.WithBasicEdition;
import com.microsoft.azure.management.sql.SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition;
import com.microsoft.azure.management.sql.SqlElasticPoolOperations.DefinitionStages.WithStandardEdition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a sql elastic pool.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::sql-elastic-pool sql-elastic-pool-example
 *         name: "sql-elastic-pool"
 *         edition: "Basic"
 *         dtu-min: "eDTU_0"
 *         dtu-max: "eDTU_5"
 *         dtu-reserved: "eDTU_50"
 *         sql-server: $(azure::sql-server sql-server-example)
 *         tags: {
 *             Name: "sql-elastic-pool-example"
 *         }
 *     end
 */
@ResourceType("sql-elastic-pool")
public class SqlElasticPoolResource extends AzureResource {

    private List<String> databaseNames;
    private String dtuMax;
    private String dtuMin;
    private String dtuReserved;
    private String edition;
    private String id;
    private String name;
    private SqlServerResource sqlServer;
    private String storageCapacity;
    private Map<String, String> tags;

    /**
     * The databases within the elastic pool. (Optional)
     */
    @ResourceUpdatable
    public List<String> getDatabaseNames() {
        if (databaseNames == null) {
            databaseNames = new ArrayList<>();
        }

        return databaseNames;
    }

    public void setDatabaseNames(List<String> databaseNames) {
        this.databaseNames = databaseNames;
    }

    /**
     * Sets the maximum eDTU for the each database in the pool. (Required)
     */
    @ResourceUpdatable
    public String getDtuMax() {
        return dtuMax;
    }

    public void setDtuMax(String dtuMax) {
        this.dtuMax = dtuMax;
    }

    /**
     * Sets the minimum of eDTU for each database in the pool. (Required)
     */
    @ResourceUpdatable
    public String getDtuMin() {
        return dtuMin;
    }

    public void setDtuMin(String dtuMin) {
        this.dtuMin = dtuMin;
    }

    /**
     * Sets the total shared eDTU for the elastic pool. (Required)
     */
    @ResourceUpdatable
    public String getDtuReserved() {
        return dtuReserved;
    }

    public void setDtuReserved(String dtuReserved) {
        this.dtuReserved = dtuReserved;
    }

    /**
     * The edition of the elastic pool. (Required)
     */
    @ResourceUpdatable
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    /**
     * The id of the elastic pool.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the elastic pool. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the storage limit for the database elastic pool. Used with Standard and Premium editions (Conditional)
     */
    @ResourceUpdatable
    public String getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(String storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    /**
     * The sql server where the elastic pool is found. (Required)
     */
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags associated with the elastic pool. (Optional)
     */
    @ResourceUpdatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlElasticPool elasticPool = getSqlElasticPool(client);

        if (elasticPool == null) {
            return false;
        }

        getDatabaseNames().clear();
        elasticPool.listDatabases().forEach(db -> getDatabaseNames().add(db.name()));
        setDtuMax("eDTU_" + Integer.toString(elasticPool.databaseDtuMax()));
        setDtuMin("eDTU_" + Integer.toString(elasticPool.databaseDtuMin()));
        setDtuReserved("eDTU_" + Integer.toString(elasticPool.dtu()));
        setEdition(elasticPool.edition().toString());
        setId(elasticPool.id());
        setName(elasticPool.name());
        setStorageCapacity(Integer.toString(elasticPool.storageCapacityInMB()));
        setTags(elasticPool.inner().getTags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        WithEdition buildPool = client.sqlServers().getById(getSqlServerId()).elasticPools().define(getName());

        SqlElasticPool elasticPool = null;
        WithBasicEdition withBasicEdition;
        WithPremiumEdition withPremiumEdition;
        WithStandardEdition withStandardEdition;

        if (getEdition().equals("Basic")) {
            withBasicEdition = buildPool.withBasicPool()
                    .withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolBasicEDTUs.valueOf(getDtuReserved()));

            for (String database : getDatabaseNames()) {
                withBasicEdition.withExistingDatabase(database);
            }

            elasticPool = withBasicEdition.create();

        } else if (getEdition().equals("Premium")) {
            withPremiumEdition = buildPool.withPremiumPool()
                    .withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolPremiumEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolPremiumSorage.valueOf(getStorageCapacity()));

            for (String database : getDatabaseNames()) {
                withPremiumEdition.withExistingDatabase(database);
            }

            elasticPool = withPremiumEdition.create();

        } else if (getEdition().equals("Standard")) {
            withStandardEdition = buildPool.withStandardPool()
                    .withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolStandardEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolStandardStorage.valueOf(getStorageCapacity()));

            for (String database : getDatabaseNames()) {
                withStandardEdition.withExistingDatabase(database);
            }

            elasticPool = withStandardEdition.withTags(getTags()).create();
        }

        setId(elasticPool.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlElasticPool.Update update = getSqlElasticPool(client).update();

        if (getEdition().equals("Basic")) {
            update.withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolBasicEDTUs.valueOf(getDtuReserved()));
        } else if (getEdition().equals("Premium")) {
            update.withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolPremiumEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolPremiumSorage.valueOf(getStorageCapacity()));
        } else if (getEdition().equals("Standard")) {
            update.withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolStandardEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolStandardStorage.valueOf(getStorageCapacity()));
        }

        for (String database : getDatabaseNames()) {
            update.withNewDatabase(database);
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getSqlElasticPool(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "sql elastic pool " + getName();
    }

    SqlElasticPool getSqlElasticPool(Azure client) {
        return client.sqlServers().getById(getSqlServerId()).elasticPools().get(getName());
    }
}
