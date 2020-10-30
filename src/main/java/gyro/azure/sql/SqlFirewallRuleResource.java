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

import com.microsoft.azure.management.sql.SqlServer;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlFirewallRuleOperations.DefinitionStages.WithIPAddressRange;
import com.microsoft.azure.management.sql.SqlFirewallRuleOperations.DefinitionStages.WithCreate;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.Set;

/**
 * Creates a sql firewall rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::sql-firewall-rule firewall
 *         start-ip-address: "10.0.0.0"
 *         name: "test firewall rule"
 *         sql-server: $(azure::sql-server sql-server-example)
 *     end
 */
@Type("sql-firewall-rule")
public class SqlFirewallRuleResource extends AzureResource implements Copyable<SqlFirewallRule> {

    private String id;
    private String startIpAddress;
    private String endIpAddress;
    private String name;
    private SqlServerResource sqlServer;

    /**
     * The ID of the firewall rule.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The starting or only ip address of the firewall rule.
     */
    @Required
    @Updatable
    public String getStartIpAddress() {
        return startIpAddress;
    }

    public void setStartIpAddress(String startIpAddress) {
        this.startIpAddress = startIpAddress;
    }

    /**
     * The ending ip address of the firewall rule.
     */
    @Updatable
    public String getEndIpAddress() {
        return endIpAddress;
    }

    public void setEndIpAddress(String endIpAddress) {
        this.endIpAddress = endIpAddress;
    }

    /**
     * The name of the firewall rule.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The SQL Server where the Firewall Rule is found.
     */
    @Required
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    @Override
    public void copyFrom(SqlFirewallRule firewallRule) {
        setId(firewallRule.id());
        setStartIpAddress(firewallRule.startIPAddress());
        setEndIpAddress(firewallRule.endIPAddress());
        setName(firewallRule.name());
        setSqlServer(findById(SqlServerResource.class, firewallRule.sqlServerName()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlFirewallRule firewallRule = getSqlFirewallRule(client);

        if (firewallRule == null) {
            return false;
        }

        boolean isEndIpAddressSet = !ObjectUtils.isBlank(getEndIpAddress());

        copyFrom(firewallRule);

        if (!isEndIpAddressSet) {
            setEndIpAddress(null);
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        Azure client = createClient();

        WithIPAddressRange rule = client.sqlServers().getById(getSqlServer().getId()).firewallRules().define(getName());

        WithCreate withCreate;
        if (ObjectUtils.isBlank(getEndIpAddress())) {
            withCreate = rule.withIPAddress(getStartIpAddress());
        } else {
            withCreate = rule.withIPAddressRange(getStartIpAddress(), getEndIpAddress());
        }

        SqlFirewallRule sqlFirewallRule = withCreate.create();

        setId(sqlFirewallRule.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlFirewallRule.Update update = getSqlFirewallRule(client).update();

        if (ObjectUtils.isBlank(getEndIpAddress())) {
            update.withStartIPAddress(getStartIpAddress()).withEndIPAddress(getStartIpAddress()).apply();
        } else {
            update.withStartIPAddress(getStartIpAddress())
                .withEndIPAddress(getEndIpAddress())
                .apply();
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        SqlFirewallRule sqlFirewallRule = getSqlFirewallRule(client);

        if (sqlFirewallRule != null) {
            sqlFirewallRule.delete();
        }
    }

    private SqlFirewallRule getSqlFirewallRule(Azure client) {
        SqlFirewallRule sqlFirewallRule = null;
        SqlServer sqlServer = client.sqlServers().getById(getSqlServer().getId());

        if (sqlServer != null) {
            sqlFirewallRule = sqlServer.firewallRules().get(getName());
        }

        return sqlFirewallRule;
    }
}
