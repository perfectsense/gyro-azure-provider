package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import gyro.core.scope.State;

import java.util.Set;

/**
 * Creates a sql firewall rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     firewall-rule firewall
 *         start-ip-address: "10.0.0.0"
 *         end-ip-address: "10.0.0.0"
 *         name: "test firewall rule"
 *     end
 */
public class SqlFirewallRuleResource extends AzureResource implements Copyable<SqlFirewallRule> {

    private String id;
    private String startIpAddress;
    private String endIpAddress;
    private String name;

    /**
     * The ID of the Firewall Rule.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The starting or only ip address of the Firewall Rule. (Required)
     */
    @Updatable
    public String getStartIpAddress() {
        return startIpAddress;
    }

    public void setStartIpAddress(String startIpAddress) {
        this.startIpAddress = startIpAddress;
    }

    /**
     * The ending ip address of the Firewall Rule. (Required)
     */
    @Updatable
    public String getEndIpAddress() {
        return endIpAddress;
    }

    public void setEndIpAddress(String endIpAddress) {
        this.endIpAddress = endIpAddress;
    }

    /**
     * The name of the Firewall Rule. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(SqlFirewallRule firewallRule) {
        setId(firewallRule.id());
        setStartIpAddress(firewallRule.startIPAddress());
        setEndIpAddress(firewallRule.endIPAddress());
        setName(firewallRule.name());
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

        SqlFirewallRule sqlFirewallRule = client.sqlServers().getById(parent.getId())
            .firewallRules().define(getName())
            .withIPAddressRange(getStartIpAddress(), getEndIpAddress())
            .create();

        setId(sqlFirewallRule.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        sqlFirewallRule(client).update().withStartIPAddress(getStartIpAddress()).withEndIPAddress(getStartIpAddress()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        sqlFirewallRule(client).delete();
    }

    private SqlFirewallRule sqlFirewallRule(Azure client) {
        SqlServerResource parent = (SqlServerResource) parent();

        return client.sqlServers().getById(parent.getId()).firewallRules().get(getName());
    }
}
