package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.network.NetworkResource;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
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
 *     virtual-network-rule vnrule
 *         name: "test vn rule"
 *         network: $(azure::network sql-network-example)
 *         subnet-name: "subnet1"
 *     end
 */
public class SqlVirtualNetworkRuleResource extends AzureResource implements Copyable<SqlVirtualNetworkRule> {

    private String id;
    private String name;
    private NetworkResource network;
    private String subnetName;

    /**
     * The ID of the Virtual Network Rule.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Virtual Network Rule. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Network where the to be attached subnet is found for the Virtual Network Rule. (Required)
     */
    @Updatable
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The name of a Subnet within the specified network for the Virtual Network Rule. (Required)
     */
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
        setNetwork(findById(NetworkResource.class,virtualNetworkRule.subnetId().split("/subnets/")[0]));
        setSubnetName(virtualNetworkRule.subnetId().split("/subnets/")[1]);
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        SqlServerResource parent = (SqlServerResource) parent();

        WithServiceEndpoint withServiceEndpoint = client.sqlServers().getById(parent.getId()).virtualNetworkRules().define(getName())
                .withSubnet(getNetwork().getId(), getSubnetName());

        SqlVirtualNetworkRule virtualNetworkRule = withServiceEndpoint.create();

        copyFrom(virtualNetworkRule);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlVirtualNetworkRule.Update update = virtualNetworkRule(client)
                .update()
                .withSubnet(getNetwork().getId(), getSubnetName());

        SqlVirtualNetworkRule virtualNetworkRule = update.apply();

        copyFrom(virtualNetworkRule);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        virtualNetworkRule(client).delete();
    }

    private SqlVirtualNetworkRule virtualNetworkRule(Azure client) {
        SqlServerResource parent = (SqlServerResource) parent();

        return client.sqlServers().getById(parent.getId()).virtualNetworkRules().get(getName());
    }
}
