package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.CreateMode;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.SampleName;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlDatabaseBasicStorage;
import com.microsoft.azure.management.sql.SqlDatabasePremiumServiceObjective;
import com.microsoft.azure.management.sql.SqlDatabasePremiumStorage;
import com.microsoft.azure.management.sql.SqlDatabaseStandardServiceObjective;
import com.microsoft.azure.management.sql.SqlDatabaseStandardStorage;
import com.microsoft.azure.management.sql.SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions;
import com.microsoft.azure.management.sql.SqlDatabaseOperations.DefinitionStages.WithExistingDatabaseAfterElasticPool;
import com.microsoft.azure.management.storage.StorageAccount;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
@ResourceType("sql-database")
public class SqlDatabaseResource extends AzureResource {

    private static final String BASIC_EDITION = "Basic";
    private static final String STANDARD_EDITION = "Standard";
    private static final String PREMIUM_EDITION = "Premium";

    private String collation;
    private String createMode;
    private String edition;
    private String editionServiceObjective;
    private String elasticPoolName;
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
    private SqlDatabase sqlDatabase;
    private Map<String, String> tags;

    /**
     * The collation of the database. (Optional)
     */
    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    /**
     * The create mode of the database. (Optional)
     */
    @Updatable
    public String getCreateMode() {
        return createMode;
    }

    public void setCreateMode(String createMode) {
        this.createMode = createMode;
    }

    /**
     * The edition of the database. Options include ``Basic``, ``Premium``, and ``Standard``. (Optional)
     */
    @Updatable
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    /**
     * The edition service objective of the database. Required when used with editions ``Basic``, ``Premium``, and ``Standard``. (Optional)
     */
    @Updatable
    public String getEditionServiceObjective() {
        return editionServiceObjective;
    }

    @Updatable
    public void setEditionServiceObjective(String editionServiceObjective) {
        this.editionServiceObjective = editionServiceObjective;
    }

    /**
     * The elastic pool associated with the database. (Optional)
     */
    @Updatable
    public String getElasticPoolName() {
        return elasticPoolName;
    }

    public void setElasticPoolName(String elasticPoolName) {
        this.elasticPoolName = elasticPoolName;
    }

    /**
     * The id of the database.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The container the source file is coming from. Required when used with storage account and source filename. (Optional)
     */
    public String getImportFromContainerName() {
        return importFromContainerName;
    }

    public void setImportFromContainerName(String importFromContainerName) {
        this.importFromContainerName = importFromContainerName;
    }

    /**
     * The source filename. Required when used with container name and storage account id. (Optional)
     */
    public String getImportFromFilename() {
        return importFromFilename;
    }

    public void setImportFromFilename(String importFromFilename) {
        this.importFromFilename = importFromFilename;
    }

    /**
     * The storage account id the source file is coming from. Required when used with container name and source filename. (Optional)
     */
    public String getImportFromStorageAccountId() {
        return importFromStorageAccountId;
    }

    public void setImportFromStorageAccountId(String importFromStorageAccountId) {
        this.importFromStorageAccountId = importFromStorageAccountId;
    }

    /**
     * The maximum size of the database. Required when used with ``Premium`` and ``Standard`` editions. (Optional)
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
     * The name of the database. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines if the sample database is used. (Optional)
     */
    public Boolean getWithSampleDatabase() {
        return withSampleDatabase;
    }

    public void setWithSampleDatabase(Boolean withSampleDatabase) {
        this.withSampleDatabase = withSampleDatabase;
    }

    /**
     * The name of the source database. (Optional)
     */
    public String getSourceDatabaseName() {
        return sourceDatabaseName;
    }

    public void setSourceDatabaseName(String sourceDatabaseName) {
        this.sourceDatabaseName = sourceDatabaseName;
    }

    /**
     * The source uri of the database to be imported. (Optional)
     */
    public String getStorageUri() {
        return storageUri;
    }

    public void setStorageUri(String storageUri) {
        this.storageUri = storageUri;
    }

    /**
     * The storage account related to a database to be imported. (Optional)
     */
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    /**
     * The sql server where the database is found. (Required)
     */
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags associated with the database. (Optional)
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
    public boolean refresh() {
        Azure client = createClient();

        SqlDatabase database = sqlDatabase(client);

        if (database == null) {
            return false;
        }

        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        setCollation(database.collation());
        setCreateMode(database.inner().createMode() == null ? null : database.inner().createMode().toString());
        setEdition(database.edition().toString());
        setEditionServiceObjective(database.serviceLevelObjective().toString());
        setElasticPoolName(database.elasticPoolName());
        setId(getSqlServer().getId() + "/databases/" + getName());
        setName(database.name());
        setMaxStorageCapacity(findMaxCapacity(database.maxSizeBytes()));
        setTags(database.inner().getTags());

        return true;
    }

