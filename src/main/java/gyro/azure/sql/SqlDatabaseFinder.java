package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query sql database.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-database: $(external-query azure::sql-database {})
 */
@Type("sql-database")
public class SqlDatabaseFinder extends AzureFinder<SqlDatabase, SqlDatabaseResource> {
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
     * The name of the Database.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SqlDatabase> findAllAzure(Azure client) {
        return client.sqlServers().list().stream().map(o -> o.databases().list()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SqlDatabase> findAzure(Azure client, Map<String, String> filters) {
        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));
        if (sqlServer != null) {
            if (filters.containsKey("name")) {
                return Collections.singletonList(sqlServer.databases().get(filters.get("name")));
            } else {
                return sqlServer.databases().list();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
