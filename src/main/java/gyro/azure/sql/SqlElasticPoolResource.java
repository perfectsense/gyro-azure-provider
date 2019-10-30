/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.sql;

import com.microsoft.azure.management.sql.SqlDatabaseStandardStorage;
import com.microsoft.azure.management.sql.SqlElasticPoolOperations;
import com.microsoft.azure.management.sql.SqlServer;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
@Type("sql-elastic-pool")
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
    private SqlServerResource sqlServer;
    private String storageCapacity;
    private Map<String, String> tags;

    /**
     * The databases within the elastic pool. (Optional)
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
     * The maximum eDTU for the each database in the elastic pool. (Required)
     */
    @Required
    @Updatable
    public String getDtuMax() {
        return dtuMax;
    }

    public void setDtuMax(String dtuMax) {
        this.dtuMax = dtuMax;
    }

    /**
     * The minimum of eDTU for each database in the elastic pool. (Required)
     */
    @Required
    @Updatable
    public String getDtuMin() {
        return dtuMin;
    }

    public void setDtuMin(String dtuMin) {
        this.dtuMin = dtuMin;
    }

    /**
     * The total shared eDTU for the elastic pool. (Required)
     */
    @Required
    @Updatable
    public String getDtuReserved() {
        return dtuReserved;
    }

    public void setDtuReserved(String dtuReserved) {
        this.dtuReserved = dtuReserved;
    }

    /**
     * The edition of the elastic pool. Valid values are ``Basic``, ``Premium``, or  ``Standard`` (Required)
     */
    @Required
    @ValidStrings({"Basic", "Premium", "Standard"})
    @Updatable
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    /**
     * The ID of the elastic pool.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the elastic pool. (Required)
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The storage limit for the database elastic pool. Required when used with ``Standard`` or ``Premium`` editions. (Optional)
     */
    @Updatable
    public String getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(String storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    /**
     * The SQL Server where the elastic pool is found. (Required)
     */
    @Required
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags associated with the elastic pool. (Optional)
     */
    @Updatable
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
    public void copyFrom(SqlElasticPool elasticPool) {
        getDatabaseNames().clear();
        elasticPool.listDatabases().forEach(db -> getDatabaseNames().add(db.name()));
        setDtuMax("eDTU_" + elasticPool.databaseDtuMax());
        setDtuMin("eDTU_" + elasticPool.databaseDtuMin());
        setDtuReserved("eDTU_" + elasticPool.dtu());
        setEdition(elasticPool.edition().toString());
        setId(elasticPool.id());
        setName(elasticPool.name());
        setStorageCapacity(!getEdition().equals(EDITION_BASIC) ? Integer.toString(elasticPool.storageCapacityInMB()) : null);
        setTags(elasticPool.inner().getTags());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlElasticPool elasticPool = getSqlElasticPool(client);

        if (elasticPool == null) {
            return false;
        }

        copyFrom(elasticPool);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        Azure client = createClient();

        WithEdition buildPool = client.sqlServers().getById(getSqlServer().getId()).elasticPools().define(getName());

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
            throw new GyroException("Invalid edition. Valid values are Basic, Standard, and Premium");
        }

        for (String database : getDatabaseNames()) {
            elasticPool.withExistingDatabase(database);
        }

        elasticPool.withTags(getTags());

        SqlElasticPool pool = elasticPool.create();

        setId(pool.id());

        copyFrom(pool);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
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
        }

        for (String database : getDatabaseNames()) {
            update.withNewDatabase(database);
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        SqlElasticPool sqlElasticPool = getSqlElasticPool(client);

        if (sqlElasticPool != null) {
            sqlElasticPool.delete();
        }
    }

    private SqlElasticPool getSqlElasticPool(Azure client) {
        SqlElasticPool sqlElasticPool = null;
        SqlServer sqlServer = client.sqlServers().getById(getSqlServer().getId());
        if (sqlServer != null) {
            sqlElasticPool = sqlServer.elasticPools().get(getName());
        }

        return sqlElasticPool;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (EDITION_PREMIUM.equalsIgnoreCase(getEdition())) {
            if (!Arrays.stream(SqlElasticPoolPremiumMaxEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuMax())) {
                errors.add(new ValidationError(this, "dtu-max", "Invalid value for 'dtu-max' when 'edition' set to 'Premium'."));
            }

            if (!Arrays.stream(SqlElasticPoolPremiumMinEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuMin())) {
                errors.add(new ValidationError(this, "dtu-min", "Invalid value for 'dtu-min' when 'edition' set to 'Premium'."));
            }

            if (!Arrays.stream(SqlElasticPoolPremiumEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuReserved())) {
                errors.add(new ValidationError(this, "dtu-reserved", "Invalid value for 'dtu-reserved' when 'edition' set to 'Premium'."));
            }

            if (!Arrays.stream(SqlElasticPoolPremiumSorage.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getStorageCapacity())) {
                errors.add(new ValidationError(this, "storage-capacity", "Invalid value for 'storage-capacity' when 'edition' set to 'Premium'."));
            }
        } else if (EDITION_STANDARD.equalsIgnoreCase(getEdition())) {
            if (!Arrays.stream(SqlElasticPoolStandardMaxEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuMax())) {
                errors.add(new ValidationError(this, "dtu-max", "Invalid value for 'dtu-max' when 'edition' set to 'Standard'."));
            }

            if (!Arrays.stream(SqlElasticPoolStandardMinEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuMin())) {
                errors.add(new ValidationError(this, "dtu-min", "Invalid value for 'dtu-min' when 'edition' set to 'Standard'."));
            }

            if (!Arrays.stream(SqlElasticPoolStandardEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuReserved())) {
                errors.add(new ValidationError(this, "dtu-reserved", "Invalid value for 'dtu-reserved' when 'edition' set to 'Standard'."));
            }

            if (!Arrays.stream(SqlDatabaseStandardStorage.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getStorageCapacity())) {
                errors.add(new ValidationError(this, "storage-capacity", "Invalid value for 'storage-capacity' when 'edition' set to 'Standard'."));
            }
        } else if (EDITION_BASIC.equalsIgnoreCase(getEdition())) {
            if (!Arrays.stream(SqlElasticPoolBasicMaxEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuMax())) {
                errors.add(new ValidationError(this, "dtu-max", "Invalid value for 'dtu-max' when 'edition' set to 'Basic'."));
            }

            if (!Arrays.stream(SqlElasticPoolBasicMinEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuMin())) {
                errors.add(new ValidationError(this, "dtu-min", "Invalid value for 'dtu-min' when 'edition' set to 'Basic'."));
            }

            if (!Arrays.stream(SqlElasticPoolBasicEDTUs.values()).map(Enum::toString).collect(Collectors.toSet()).contains(getDtuReserved())) {
                errors.add(new ValidationError(this, "dtu-reserved", "Invalid value for 'dtu-reserved' when 'edition' set to 'Basic'."));
            }

            if (!ObjectUtils.isBlank(getStorageCapacity())) {
                errors.add(new ValidationError(this, "storage-capacity", "Cannot set 'storage-capacity' when 'edition' set to 'Basic'."));
            }
        }

        return errors;
    }
}
