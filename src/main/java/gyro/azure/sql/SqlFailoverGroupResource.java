package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFailoverGroup;
import com.microsoft.azure.management.sql.SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy;
import com.microsoft.azure.management.sql.ReadOnlyEndpointFailoverPolicy;
import com.microsoft.azure.management.sql.ReadWriteEndpointFailoverPolicy;
import com.microsoft.azure.management.sql.SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer;
import gyro.core.scope.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a sql failover group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure:sql-failover-group failover-example
 *         name: "sql-failover-example"
 *         database-ids: [$(azure::sql-database sql-database-example)]
 *         manual-read-and-write-policy: false
 *         read-write-grace-period: 2
 *         partner-server: [$(azure::sql-server sql-server-example-partner-server)]
 *         read-only-policy-enabled: false
 *     end
 */
@Type("sql-failover-group")
public class SqlFailoverGroupResource extends AzureResource implements Copyable<SqlFailoverGroup> {

    private Set<SqlDatabaseResource> database;
    private String id;
    private Boolean manualReadAndWritePolicy;
    private String name;
    private Set<SqlServerResource> partnerServer;
    private Boolean readOnlyPolicyEnabled;
    private Integer readWriteGracePeriod;
    private SqlServerResource sqlServer;
    private Map<String, String> tags;

    /**
     * The databases within the Failover Group. (Optional)
     */
    @Updatable
    public Set<SqlDatabaseResource> getDatabase() {
        if (database == null) {
            database = new HashSet<>();
        }

        return database;
    }

    public void setDatabase(Set<SqlDatabaseResource> database) {
        this.database = database;
    }

    /**
     * The ID of the Failover Group.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Determines whether the read and write policy is manual or automatic for the Failover Group. (Required)
     */
    @Updatable
    public Boolean getManualReadAndWritePolicy() {
        return manualReadAndWritePolicy;
    }

    public void setManualReadAndWritePolicy(Boolean manualReadAndWritePolicy) {
        this.manualReadAndWritePolicy = manualReadAndWritePolicy;
    }

    /**
     * The name of the Failover Group. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The set of partner servers for the Failover Group. (Optional)
     */
    @Updatable
    public Set<SqlServerResource> getPartnerServer() {
        if (partnerServer == null) {
            partnerServer = new HashSet<>();
        }

        return partnerServer;
    }

    public void setPartnerServer(Set<SqlServerResource> partnerServer) {
        this.partnerServer = partnerServer;
    }

    /**
     * Determines if the read only policy is enabled for the Failover Group. (Optional)
     */
    @Updatable
    public Boolean getReadOnlyPolicyEnabled() {
        return readOnlyPolicyEnabled;
    }

    public void setReadOnlyPolicyEnabled(Boolean readOnlyPolicyEnabled) {
        this.readOnlyPolicyEnabled = readOnlyPolicyEnabled;
    }

    /**
     * Determines the grace period for the Failover Group. Required when used with the automatic read and write policy. (Optional)
     */
    @Updatable
    public Integer getReadWriteGracePeriod() {
        return readWriteGracePeriod;
    }

    public void setReadWriteGracePeriod(Integer readWriteGracePeriod) {
        this.readWriteGracePeriod = readWriteGracePeriod;
    }

    /**
     * The Sql Server where the failover group is found. (Required)
     */
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags for the Failover Group. (Optional)
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
    public void copyFrom(SqlFailoverGroup failoverGroup) {
        setDatabase(failoverGroup.databases().stream().map(o -> findById(SqlDatabaseResource.class, o)).collect(Collectors.toSet()));
        setId(failoverGroup.id());
        setManualReadAndWritePolicy(failoverGroup.readWriteEndpointPolicy() == ReadWriteEndpointFailoverPolicy.MANUAL);
        setName(failoverGroup.name());
        getPartnerServer().clear();
        setPartnerServer(failoverGroup.partnerServers().stream().map(server -> findById(SqlServerResource.class, server.id())).collect(Collectors.toSet()));
        setReadOnlyPolicyEnabled(failoverGroup.readOnlyEndpointPolicy() == ReadOnlyEndpointFailoverPolicy.ENABLED);
        setReadWriteGracePeriod(failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        setTags(failoverGroup.tags());
        setSqlServer(findById(SqlServerResource.class, failoverGroup.parentId()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlFailoverGroup failoverGroup = sqlFailoverGroup(client);

        if (failoverGroup == null) {
            return false;
        }

        copyFrom(failoverGroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        WithReadWriteEndpointPolicy buildFailoverGroup = client.sqlServers().getById(getSqlServer().getId()).failoverGroups().define(getName());

        WithPartnerServer withPartnerServer;
        if (getManualReadAndWritePolicy() != null) {
            if (getManualReadAndWritePolicy()) {
                withPartnerServer = buildFailoverGroup.withManualReadWriteEndpointPolicy();
            } else {
                withPartnerServer = buildFailoverGroup.withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(getReadWriteGracePeriod());
            }

            for (String id : getPartnerServer().stream().map(SqlServerResource::getId).collect(Collectors.toList())) {
                withPartnerServer.withPartnerServerId(id);
            }

            for (String id : getDatabase().stream().map(SqlDatabaseResource::getId).collect(Collectors.toList())) {
                withPartnerServer.withDatabaseId(id);
            }

            if (getReadOnlyPolicyEnabled() != null) {
                if (getReadOnlyPolicyEnabled()) {
                    withPartnerServer.withReadOnlyEndpointPolicyEnabled();
                } else {
                    withPartnerServer.withReadOnlyEndpointPolicyDisabled();
                }
            }

            SqlFailoverGroup failoverGroup = withPartnerServer.withTags(getTags()).create();

            copyFrom(failoverGroup);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlFailoverGroup.Update update = sqlFailoverGroup(client).update();

        SqlFailoverGroupResource oldResource = (SqlFailoverGroupResource) current;

        if (getManualReadAndWritePolicy() != null) {
            if (getManualReadAndWritePolicy()) {
                update.withManualReadWriteEndpointPolicy();
            } else {
                update.withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(getReadWriteGracePeriod());
            }
        }

        Set<String> removeDatabaseIds = oldResource.getDatabase().stream().map(SqlDatabaseResource::getId).collect(Collectors.toSet());
        removeDatabaseIds.removeAll(getDatabase().stream().map(SqlDatabaseResource::getId).collect(Collectors.toSet()));

        for (String databaseId : removeDatabaseIds) {
            update.withoutDatabaseId(databaseId);
        }

        Set<String> addDatabaseIds = getDatabase().stream().map(SqlDatabaseResource::getId).collect(Collectors.toSet());
        addDatabaseIds.removeAll(oldResource.getDatabase().stream().map(SqlDatabaseResource::getId).collect(Collectors.toSet()));

        for (String databaseId : addDatabaseIds) {
            update.withNewDatabaseId(databaseId);
        }

        if (getReadOnlyPolicyEnabled() != null) {
            if (getReadOnlyPolicyEnabled()) {
                update.withReadOnlyEndpointPolicyEnabled();
            } else {
                update.withReadOnlyEndpointPolicyDisabled();
            }
        }

        update.withTags(getTags());
        SqlFailoverGroup failoverGroup = update.apply();
        copyFrom(failoverGroup);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        sqlFailoverGroup(client).delete();
    }

    private SqlFailoverGroup sqlFailoverGroup(Azure client) {
        return client.sqlServers().getById(getSqlServer().getId()).failoverGroups().get(getName());
    }
}
