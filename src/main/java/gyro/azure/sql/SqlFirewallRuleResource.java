package gyro.azure.sql;

import gyro.azure.AzureResource;
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
public class SqlFirewallRuleResource extends AzureResource {

    private String id;
    private String startIpAddress;
    private String endIpAddress;
    private String name;
    private SqlFirewallRule sqlFirewallRule;
    private SqlServerResource sqlServer;

    /**
     * The id of the firewall rule. (Required)
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The starting or only ip address of the firewall rule. (Required)
     */
    @Updatable
    public String getStartIpAddress() {
        return startIpAddress;
    }

    public void setStartIpAddress(String startIpAddress) {
        this.startIpAddress = startIpAddress;
    }

    /**
     * The ending ip address of the firewall rule. (Optional)
     */
    @Updatable
    public String getEndIpAddress() {
        return endIpAddress;
    }

    public void setEndIpAddress(String endIpAddress) {
        this.endIpAddress = endIpAddress;
    }

    /**
     * The name of the firewall rule. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The sql server where the firewall rule is found. (Required)
     */
    public SqlServerResource getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(SqlServerResource sqlServer) {
        this.sqlServer = sqlServer;
    }

    @Override
    public boolean doRefresh() {
        Azure client = createClient();

        SqlFirewallRule firewallRule = sqlFirewallRule(client);

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
    public void doCreate(GyroUI ui, State state) {
        if (getSqlServer() == null) {
            throw new GyroException("You must provide a sql server resource.");
        }

        Azure client = createClient();

        WithIPAddressRange rule = client.sqlServers().getById(getSqlServer().getId()).firewallRules().define(getName());

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
    public void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlFirewallRule.Update update = sqlFirewallRule(client).update();

        if (getStartIpAddress() != null && getEndIpAddress() != null) {
            update.withStartIPAddress(getStartIpAddress())
                    .withEndIPAddress(getEndIpAddress())
                    .apply();
        } else {
            update.withStartIPAddress(getStartIpAddress()).apply();
        }
    }

    @Override
    public void doDelete(GyroUI ui, State state) {
        Azure client = createClient();

        sqlFirewallRule(client).delete();
    }

    private SqlFirewallRule sqlFirewallRule(Azure client) {
        if (sqlFirewallRule == null) {
            sqlFirewallRule = client.sqlServers().getById(getSqlServer().getId()).firewallRules().get(getName());
        }

        return sqlFirewallRule;
    }
}
