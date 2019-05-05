package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.CreateMode;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlDatabaseBasicStorage;
import com.microsoft.azure.management.sql.SqlDatabasePremiumServiceObjective;
import com.microsoft.azure.management.sql.SqlDatabasePremiumStorage;
import com.microsoft.azure.management.sql.SqlDatabaseStandardServiceObjective;
import com.microsoft.azure.management.sql.SqlDatabaseStandardStorage;
import com.microsoft.azure.management.sql.SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions;
import com.microsoft.azure.management.sql.SqlDatabaseOperations.DefinitionStages.WithExistingDatabaseAfterElasticPool;
import com.microsoft.azure.management.sql.SqlDatabaseOperations.DefinitionStages.WithCreateMode;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ResourceName("sql-database")
public class SqlDatabaseResource extends AzureResource {

    private String collation;
    private String createMode;
    private String edition;
    private String editionServiceObjective;
    private String elasticPoolName;
    private String id;
    private String maxStorageCapacity;
    private String name;
    private String sourceDatabaseId;
    private Map<String, String> tags;

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    @ResourceDiffProperty(updatable = true)
    public String getCreateMode() {
        return createMode;
    }

    public void setCreateMode(String createMode) {
        this.createMode = createMode;
    }

    @ResourceDiffProperty(updatable = true)
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    @ResourceDiffProperty(updatable = true)
    public String getEditionServiceObjective() {
        return editionServiceObjective;
    }

    @ResourceDiffProperty(updatable = true)
    public void setEditionServiceObjective(String editionServiceObjective) {
        this.editionServiceObjective = editionServiceObjective;
    }

    @ResourceDiffProperty(updatable = true)
    public String getElasticPoolName() {
        return elasticPoolName;
    }

    public void setElasticPoolName(String elasticPoolName) {
        this.elasticPoolName = elasticPoolName;
    }

    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ResourceDiffProperty(updatable = true)
    public String getMaxStorageCapacity() {
        if (!StringUtils.isNumeric(maxStorageCapacity)) {
            if (getElasticPoolName() == null) {
                if (getEdition().equals("Basic")) {
                    return Long.toString(SqlDatabaseBasicStorage.valueOf(maxStorageCapacity).capacity());
                } else if (getEdition().equals("Standard")) {
                    return Long.toString(SqlDatabaseStandardStorage.valueOf(maxStorageCapacity).capacity());
                } else {
                    return Long.toString(SqlDatabasePremiumStorage.valueOf(maxStorageCapacity).capacity());
                }
            }
        }
        return maxStorageCapacity;
    }

    public void setMaxStorageCapacity(String maxStorageCapacity) {
        this.maxStorageCapacity = maxStorageCapacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceDatabaseId() {
        return sourceDatabaseId;
    }

    public void setSourceDatabaseId(String sourceDatabaseId) {
        this.sourceDatabaseId = sourceDatabaseId;
    }

    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    @ResourceDiffProperty(updatable = true)
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

        if (getSqlDatabase(client) == null) {
            return false;
        }

        SqlDatabase database = getSqlDatabase(client);

        setCollation(database.collation());
        setCreateMode(database.inner().createMode() == null ? null : database.inner().createMode().toString());
        setEdition(database.edition().toString());
        setEditionServiceObjective(database.serviceLevelObjective().toString());
        setElasticPoolName(database.elasticPoolName());
        setId(getSqlServerId() + "/databases/" + getName());
        setName(database.name());
        setMaxStorageCapacity(Long.toString(database.maxSizeBytes()));
        setSourceDatabaseId(database.inner().sourceDatabaseId());
        setTags(database.inner().getTags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        WithAllDifferentOptions buildDatabase = client.sqlServers().getById(getSqlServerId()).databases().define(getName());

        if (getCollation() != null) {
            buildDatabase.withCollation(getCollation());
        }

        WithCreateMode withCreateMode = null;
        if (getSourceDatabaseId() != null) {
            withCreateMode = buildDatabase.withSourceDatabase(getSourceDatabaseId());

            if (getCreateMode() != null) {
                withCreateMode.withMode(CreateMode.fromString(getCreateMode()));
            }
        }
        WithAllDifferentOptions buildDatabase = client.sqlServers().getById(getSqlServer().getId()).databases().define(getName());

        WithExistingDatabaseAfterElasticPool withExistingDatabaseAfterElasticPool = null;
        if (getElasticPoolName() != null) {
            withExistingDatabaseAfterElasticPool = buildDatabase.withExistingElasticPool(getElasticPoolName());

            if (getMaxStorageCapacity() != null) {
                withExistingDatabaseAfterElasticPool.withMaxSizeBytes(Long.parseLong(getMaxStorageCapacity()));
            }
        } else {
            if (getEdition() != null) {
                if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                    if (getEdition().equals("Premium")) {
                        buildDatabase.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()),
                                SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()));
                    } else if (getEdition().equals("Standard")) {
                        buildDatabase.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()),
                                SqlDatabaseStandardStorage.valueOf(getMaxStorageCapacity()));
                    }
                } else if (getEditionServiceObjective() != null) {
                    if (getEdition().equals("Basic")) {
                        buildDatabase.withBasicEdition(SqlDatabaseBasicStorage.valueOf(getEditionServiceObjective()));
                    } else if (getEdition().equals("Premium")) {
                        buildDatabase.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()));
                    } else if (getEdition().equals("Standard")) {
                        buildDatabase.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()));
                    }
                } else if (getEdition().equals("Basic")) {
                    buildDatabase.withBasicEdition();
                } else {
                    buildDatabase.withEdition(DatabaseEditions.fromString(getEdition()));
                }
            }
        }

        buildDatabase.withTags(getTags()).create();


        setId(getSqlServer().getId() + "/databases/" + getName());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlDatabase.Update update = getSqlDatabase(client).update();

        if (getElasticPoolName() != null) {
            update.withExistingElasticPool(getElasticPoolName());
        } else {
            update.withoutElasticPool();

            if (getEdition() != null) {
                if (getEditionServiceObjective() != null && getMaxStorageCapacity() != null) {
                    if (getEdition().equals("Premium")) {
                        update.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()),
                                SqlDatabasePremiumStorage.valueOf(getMaxStorageCapacity()));
                    } else if (getEdition().equals("Standard")) {
                        update.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()),
                                SqlDatabaseStandardStorage.valueOf(getMaxStorageCapacity()));
                    }
                } else if (getEditionServiceObjective() != null) {
                    if (getEdition().equals("Basic")) {
                        update.withBasicEdition(SqlDatabaseBasicStorage.valueOf(getEditionServiceObjective()));
                    } else if (getEdition().equals("Premium")) {
                        update.withPremiumEdition(SqlDatabasePremiumServiceObjective.fromString(getEditionServiceObjective()));
                    } else if (getEdition().equals("Standard")) {
                        update.withStandardEdition(SqlDatabaseStandardServiceObjective.fromString(getEditionServiceObjective()));
                    }
                } else if (getEdition().equals("Basic")) {
                    update.withBasicEdition();
                }
            }
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getSqlDatabase(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "sql database " + getName();
    }

    SqlDatabase getSqlDatabase(Azure client) {
        return client.sqlServers().getById(getSqlServer().getId()).databases().get(getName());
    }
}
