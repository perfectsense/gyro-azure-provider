package gyro.azure.sql;

import com.microsoft.azure.management.sql.SqlElasticPoolOperations;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;

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
import gyro.core.scope.State;

import java.util.HashMap;
import java.util.HashSet;
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
 *     elastic-pool sql-elastic-pool-example
 *         name: "sql-elastic-pool"
 *         edition: "Basic"
 *         dtu-min: "eDTU_0"
 *         dtu-max: "eDTU_5"
 *         dtu-reserved: "eDTU_50"
 *         tags: {
 *             Name: "sql-elastic-pool-example"
 *         }
 *     end
 */
public class SqlElasticPoolResource extends AzureResource implements Copyable<SqlElasticPool> {

    private static final String EDITION_BASIC = "Basic";
    private static final String EDITION_PREMIUM = "Premium";
    private static final String EDITION_STANDARD = "Standard";

    private Set<String> databaseNames;
    private String dtuMax;
    private String dtuMin;
    private String dtuReserved;
    private String edition;
    private String id;
    private String name;
    private String storageCapacity;
    private Map<String, String> tags;

    /**
     * The databases within the Elastic Pool. (Optional)
     */
    @Updatable
    public Set<String> getDatabaseNames() {
        if (databaseNames == null) {
            databaseNames = new HashSet<>();
        }

        return databaseNames;
    }

    public void setDatabaseNames(Set<String> databaseNames) {
        this.databaseNames = databaseNames;
    }

    /**
     * The maximum eDTU for the each database in the Elastic Pool. (Required)
     */
    @Updatable
    public String getDtuMax() {
        return dtuMax;
    }

    public void setDtuMax(String dtuMax) {
        this.dtuMax = dtuMax;
    }

    /**
     * The minimum of eDTU for each database in the Elastic Pool. (Required)
     */
    @Updatable
    public String getDtuMin() {
        return dtuMin;
    }

    public void setDtuMin(String dtuMin) {
        this.dtuMin = dtuMin;
    }

    /**
     * The total shared eDTU for the Elastic Pool. (Required)
     */
    @Updatable
    public String getDtuReserved() {
        return dtuReserved;
    }

    public void setDtuReserved(String dtuReserved) {
        this.dtuReserved = dtuReserved;
    }

    /**
     * The edition of the Elastic Pool. (Required)
     */
    @Updatable
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    /**
     * The ID of the Elastic Pool.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Elastic Pool. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The storage limit for the Elastic Pool. Required when used with ``Standard`` or ``Premium`` editions. (Optional)
     */
    @Updatable
    public String getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(String storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    /**
     * The tags associated with the Elastic Pool. (Optional)
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public void copyFrom(SqlElasticPool elasticPool) {
        getDatabaseNames().clear();
        elasticPool.listDatabases().forEach(db -> getDatabaseNames().add(db.name()));
        setDtuMax("eDTU_" + elasticPool.databaseDtuMax());
        setDtuMin("eDTU_" + elasticPool.databaseDtuMin());
        setDtuReserved("eDTU_" + elasticPool.dtu());
        setEdition(elasticPool.edition().toString());
        setId(elasticPool.id());
        setName(elasticPool.name());
        setStorageCapacity(Integer.toString(elasticPool.storageCapacityInMB()));
        //tags arn't refreshed
        //api fails to provide.
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        SqlServerResource parent = (SqlServerResource) parent();

        WithEdition buildPool = client.sqlServers().getById(parent.getId()).elasticPools().define(getName());

        SqlElasticPoolOperations.DefinitionStages.WithCreate elasticPool;

        if (EDITION_BASIC.equalsIgnoreCase(getEdition())) {
            elasticPool = buildPool.withBasicPool()
                    .withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolBasicEDTUs.valueOf(getDtuReserved()));
        } else if (EDITION_PREMIUM.equalsIgnoreCase(getEdition())) {
            elasticPool = buildPool.withPremiumPool()
                    .withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolPremiumEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolPremiumSorage.valueOf(getStorageCapacity()));
        } else if (EDITION_STANDARD.equalsIgnoreCase(getEdition())) {
            elasticPool = buildPool.withStandardPool()
                    .withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs.valueOf(getDtuMax()))
                    .withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs.valueOf(getDtuMin()))
                    .withReservedDtu(SqlElasticPoolStandardEDTUs.valueOf(getDtuReserved()))
                    .withStorageCapacity(SqlElasticPoolStandardStorage.valueOf(getStorageCapacity()));
        } else {
            throw new GyroException("Invalid edition. Valid values are Basic, Standard, or Premium");
        }

        for (String database : getDatabaseNames()) {
            elasticPool.withExistingDatabase(database);
        }

        elasticPool.withTags(getTags());

        SqlElasticPool pool = elasticPool.create();

        copyFrom(pool);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlElasticPool.Update update = sqlElasticPool(client).update();

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
            throw new GyroException("Invalid edition. Valid values are Basic, Standard, or Premium");
        }

        for (String database : getDatabaseNames()) {
            update.withNewDatabase(database);
        }

        SqlElasticPool pool = update.withTags(getTags()).apply();

        copyFrom(pool);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        sqlElasticPool(client).delete();
    }

    private SqlElasticPool sqlElasticPool(Azure client) {
        SqlServerResource parent = (SqlServerResource) parent();

        return client.sqlServers().getById(parent.getId()).elasticPools().get(getName());
    }
}
