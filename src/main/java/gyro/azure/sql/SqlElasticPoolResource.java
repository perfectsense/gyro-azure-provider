package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
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

    private static final String EDITION_BASIC = "Basic";
    private static final String EDITION_PREMIUM = "Premium";
    private static final String EDITION_STANDARD = "Standard";

    private List<String> databaseNames;
    private String dtuMax;
    private String dtuMin;
    private String dtuReserved;
    private String edition;
    private String id;
    private String name;
    private SqlElasticPool sqlElasticPool;
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
     * The maximum eDTU for the each database in the pool. (Required)
     */
    @ResourceUpdatable
    public String getDtuMax() {
        return dtuMax;
    }

    public void setDtuMax(String dtuMax) {
        this.dtuMax = dtuMax;
    }

    /**
     * The minimum of eDTU for each database in the pool. (Required)
     */
    @ResourceUpdatable
    public String getDtuMin() {
        return dtuMin;
    }

    public void setDtuMin(String dtuMin) {
        this.dtuMin = dtuMin;
    }

    /**
     * The total shared eDTU for the elastic pool. (Required)
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
     * The storage limit for the database elastic pool. Used with Standard and Premium editions (Conditional)
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

        SqlElasticPool elasticPool = sqlElasticPool(client);

        if (elasticPool == null) {
            return false;
        }

        getDatabaseNames().clear();
        elasticPool.listDatabases().forEach(db -> getDatabaseNames().add(db.name()));
        setDtuMax("eDTU_" + elasticPool.databaseDtuMax());
        setDtuMin("eDTU_" + elasticPool.databaseDtuMin());
        setDtuReserved("eDTU_" + elasticPool.dtu());
        setEdition(elasticPool.edition().toString());
        setId(elasticPool.id());
        setName(elasticPool.name());
        setStorageCapacity(Integer.toString(elasticPool.storageCapacityInMB()));
        setTags(elasticPool.inner().getTags());

        return true;
    }

    @Override
    public void create() {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        Azure client = createClient();

        WithEdition buildPool = client.sqlServers().getById(getSqlServerId()).elasticPools().define(getName());

        SqlElasticPool elasticPool = null;
        WithBasicEdition withBasicEdition;
        WithPremiumEdition withPremiumEdition;
        WithStandardEdition withStandardEdition;

        if (EDITION_BASIC.equalsIgnoreCase(getEdition())) {
            withBasicEdition = buildPool.withBasicPool()
                    .withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolBasicEDTUs.valueOf(getDtuReserved()));

            for (String database : getDatabaseNames()) {
                withBasicEdition.withExistingDatabase(database);
            }

            elasticPool = withBasicEdition.withTags(getTags()).create();

        } else if (EDITION_PREMIUM.equalsIgnoreCase(getEdition())) {
            withPremiumEdition = buildPool.withPremiumPool()
                    .withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolPremiumEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolPremiumSorage.valueOf(getStorageCapacity()));

            for (String database : getDatabaseNames()) {
                withPremiumEdition.withExistingDatabase(database);
            }

            elasticPool = withPremiumEdition.withTags(getTags()).create();

        } else if (EDITION_STANDARD.equalsIgnoreCase(getEdition())) {
            withStandardEdition = buildPool.withStandardPool()
                    .withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolStandardEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolStandardStorage.valueOf(getStorageCapacity()));

            for (String database : getDatabaseNames()) {
                withStandardEdition.withExistingDatabase(database);
            }

            elasticPool = withStandardEdition.withTags(getTags()).create();
        } else {
            throw new GyroException("Invalid edition. Valid values are Basic, Standard, and Premium");
        }

        setId(elasticPool.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlElasticPool.Update update = getSqlElasticPool(client).update();

        if (EDITION_BASIC.equalsIgnoreCase(getEdition())) {
            update.withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolBasicEDTUs.valueOf(getDtuReserved()));
        } else if (EDITION_PREMIUM.equalsIgnoreCase(getEdition())) {
            update.withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolPremiumEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolPremiumSorage.valueOf(getStorageCapacity()));
        } else if (EDITION_STANDARD.equalsIgnoreCase(getEdition())) {
            update.withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolStandardEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolStandardStorage.valueOf(getStorageCapacity()));
        } else {
            throw new GyroException("Invalid edition. Valid values are Basic, Standard, and Premium");
        }

        for (String database : getDatabaseNames()) {
            update.withNewDatabase(database);
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        sqlElasticPool(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "sql elastic pool " + getName();
    }

    private SqlElasticPool sqlElasticPool(Azure client) {
        if (sqlElasticPool == null) {
            sqlElasticPool = client.sqlServers().getById(getSqlServer().getId()).elasticPools().get(getName());
        }

        return sqlElasticPool;
    }
}
