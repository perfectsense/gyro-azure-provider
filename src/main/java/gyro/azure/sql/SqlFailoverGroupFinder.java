/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.sql;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.sql.models.SqlFailoverGroup;
import com.azure.resourcemanager.sql.models.SqlServer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

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
public class SqlFailoverGroupFinder extends AzureFinder<AzureResourceManager, SqlFailoverGroup, SqlFailoverGroupResource> {

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
     * The name of the failover group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SqlFailoverGroup> findAllAzure(AzureResourceManager client) {
        return client.sqlServers()
            .list()
            .stream()
            .map(o -> o.failoverGroups().list())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    protected List<SqlFailoverGroup> findAzure(AzureResourceManager client, Map<String, String> filters) {
        SqlServer sqlServer = client.sqlServers().getById(filters.get("sql-server-id"));

        if (sqlServer == null) {
            return Collections.emptyList();
        }

        if (filters.containsKey("name")) {
            return Collections.singletonList(sqlServer.failoverGroups().get(filters.get("name")));
        } else {
            return sqlServer.failoverGroups().list();
        }
    }
}
