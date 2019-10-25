package gyro.azure.sql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query sql elastic pool.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-elastic-pool: $(external-query azure::sql-elastic-pool {})
 */
@Type("sql-elastic-pool")
public class SqlElasticPoolFinder extends AzureFinder<SqlElasticPool, SqlElasticPoolResource> {
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
     * The name of the elastic pool.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SqlElasticPool> findAllAzure(Azure client) {
        return client.sqlServers().list().stream().map(o -> o.elasticPools().list()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SqlElasticPool> findAzure(Azure client, Map<String, String> filters) {
        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));

        if (sqlServer == null) {
            return Collections.emptyList();
        }

        if (filters.containsKey("name")) {
            return Collections.singletonList(sqlServer.elasticPools().get(filters.get("name")));
        } else {
            return sqlServer.elasticPools().list();
        }
    }
}
