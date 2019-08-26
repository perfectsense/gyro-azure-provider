package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        SqlServer sqlServer = client.sqlServers().getById(filters.get(""));
        if (sqlServer == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(sqlServer);
        }
    }
}
