package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.RouteTable;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    route-table: $(external-query azure::route-table {})
 */
@Type("route-table")
public class RouteTableFinder extends AzureFinder<RouteTable, RouteTableResource> {
    private String id;

    /**
     * The ID of the Route Table.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    protected List<RouteTable> findAllAzure(Azure client) {
        return client.routeTables().list();
    }

    @Override
    protected List<RouteTable> findAzure(Azure client, Map<String, String> filters) {
        RouteTable routeTable = client.routeTables().getById(filters.get("id"));
        if (routeTable == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(routeTable);
        }
    }
}