    @Override
    public void create() {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        Azure client = createClient();

        WithAllDifferentOptions buildDatabase = client.sqlServers().getById(getSqlServer().getId()).databases().define(getName());

        //configures the source database within the elastic pool
        WithExistingDatabaseAfterElasticPool withExistingDatabaseAfterElasticPool;
        if (getElasticPoolName() != null) {
            withExistingDatabaseAfterElasticPool = buildDatabase.withExistingElasticPool(getElasticPoolName());

            if (getMaxStorageCapacity() != null) {
                withExistingDatabaseAfterElasticPool.
                        withMaxSizeBytes(SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()).capacity());
            }

            if (getCollation() != null) {
                withExistingDatabaseAfterElasticPool.withCollation(getCollation());
            }

            if (getImportFromContainerName() != null
                    && getImportFromFilename() != null
                    && getImportFromStorageAccountId() != null)
            {
                StorageAccount storageAccount = client.storageAccounts().getById(getStorageAccount().getId());
                withExistingDatabaseAfterElasticPool.importFrom(storageAccount,
                        getImportFromContainerName(),
                        getImportFromFilename())
                        .withSqlAdministratorLoginAndPassword(getSqlServer().getAdministratorLogin(), getSqlServer().getAdministratorPassword());
            } else if (getStorageUri() != null && getStorageAccount() != null) {
                buildDatabase.importFrom(getStorageUri()).withStorageAccessKey(getStorageAccount().getKeys().get("key1"))
                .withSqlAdministratorLoginAndPassword(getSqlServer().getAdministratorLogin(), getSqlServer().getAdministratorPassword());
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
                        buildDatabase.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()),
                                SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()));
                    } else {
                        buildDatabase.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()));
                    }
                } else if (STANDARD_EDITION.equalsIgnoreCase(getEdition())) {
                    if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                        buildDatabase.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()),
                                SqlDatabaseStandardStorage.valueOf(getMaxStorageCapacity()));
                    } else {
                        buildDatabase.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()));
                    }
                } else if (BASIC_EDITION.equalsIgnoreCase(getEdition())) {
                    if (getMaxStorageCapacity() != null) {
                        buildDatabase.withBasicEdition(SqlDatabaseBasicStorage.valueOf(getMaxStorageCapacity()));
                    } else {
                        buildDatabase.withBasicEdition();
                    }
                } else {
                    buildDatabase.withEdition(DatabaseEditions.fromString(getEdition()));
                }
            }
        }

        //pick the source of data for the database
        if (getSourceDatabaseName() != null && getCreateMode() != null) {
            SqlDatabase db = client.sqlServers().getById(getSqlServer().getId()).databases().get(getSourceDatabaseName());
            buildDatabase.withSourceDatabase(db).withMode(CreateMode.fromString(getCreateMode()));
        } else if (getImportFromStorageAccountId() != null && getImportFromContainerName() != null && getImportFromFilename() != null) {
            StorageAccount storageAccount = client.storageAccounts().getById(getImportFromStorageAccountId());
            buildDatabase.importFrom(storageAccount, getImportFromContainerName(), getImportFromFilename())
                    .withSqlAdministratorLoginAndPassword(getSqlServer().getAdministratorLogin(), getSqlServer().getAdministratorPassword());
        } else if (getStorageUri() != null && getStorageAccount() != null) {
            buildDatabase.importFrom(getStorageUri()).withStorageAccessKey(getStorageAccount().getKeys().get("key1"))
                    .withSqlAdministratorLoginAndPassword(getSqlServer().getAdministratorLogin(), getSqlServer().getAdministratorPassword());
        } else if (getWithSampleDatabase() != null) {
            buildDatabase.fromSample(SampleName.ADVENTURE_WORKS_LT);
        }

        buildDatabase.withTags(getTags());
        buildDatabase.create();

        setId(getSqlServer().getId() + "/databases/" + getName());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlDatabase.Update update = sqlDatabase(client).update();

        if (getElasticPoolName() != null) {
            update.withExistingElasticPool(getElasticPoolName());
        } else {
            update.withoutElasticPool();

            if (PREMIUM_EDITION.equalsIgnoreCase(getEdition())) {
                if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                    update.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()),
                            SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()));
                } else {
                    update.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()));
                }
            } else if (STANDARD_EDITION.equalsIgnoreCase(getEdition())) {
                if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                    update.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()),
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
    public void delete() {
        Azure client = createClient();

        sqlDatabase(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "sql database " + getName();
    }

    private String findMaxCapacity(Long storage) {
        for (SqlDatabasePremiumStorage val : SqlDatabasePremiumStorage.values()) {
            if (storage.equals(val.capacity())) {
                return val.toString();
            }
        }

        return null;
    }

    private SqlDatabase sqlDatabase(Azure client) {
        if (sqlDatabase == null) {
            sqlDatabase = client.sqlServers().getById(getSqlServer().getId()).databases().get(getName());
        }

        return sqlDatabase;
    }
}
