package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlVirtualNetworkRule;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query sql virtual network rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-virtual-network-rule: $(external-query azure::sql-virtual-network-rule {})
 */
@Type("sql-virtual-network-rule")
public class SqlVirtualNetworkRuleFinder extends AzureFinder<SqlVirtualNetworkRule, SqlVirtualNetworkRuleResource> {
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
     * The name of the virtual network rule.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SqlVirtualNetworkRule> findAllAzure(Azure client) {
        return client.sqlServers().list().stream().map(o -> o.virtualNetworkRules().list()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SqlVirtualNetworkRule> findAzure(Azure client, Map<String, String> filters) {
        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));
        if (sqlServer != null) {
            if (filters.containsKey("name")) {
                return Collections.singletonList(sqlServer.virtualNetworkRules().get(filters.get("name")));
            } else {
                return sqlServer.virtualNetworkRules().list();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
