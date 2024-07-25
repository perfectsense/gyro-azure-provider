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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.sql.models.ReadOnlyEndpointFailoverPolicy;
import com.azure.resourcemanager.sql.models.ReadWriteEndpointFailoverPolicy;
import com.azure.resourcemanager.sql.models.SqlFailoverGroup;
import com.azure.resourcemanager.sql.models.SqlFailoverGroupOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Min;
import gyro.core.validation.Required;

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
 *         database-ids: [$(azure::sql-database sql-database-example).id]
 *         sql-server: $(azure::sql-server sql-server-example)
 *         manual-read-and-write-policy: false
 *         read-write-grace-period: 60
 *         partner-server-ids: [$(azure::sql-server sql-server-example-partner-server).id]
 *         read-only-policy-enabled: false
 *     end
 */
@Type("sql-failover-group")
public class SqlFailoverGroupResource extends AzureResource implements Copyable<SqlFailoverGroup> {

    private Set<String> databaseIds;
    private String id;
    private Boolean manualReadAndWritePolicy;
    private String name;
    private Set<String> partnerServerIds;
    private Boolean readOnlyPolicyEnabled;
    private Integer readWriteGracePeriod;
    private SqlServerResource sqlServer;
    private Map<String, String> tags;

    /**
     * The databases within the failover group.
     */
    @Updatable
    public Set<String> getDatabaseIds() {
        if (databaseIds == null) {
            databaseIds = new HashSet<>();
        }

        return databaseIds;
    }

    public void setDatabaseIds(Set<String> databaseIds) {
        this.databaseIds = databaseIds;
    }

    /**
     * The ID of the failover group.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Determines whether the read and write policy is manual or automatic. Defaults to ``false``.
     */
    @Updatable
    public Boolean getManualReadAndWritePolicy() {
        if (manualReadAndWritePolicy == null) {
            manualReadAndWritePolicy = false;
        }

        return manualReadAndWritePolicy;
    }

    public void setManualReadAndWritePolicy(Boolean manualReadAndWritePolicy) {
        this.manualReadAndWritePolicy = manualReadAndWritePolicy;
    }

    /**
     * The name of the failover group.
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
     * The IDs of the partner servers.
     */
    @Updatable
    public Set<String> getPartnerServerIds() {
        if (partnerServerIds == null) {
            partnerServerIds = new HashSet<>();
        }

        return partnerServerIds;
    }

    public void setPartnerServerIds(Set<String> partnerServerIds) {
        this.partnerServerIds = partnerServerIds;
    }

    /**
     * Determines if the read only policy is enabled.
     */
    @Updatable
    public Boolean getReadOnlyPolicyEnabled() {
        return readOnlyPolicyEnabled;
    }

    public void setReadOnlyPolicyEnabled(Boolean readOnlyPolicyEnabled) {
        this.readOnlyPolicyEnabled = readOnlyPolicyEnabled;
    }

    /**
     * Determines the grace period. Required when used with the automatic read and write policy.
     */
    @Updatable
    @Min(60)
    public Integer getReadWriteGracePeriod() {
        return readWriteGracePeriod;
    }

    public void setReadWriteGracePeriod(Integer readWriteGracePeriod) {
        this.readWriteGracePeriod = readWriteGracePeriod;
    }

    /**
     * The sql server where the failover group is found.
     */
    @Required
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The tags for the failover group.
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
        setDatabaseIds(new HashSet<>(failoverGroup.databases()));
        setId(failoverGroup.id());
        setManualReadAndWritePolicy(failoverGroup.readWriteEndpointPolicy() == ReadWriteEndpointFailoverPolicy.MANUAL);
        setName(failoverGroup.name());
        getPartnerServerIds().clear();
        failoverGroup.partnerServers().forEach(server -> getPartnerServerIds().add(server.id()));
        setReadOnlyPolicyEnabled(failoverGroup.readOnlyEndpointPolicy() == ReadOnlyEndpointFailoverPolicy.ENABLED);
        setReadWriteGracePeriod(failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        setTags(failoverGroup.tags());
        setSqlServer(findById(SqlServerResource.class, failoverGroup.sqlServerName()));
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        SqlFailoverGroup failoverGroup = getSqlFailoverGroup(client);

        if (failoverGroup == null) {
            return false;
        }

        copyFrom(failoverGroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy buildFailoverGroup = client.sqlServers()
            .getById(getSqlServer().getId())
            .failoverGroups()
            .define(getName());

        SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer withPartnerServer;
        if (getManualReadAndWritePolicy() != null) {
            if (getManualReadAndWritePolicy()) {
                withPartnerServer = buildFailoverGroup.withManualReadWriteEndpointPolicy();
            } else {
                withPartnerServer = buildFailoverGroup.withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(
                    getReadWriteGracePeriod());
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

            copyFrom(failoverGroup);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createClient();

        SqlFailoverGroup.Update update = getSqlFailoverGroup(client).update();

        SqlFailoverGroupResource oldResource = (SqlFailoverGroupResource) current;

        if (getManualReadAndWritePolicy() != null) {
            if (getManualReadAndWritePolicy()) {
                update.withManualReadWriteEndpointPolicy();
            } else {
                update.withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(getReadWriteGracePeriod());
            }
        }

        Set<String> removeDatabaseIds = new HashSet<>(oldResource.getDatabaseIds());
        removeDatabaseIds.removeAll(getDatabaseIds());

        for (String databaseId : removeDatabaseIds) {
            update.withoutDatabaseId(databaseId);
        }

        Set<String> addDatabaseIds = new HashSet<>(getDatabaseIds());
        addDatabaseIds.removeAll(oldResource.getDatabaseIds());

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
        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        SqlFailoverGroup sqlFailoverGroup = getSqlFailoverGroup(client);

        if (sqlFailoverGroup != null) {
            sqlFailoverGroup.delete();
        }
    }

    private SqlFailoverGroup getSqlFailoverGroup(AzureResourceManager client) {
        SqlFailoverGroup sqlFailoverGroup = null;
        SqlServer sqlServer = client.sqlServers().getById(getSqlServer().getId());
        if (sqlServer != null) {
            sqlFailoverGroup = sqlServer.failoverGroups().get(getName());
        }

        return sqlFailoverGroup;
    }
}
