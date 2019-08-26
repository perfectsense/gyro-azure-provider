package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("sql-database")
public class SqlDatabaseFinder extends AzureFinder<SqlDatabase, SqlDatabaseResource> {
    private String sqlServerId;
    private String name;

    /**
     * The ID of the SQL Server the Database belongs to.
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
        return client.sqlServers().list().stream().map(o -> o.databases().list()).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SqlDatabase> findAzure(Azure client, Map<String, String> filters) {
        if (ObjectUtils.isBlank(filters.get("sql-server-id"))) {
            throw new GyroException("'sql-server-id' is required.");
        }

        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));
        if (sqlServer == null) {
            return Collections.emptyList();
        } else {
            if (filters.containsKey("name")) {
                return Collections.singletonList(sqlServer.databases().get(filters.get("name")));
            } else {
                return sqlServer.databases().list();
            }
        }
    }
}
