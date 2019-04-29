package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;

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

@ResourceName("sql-failover-group")
public class SqlFailoverGroupResource extends AzureResource {

    private List<String> databaseIds;
    private String id;
    private Boolean manualReadAndWritePolicy;
    private String name;
    private List<String> partnerServerIds;
    private Boolean readOnlyPolicyEnabled;
    private Integer readWriteGracePeriod;
    private String sqlServerId;
    private Map<String, String> tags;

    @ResourceDiffProperty(updatable = true)
    public List<String> getDatabaseIds() {
        if (databaseIds == null) {
            databaseIds = new ArrayList<>();
        }

        return databaseIds;
    }

    public void setDatabaseIds(List<String> databaseIds) {
        this.databaseIds = databaseIds;
    }

    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getManualReadAndWritePolicy() {
        return manualReadAndWritePolicy;
    }

    public void setManualReadAndWritePolicy(Boolean manualReadAndWritePolicy) {
        this.manualReadAndWritePolicy = manualReadAndWritePolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getPartnerServerIds() {
        if (partnerServerIds == null) {
            partnerServerIds = new ArrayList<>();
        }

        return partnerServerIds;
    }

    public void setPartnerServerIds(List<String> partnerServerIds) {
        this.partnerServerIds = partnerServerIds;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getReadOnlyPolicyEnabled() {
        return readOnlyPolicyEnabled;
    }

    public void setReadOnlyPolicyEnabled(Boolean readOnlyPolicyEnabled) {
        this.readOnlyPolicyEnabled = readOnlyPolicyEnabled;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getReadWriteGracePeriod() {
        return readWriteGracePeriod;
    }

    public void setReadWriteGracePeriod(Integer readWriteGracePeriod) {
        this.readWriteGracePeriod = readWriteGracePeriod;
    }

    public String getSqlServerId() {
        return sqlServerId;
    }

    public void setSqlServerId(String sqlServerId) {
        this.sqlServerId = sqlServerId;
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

        SqlFailoverGroup failoverGroup = getFailoverGroups(client);

        if (failoverGroup == null) {
            return false;
        }

        setDatabaseIds(failoverGroup.databases());
        setId(failoverGroup.id());
        setManualReadAndWritePolicy(failoverGroup.readWriteEndpointPolicy() == ReadWriteEndpointFailoverPolicy.MANUAL ?
                true : false);
        setName(failoverGroup.name());
        getPartnerServerIds().clear();
        failoverGroup.partnerServers().forEach(server -> getPartnerServerIds().add(server.id()));
        setReadOnlyPolicyEnabled(failoverGroup.readOnlyEndpointPolicy() == ReadOnlyEndpointFailoverPolicy.ENABLED ? true : false);
        setReadWriteGracePeriod(failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        setTags(failoverGroup.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        WithReadWriteEndpointPolicy buildFailoverGroup = client.sqlServers().getById(getSqlServerId()).failoverGroups().define(getName());

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

        SqlFailoverGroup.Update update = getFailoverGroups(client).update();

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

        getFailoverGroups(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "failover group " + getName();
    }

    SqlFailoverGroup getFailoverGroups(Azure client) {
        return client.sqlServers().getById(getSqlServerId()).failoverGroups().get(getName());
    }
}
