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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseBasicStorage;
import com.azure.resourcemanager.sql.models.SqlDatabaseOperations;
import com.azure.resourcemanager.sql.models.SqlDatabasePremiumServiceObjective;
import com.azure.resourcemanager.sql.models.SqlDatabasePremiumStorage;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardStorage;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import org.apache.commons.lang.StringUtils;

/**
 * Creates a sql database.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::sql-database sql-database-example-source
 *          name: "sql-database-example-source"
 *          collation: "SQL_Latin1_General_CP1_CI_AS"
 *          edition: "Basic"
 *          max-storage-capacity: "MAX_100_MB"
 *          sql-server: $(azure::sql-server sql-server-example)
 *          tags: {
 *              Name: "sql-database-example"
 *          }
 *     end
 */
@Type("sql-database")
public class SqlDatabaseResource extends AzureResource implements Copyable<SqlDatabase> {

    private static final String BASIC_EDITION = "Basic";
    private static final String STANDARD_EDITION = "Standard";
    private static final String PREMIUM_EDITION = "Premium";

    private String collation;
    private String createMode;
    private String edition;
    private String editionServiceObjective;
    private SqlElasticPoolResource elasticPool;
    private String id;
    private String importFromFilename;
    private String importFromContainerName;
    private String importFromStorageAccountId;
    private String maxStorageCapacity;
    private String name;
    private Boolean withSampleDatabase;
    private String sourceDatabaseName;
    private String storageUri;
    private StorageAccountResource storageAccount;
    private SqlServerResource sqlServer;
    private Map<String, String> tags;

    /**
     * The collation of the database.
     */
    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    /**
     * The create mode of the database.
     */
    @ValidStrings({
        "Copy",
        "Default",
        "NonReadableSecondary",
        "OnlineSecondary",
        "PointInTimeRestore",
        "Recovery",
        "Restore",
        "RestoreLongTermRetentionBackup" })
    public String getCreateMode() {
        return createMode;
    }

    public void setCreateMode(String createMode) {
        this.createMode = createMode;
    }

    /**
     * The edition of the database.
     */
    @ValidStrings({ "Basic", "Premium", "Standard" })
    @Updatable
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    /**
     * The edition service objective of the database. Required when used with editions ``Basic``, ``Premium``, or ``Standard``.
     */
    @Updatable
    public String getEditionServiceObjective() {
        return editionServiceObjective;
    }

    public void setEditionServiceObjective(String editionServiceObjective) {
        this.editionServiceObjective = editionServiceObjective;
    }

    /**
     * The Elastic Pool associated with the database.
     */
    @Updatable
    public SqlElasticPoolResource getElasticPool() {
        return elasticPool;
    }

    public void setElasticPool(SqlElasticPoolResource elasticPool) {
        this.elasticPool = elasticPool;
    }

    /**
     * The ID of the database.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The container the source file is coming from. Required when used with storage account and source filename.
     */
    public String getImportFromContainerName() {
        return importFromContainerName;
    }

    public void setImportFromContainerName(String importFromContainerName) {
        this.importFromContainerName = importFromContainerName;
    }

    /**
     * The source filename. Required when used with container name and storage account id.
     */
    public String getImportFromFilename() {
        return importFromFilename;
    }

    public void setImportFromFilename(String importFromFilename) {
        this.importFromFilename = importFromFilename;
    }

    /**
     * The storage account id the source file is coming from. Required when used with container name and source filename.
     */
    public String getImportFromStorageAccountId() {
        return importFromStorageAccountId;
    }

    public void setImportFromStorageAccountId(String importFromStorageAccountId) {
        this.importFromStorageAccountId = importFromStorageAccountId;
    }

    /**
     * The maximum size of the database. Required when used with ``Premium`` or ``Standard`` editions.
     */
    @Updatable
    public String getMaxStorageCapacity() {
        if (StringUtils.isNumeric(maxStorageCapacity)) {
            maxStorageCapacity = findMaxCapacity(Long.parseLong(maxStorageCapacity));
        }

        return maxStorageCapacity;
    }

    public void setMaxStorageCapacity(String maxStorageCapacity) {
        this.maxStorageCapacity = maxStorageCapacity;
    }

    /**
     * The name of the database.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines if the sample database is used.
     */
    public Boolean getWithSampleDatabase() {
        return withSampleDatabase;
    }

    public void setWithSampleDatabase(Boolean withSampleDatabase) {
        this.withSampleDatabase = withSampleDatabase;
    }

    /**
     * The name of the source database.
     */
    public String getSourceDatabaseName() {
        return sourceDatabaseName;
    }

    public void setSourceDatabaseName(String sourceDatabaseName) {
        this.sourceDatabaseName = sourceDatabaseName;
    }

    /**
     * The source uri of the database to be imported.
     */
    public String getStorageUri() {
        return storageUri;
    }

    public void setStorageUri(String storageUri) {
        this.storageUri = storageUri;
    }

