package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlFailoverGroup;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query sql failover group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-failover-group: $(external-query azure::sql-failover-group {})
 */
@Type("sql-failover-group")
public class SqlFailoverGroupFinder extends AzureFinder<SqlFailoverGroup, SqlFailoverGroupResource> {
    private String sqlServerId;
    private String name;

    /**
     * The ID of the SQL Server.
     */
    public String getSqlServerId() {
        return sqlServerId;
    }

    public void setSqlServerId(String sqlServerId) {
        this.sqlServerId = sqlServerId;
    }

    /**
     * The name of the Failover Group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SqlFailoverGroup> findAllAzure(Azure client) {
        return client.sqlServers().list().stream().map(o -> o.failoverGroups().list()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SqlFailoverGroup> findAzure(Azure client, Map<String, String> filters) {
        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));
        if (sqlServer != null) {
            if (filters.containsKey("name")) {
                return Collections.singletonList(sqlServer.failoverGroups().get(filters.get("name")));
            } else {
                return sqlServer.failoverGroups().list();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
