package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query sql server.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-server: $(external-query azure::sql-server {})
 */
@Type("sql-server")
public class SqlServerFinder extends AzureFinder<SqlServer, SqlServerResource> {
    private String id;

    /**
     * The ID of the SQL Server.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<SqlServer> findAllAzure(Azure client) {
        return client.sqlServers().list();
    }

    @Override
    protected List<SqlServer> findAzure(Azure client, Map<String, String> filters) {
        SqlServer sqlServer = filters.containsKey("id") ? client.sqlServers().getById(filters.get("id")) : null;
        if (sqlServer != null) {
            return Collections.singletonList(sqlServer);
        } else {
            return Collections.emptyList();
        }
    }
}
