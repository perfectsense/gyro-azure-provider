package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query sql firewall rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-firewall-rule: $(external-query azure::sql-firewall-rule {})
 */
@Type("sql-firewall-rule")
public class SqlFirewallRuleFinder extends AzureFinder<SqlFirewallRule, SqlFirewallRuleResource> {
    private String sqlServerId;
    private String name;

    /**
     * The ID of the sql server.
     */
    public String getSqlServerId() {
        return sqlServerId;
    }

    public void setSqlServerId(String sqlServerId) {
        this.sqlServerId = sqlServerId;
    }

    /**
     * The name of the firewall rule.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SqlFirewallRule> findAllAzure(Azure client) {
        return client.sqlServers().list().stream().map(o -> o.firewallRules().list()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SqlFirewallRule> findAzure(Azure client, Map<String, String> filters) {
        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));
        if (sqlServer != null) {
            if (filters.containsKey("name")) {
                return Collections.singletonList(sqlServer.firewallRules().get(filters.get("name")));
            } else {
                return sqlServer.firewallRules().list();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
