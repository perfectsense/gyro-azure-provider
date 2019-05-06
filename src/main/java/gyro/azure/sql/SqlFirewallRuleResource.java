package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlFirewallRuleOperations.DefinitionStages.WithIPAddressRange;
import com.microsoft.azure.management.sql.SqlFirewallRuleOperations.DefinitionStages.WithCreate;

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
 *         sql-server-id: $(azure::sql-server sql-server-example | id)
 *     end
 */
@ResourceName("sql-firewall-rule")
public class SqlFirewallRuleResource extends AzureResource {

    private String id;
    private String startIpAddress;
    private String endIpAddress;
    private String name;
    private String sqlServerId;

    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ResourceDiffProperty(updatable = true)
    public String getStartIpAddress() {
        return startIpAddress;
    }

    public void setStartIpAddress(String startIpAddress) {
        this.startIpAddress = startIpAddress;
    }

    @ResourceDiffProperty(updatable = true)
    public String getEndIpAddress() {
        return endIpAddress;
    }

    public void setEndIpAddress(String endIpAddress) {
        this.endIpAddress = endIpAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSqlServerId() {
        return sqlServerId;
    }

    public void setSqlServerId(String sqlServerId) {
        this.sqlServerId = sqlServerId;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlFirewallRule firewallRule = getSqlFirewallRule(client);

        if (firewallRule == null) {
            return false;
        }

        setId(firewallRule.id());
        setStartIpAddress(firewallRule.startIPAddress());
        setEndIpAddress(firewallRule.endIPAddress());
        setName(firewallRule.name());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        WithIPAddressRange rule = client.sqlServers().getById(getSqlServerId()).firewallRules().define(getName());

        WithCreate withCreate;
        if (getStartIpAddress() != null) {
            withCreate = rule.withIPAddress(getStartIpAddress());
        } else {
            withCreate = rule.withIPAddressRange(getStartIpAddress(), getEndIpAddress());
        }

        SqlFirewallRule sqlFirewallRule = withCreate.create();

        setId(sqlFirewallRule.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlFirewallRule.Update update = getSqlFirewallRule(client).update();

        if (getStartIpAddress() != null && getEndIpAddress() != null) {
            update.withStartIPAddress(getStartIpAddress())
                    .withEndIPAddress(getEndIpAddress())
                    .apply();
        } else {
            update.withStartIPAddress(getStartIpAddress()).apply();
        }
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getSqlFirewallRule(client).delete();
    }

    @Override
    public String toDisplayString() {
        return "sql firewall rule " + getName();
    }

    SqlFirewallRule getSqlFirewallRule(Azure client) {
        return client.sqlServers().getById(getSqlServerId()).firewallRules().get(getName());
    }
}
