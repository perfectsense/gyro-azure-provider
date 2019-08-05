package gyro.azure.sql;

import gyro.azure.AzureResource;
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
 *         network-id: $(azure::network sql-network-example | network-id)
 *         subnet-name: "subnet1"
 *         sql-server: $(azure::sql-server sql-server-example)
 *     end
 */
@Type("sql-virtual-network-rule")
public class SqlVirtualNetworkRuleResource extends AzureResource {

    private String id;
    private String name;
    private String networkId;
    private SqlServerResource sqlServer;
    private SqlVirtualNetworkRule sqlVirtualNetworkRule;
    private String subnetName;

    /**
     * The id of the virtual network rule.
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The network id where the subnet is found. (Required)
     */
    @Updatable
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * The sql server where the virtual network rule is found. (Required)
     */
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    /**
     * The name of a subnet within the specified network. (Required)
     */
    @Updatable
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    @Override
    public boolean doRefresh() {
        Azure client = createClient();

        SqlVirtualNetworkRule virtualNetworkRule = virtualNetworkRule(client);

        if (virtualNetworkRule == null) {
            return false;
        }

        setId(virtualNetworkRule.id());
        setName(virtualNetworkRule.name());
        setNetworkId(virtualNetworkRule.subnetId().split("/subnets/")[0]);
        setSubnetName(virtualNetworkRule.subnetId().split("/subnets/")[1]);

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }
        
        Azure client = createClient();

        WithServiceEndpoint withServiceEndpoint = client.sqlServers().getById(getSqlServer().getId()).virtualNetworkRules().define(getName())
                .withSubnet(getNetworkId(), getSubnetName());

        SqlVirtualNetworkRule virtualNetworkRule = withServiceEndpoint.create();

        setId(virtualNetworkRule.id());
    }

    @Override
    public void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlVirtualNetworkRule.Update update = virtualNetworkRule(client)
                .update()
                .withSubnet(getNetworkId(), getSubnetName());

        update.apply();
    }

    @Override
    public void doDelete(GyroUI ui, State state) {
        Azure client = createClient();

        virtualNetworkRule(client).delete();
    }

    private SqlVirtualNetworkRule virtualNetworkRule(Azure client) {
        if (sqlVirtualNetworkRule == null) {
            sqlVirtualNetworkRule = client.sqlServers().getById(getSqlServer().getId()).virtualNetworkRules().get(getName());
        }

        return sqlVirtualNetworkRule;
    }
}
