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
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.network.NetworkResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlVirtualNetworkRule;
import com.microsoft.azure.management.sql.SqlVirtualNetworkRuleOperations.DefinitionStages.WithServiceEndpoint;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.Set;

/**
 * Creates a sql virtual network rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::sql-virtual-network-rule vnrule
 *         name: "test vn rule"
 *         network: $(azure::network sql-network-example)
 *         subnet-name: "subnet1"
 *         sql-server: $(azure::sql-server sql-server-example)
 *     end
 */
@Type("sql-virtual-network-rule")
public class SqlVirtualNetworkRuleResource extends AzureResource implements Copyable<SqlVirtualNetworkRule> {

    private String id;
    private String name;
    private NetworkResource network;
    private SqlServerResource sqlServer;
    private String subnetName;

    /**
     * The ID of the virtual network rule.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the virtual network rule. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Network where the virtual network rule. (Required)
     */
    @Required
    @Updatable
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The sql server where the virtual network rule is found. (Required)
     */
    @Required
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The name of a subnet within the specified Network. (Required)
     */
    @Required
    @Updatable
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    @Override
    public void copyFrom(SqlVirtualNetworkRule virtualNetworkRule) {
        setId(virtualNetworkRule.id());
        setName(virtualNetworkRule.name());
        setNetwork(findById(NetworkResource.class, virtualNetworkRule.subnetId().split("/subnets/")[0]));
        setSubnetName(virtualNetworkRule.subnetId().split("/subnets/")[1]);
        setSqlServer(findById(SqlServerResource.class, virtualNetworkRule.sqlServerName()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlVirtualNetworkRule virtualNetworkRule = getVirtualNetworkRule(client);

        if (virtualNetworkRule == null) {
            return false;
        }

        copyFrom(virtualNetworkRule);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }
        
        Azure client = createClient();

        WithServiceEndpoint withServiceEndpoint = client.sqlServers().getById(getSqlServer().getId()).virtualNetworkRules().define(getName())
                .withSubnet(getNetwork().getId(), getSubnetName());

        SqlVirtualNetworkRule virtualNetworkRule = withServiceEndpoint.create();

        setId(virtualNetworkRule.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlVirtualNetworkRule.Update update = getVirtualNetworkRule(client)
                .update()
                .withSubnet(getNetwork().getId(), getSubnetName());

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        SqlVirtualNetworkRule virtualNetworkRule = getVirtualNetworkRule(client);
        if (virtualNetworkRule != null) {
            virtualNetworkRule.delete();
        }
    }

    private SqlVirtualNetworkRule getVirtualNetworkRule(Azure client) {
        SqlVirtualNetworkRule sqlVirtualNetworkRule = null;
        SqlServer sqlServer = client.sqlServers().getById(getSqlServer().getId());

        if (sqlServer != null) {
            sqlVirtualNetworkRule = sqlServer.virtualNetworkRules().get(getName());
        }

        return sqlVirtualNetworkRule;
    }
}
