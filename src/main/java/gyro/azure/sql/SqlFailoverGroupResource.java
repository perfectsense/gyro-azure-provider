package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFailoverGroup;
import com.microsoft.azure.management.sql.SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy;
import com.microsoft.azure.management.sql.ReadOnlyEndpointFailoverPolicy;
import com.microsoft.azure.management.sql.ReadWriteEndpointFailoverPolicy;
import com.microsoft.azure.management.sql.SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Creates a sql failover group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::sql-failover-group failover-example
 *         name: "sql-failover-example"
 *         database-ids: [$(azure::sql-database sql-database-example | id)]
 *         sql-server: $(azure::sql-server sql-server-example)
 *         manual-read-and-write-policy: false
 *         read-write-grace-period: 2
 *         partner-server-ids: [$(azure::sql-server sql-server-example-partner-server | id)]
 *        read-only-policy-enabled: false
 *     end
 */
@ResourceType("sql-failover-group")
public class SqlFailoverGroupResource extends AzureResource {

    private List<String> databaseIds;
    private String id;
    private Boolean manualReadAndWritePolicy;
    private String name;
    private List<String> partnerServerIds;
    private Boolean readOnlyPolicyEnabled;
    private Integer readWriteGracePeriod;
    private SqlFailoverGroup sqlFailoverGroup;
    private SqlServerResource sqlServer;
    private Map<String, String> tags;

    /**
     * The databases within the failover group. (Optional)
     */
    @ResourceUpdatable
    public List<String> getDatabaseIds() {
        if (databaseIds == null) {
            databaseIds = new ArrayList<>();
        }

        return databaseIds;
    }

    public void setDatabaseIds(List<String> databaseIds) {
        this.databaseIds = databaseIds;
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
     * Determines whether the read and write policy is manual or automatic. (Required)
     */
    @ResourceUpdatable
    public Boolean getManualReadAndWritePolicy() {
        return manualReadAndWritePolicy;
    }

    public void setManualReadAndWritePolicy(Boolean manualReadAndWritePolicy) {
        this.manualReadAndWritePolicy = manualReadAndWritePolicy;
    }

    /**
     * The name of the failover group. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ids of the partner servers. (Optional)
     */
    @ResourceUpdatable
    public List<String> getPartnerServerIds() {
        if (partnerServerIds == null) {
            partnerServerIds = new ArrayList<>();
        }

        return partnerServerIds;
    }

    public void setPartnerServerIds(List<String> partnerServerIds) {
        this.partnerServerIds = partnerServerIds;
    }

    /**
     * Determines if the read only policy is enabled. (Optional)
     */
    @ResourceUpdatable
    public Boolean getReadOnlyPolicyEnabled() {
        return readOnlyPolicyEnabled;
    }

    public void setReadOnlyPolicyEnabled(Boolean readOnlyPolicyEnabled) {
        this.readOnlyPolicyEnabled = readOnlyPolicyEnabled;
    }

    /**
     * Determines the grace period. Required when used with the automatic read and write policy. (Optional)
     */
    @ResourceUpdatable
    public Integer getReadWriteGracePeriod() {
        return readWriteGracePeriod;
    }

    public void setReadWriteGracePeriod(Integer readWriteGracePeriod) {
        this.readWriteGracePeriod = readWriteGracePeriod;
    }

    /**
     * The sql server where the failover group is found. (Required)
     */
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags for the failover group. (Optional)
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

        SqlFailoverGroup failoverGroup = sqlFailoverGroup(client);

        if (failoverGroup == null) {
            return false;
        }

        setDatabaseIds(failoverGroup.databases());
        setId(failoverGroup.id());
        setManualReadAndWritePolicy(failoverGroup.readWriteEndpointPolicy() == ReadWriteEndpointFailoverPolicy.MANUAL);
        setName(failoverGroup.name());
        getPartnerServerIds().clear();
        failoverGroup.partnerServers().forEach(server -> getPartnerServerIds().add(server.id()));
        setReadOnlyPolicyEnabled(failoverGroup.readOnlyEndpointPolicy() == ReadOnlyEndpointFailoverPolicy.ENABLED);
        setReadWriteGracePeriod(failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        setTags(failoverGroup.tags());

        return true;
    }

    @Override
    public void create() {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        Azure client = createClient();

        WithReadWriteEndpointPolicy buildFailoverGroup = client.sqlServers().getById(getSqlServer().getId()).failoverGroups().define(getName());

        WithPartnerServer withPartnerServer;
        if (getManualReadAndWritePolicy() != null) {
            if (getManualReadAndWritePolicy()) {
                withPartnerServer = buildFailoverGroup.withManualReadWriteEndpointPolicy();
            } else {
                withPartnerServer = buildFailoverGroup.withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(getReadWriteGracePeriod());
            }

            for (String id : getPartnerServerIds()) {
                withPartnerServer.withPartnerServerId(id);
            }

            for (String id : getDatabaseIds()) {
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

            setId(failoverGroup.id());
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
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

        List<String> removeDatabaseIds = oldResource.getDatabaseIds().stream()
                .filter(((Predicate<String>) new HashSet<>(getDatabaseIds())::contains).negate())
                .collect(Collectors.toList());

        List<String> addDatabaseIds = getDatabaseIds().stream()
                .filter(((Predicate<String>) new HashSet<>(oldResource.getDatabaseIds())::contains).negate())
                .collect(Collectors.toList());

        for (String databaseId : addDatabaseIds) {
            update.withNewDatabaseId(databaseId);
        }

        for (String databaseId : removeDatabaseIds) {
            update.withoutDatabaseId(databaseId);
        }

        if (getReadOnlyPolicyEnabled() != null) {
            if (getReadOnlyPolicyEnabled()) {
                update.withReadOnlyEndpointPolicyEnabled();
            } else {
                update.withReadOnlyEndpointPolicyDisabled();
            }
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        sqlFailoverGroup(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "failover group " + getName();
    }

    private SqlFailoverGroup sqlFailoverGroup(Azure client) {
        if (sqlFailoverGroup == null) {
            sqlFailoverGroup = client.sqlServers().getById(getSqlServer().getId()).failoverGroups().get(getName());
        }

        return sqlFailoverGroup;
    }
}