    /**
     * The Storage Account related to a database to be imported.
     */
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    /**
     * The SQL Server where the database is found.
     */
    @Required
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags associated with the database.
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
    public void copyFrom(SqlDatabase database) {
        setCollation(database.collation());
        setCreateMode(
            database.innerModel().createMode() == null ? null : database.innerModel().createMode().toString());
        setSqlServer(findById(SqlServerResource.class, database.sqlServerName()));
        setElasticPool(findById(SqlElasticPoolResource.class, database.elasticPoolName()));

        if (getElasticPool() == null) {
            setEdition(database.edition().toString());
            setEditionServiceObjective(database.requestedServiceObjectiveName());
        }

        setMaxStorageCapacity(findMaxCapacity(database.maxSizeBytes()));
        setId(getSqlServer().getId() + "/databases/" + getName());
        setName(database.name());
        setTags(database.innerModel().tags());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        SqlDatabase database = getSqlDatabase(client);

        if (database == null) {
            return false;
        }

        copyFrom(database);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions buildDatabase = client.sqlServers()
            .getById(getSqlServer().getId())
            .databases()
            .define(getName());

        //configures the source database within the elastic pool
        SqlDatabaseOperations.DefinitionStages.WithExistingDatabaseAfterElasticPool withExistingDatabaseAfterElasticPool;
        if (getElasticPool() != null) {
            withExistingDatabaseAfterElasticPool = buildDatabase.withExistingElasticPool(getElasticPool().getName());

            if (getMaxStorageCapacity() != null) {
                withExistingDatabaseAfterElasticPool.
                    withMaxSizeBytes(SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()).capacity());
            }

            if (getCollation() != null) {
                withExistingDatabaseAfterElasticPool.withCollation(getCollation());
            }

            if (getImportFromContainerName() != null
                && getImportFromFilename() != null
                && getImportFromStorageAccountId() != null) {
                StorageAccount storageAccount = client.storageAccounts().getById(getStorageAccount().getId());
                withExistingDatabaseAfterElasticPool.importFrom(
                        storageAccount,
                        getImportFromContainerName(),
                        getImportFromFilename())
                    .withSqlAdministratorLoginAndPassword(
                        getSqlServer().getAdministratorLogin(),
                        getSqlServer().getAdministratorPassword());
            } else if (getStorageUri() != null && getStorageAccount() != null) {
                buildDatabase.importFrom(getStorageUri()).withStorageAccessKey(getStorageAccount().keys().get("key1"))
                    .withSqlAdministratorLoginAndPassword(
                        getSqlServer().getAdministratorLogin(),
                        getSqlServer().getAdministratorPassword());
            } else if (getWithSampleDatabase() != null) {
                withExistingDatabaseAfterElasticPool.fromSample(SampleName.ADVENTURE_WORKS_LT);
            } else if (getSourceDatabaseName() != null) {
                withExistingDatabaseAfterElasticPool.withSourceDatabase(getSourceDatabaseName())
                    .withMode(CreateMode.fromString(getCreateMode()));
            }
        } else {
            //or create a new database
            if (getCollation() != null) {
                buildDatabase.withCollation(getCollation());
            }
            if (getEdition() != null) {
                if (PREMIUM_EDITION.equalsIgnoreCase(getEdition())) {
                    if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                        buildDatabase.withPremiumEdition(
                            SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()),
                            SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()));
                    } else {
                        buildDatabase.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(
                            getEditionServiceObjective()));
                    }
                } else if (STANDARD_EDITION.equalsIgnoreCase(getEdition())) {
                    if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                        buildDatabase.withStandardEdition(
                            SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()),
                            SqlDatabaseStandardStorage.valueOf(getMaxStorageCapacity()));
                    } else {
                        buildDatabase.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(
                            getEditionServiceObjective()));
                    }
                } else if (BASIC_EDITION.equalsIgnoreCase(getEdition())) {
                    if (getMaxStorageCapacity() != null) {
                        buildDatabase.withBasicEdition(SqlDatabaseBasicStorage.valueOf(getMaxStorageCapacity()));
                    } else {
                        buildDatabase.withBasicEdition();
                    }
                }
            }
        }

        //pick the source of data for the database
        if (getSourceDatabaseName() != null && getCreateMode() != null) {
            SqlDatabase db = client.sqlServers()
                .getById(getSqlServer().getId())
                .databases()
                .get(getSourceDatabaseName());
            buildDatabase.withSourceDatabase(db).withMode(CreateMode.fromString(getCreateMode()));
        } else if (getImportFromStorageAccountId() != null && getImportFromContainerName() != null
            && getImportFromFilename() != null) {
            StorageAccount storageAccount = client.storageAccounts().getById(getImportFromStorageAccountId());
            buildDatabase.importFrom(storageAccount, getImportFromContainerName(), getImportFromFilename())
                .withSqlAdministratorLoginAndPassword(
                    getSqlServer().getAdministratorLogin(),
                    getSqlServer().getAdministratorPassword());
        } else if (getStorageUri() != null && getStorageAccount() != null) {
            buildDatabase.importFrom(getStorageUri()).withStorageAccessKey(getStorageAccount().keys().get("key1"))
                .withSqlAdministratorLoginAndPassword(
                    getSqlServer().getAdministratorLogin(),
                    getSqlServer().getAdministratorPassword());
        } else if (getWithSampleDatabase() != null) {
            buildDatabase.fromSample(SampleName.ADVENTURE_WORKS_LT);
        }

        buildDatabase.withTags(getTags());
        SqlDatabase sqlDatabase = buildDatabase.create();
        copyFrom(sqlDatabase);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createResourceManagerClient();

        SqlDatabase.Update update = getSqlDatabase(client).update();

        if (getElasticPool() != null) {
            update.withExistingElasticPool(getElasticPool().getName());
        } else {
            update.withoutElasticPool();

            if (PREMIUM_EDITION.equalsIgnoreCase(getEdition())) {
                if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                    update.withPremiumEdition(
                        SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()),
                        SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()));
                } else {
                    update.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()));
                }
            } else if (STANDARD_EDITION.equalsIgnoreCase(getEdition())) {
                if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                    update.withStandardEdition(
                        SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()),
                        SqlDatabaseStandardStorage.valueOf(getMaxStorageCapacity()));
                } else {
                    update.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()));
                }
            } else if (BASIC_EDITION.equalsIgnoreCase(getEdition())) {
                if (getMaxStorageCapacity() != null) {
                    update.withBasicEdition(SqlDatabaseBasicStorage.valueOf(getMaxStorageCapacity()));
                } else {
                    update.withBasicEdition();
                }
            }
        }

        update.withTags(getTags());
        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        SqlDatabase sqlDatabase = getSqlDatabase(client);
        if (sqlDatabase != null) {
            sqlDatabase.delete();
        }
    }

    private String findMaxCapacity(Long storage) {
        for (SqlDatabasePremiumStorage val : SqlDatabasePremiumStorage.values()) {
            if (storage.equals(val.capacity())) {
                return val.toString();
            }
        }

        return null;
    }

    private SqlDatabase getSqlDatabase(AzureResourceManager client) {
        SqlDatabase sqlDatabase = null;
        SqlServer sqlServer = client.sqlServers().getById(getSqlServer().getId());
        if (sqlServer != null) {
            sqlDatabase = sqlServer.databases().get(getName());
        }

        return sqlDatabase;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (PREMIUM_EDITION.equalsIgnoreCase(getEdition())) {
            if (!Arrays.stream(SqlDatabasePremiumStorage.values())
                .map(Enum::toString)
                .collect(Collectors.toSet())
                .contains(getMaxStorageCapacity())) {
                errors.add(new ValidationError(
                    this,
                    "max-storage-capacity",
                    "Invalid value for 'max-storage-capacity' when 'edition' set to 'Premium'."));
            } else if (!SqlDatabasePremiumServiceObjective.values()
                .stream()
                .map(ExpandableStringEnum::toString)
                .collect(Collectors.toSet())
                .contains(getEditionServiceObjective())) {
                errors.add(new ValidationError(
                    this,
                    "edition-service-objective",
                    "Invalid value for 'edition-service-objective' when 'edition' set to 'Premium'."));
            }
        } else if (STANDARD_EDITION.equalsIgnoreCase(getEdition())) {
            if (!Arrays.stream(SqlDatabaseStandardStorage.values())
                .map(Enum::toString)
                .collect(Collectors.toSet())
                .contains(getMaxStorageCapacity())) {
                errors.add(new ValidationError(
                    this,
                    "max-storage-capacity",
                    "Invalid value for 'max-storage-capacity' when 'edition' set to 'Standard'."));
            } else if (!SqlDatabaseStandardServiceObjective.values()
                .stream()
                .map(ExpandableStringEnum::toString)
                .collect(Collectors.toSet())
                .contains(getEditionServiceObjective())) {
                errors.add(new ValidationError(
                    this,
                    "edition-service-objective",
                    "Invalid value for 'edition-service-objective' when 'edition' set to 'Standard'."));
            }
        } else if (BASIC_EDITION.equalsIgnoreCase(getEdition())) {
            if (!Arrays.stream(SqlDatabaseBasicStorage.values())
                .map(Enum::toString)
                .collect(Collectors.toSet())
                .contains(getMaxStorageCapacity())) {
                errors.add(new ValidationError(
                    this,
                    "max-storage-capacity",
                    "Invalid value for 'max-storage-capacity' when 'edition' set to 'Basic'."));
            }

            if (!ObjectUtils.isBlank(getEditionServiceObjective())) {
                errors.add(new ValidationError(
                    this,
                    "edition-service-objective",
                    "Cannot set 'edition-service-objective' when 'edition' set to 'Basic'."));
            }
        }

        return errors;
    }
}
