package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Route;
import com.microsoft.azure.management.network.RouteNextHopType;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.Route.DefinitionStages.WithNextHopType;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::route-table route-table-example
 *          bgp-route-propagation-disabled: true
 *          name: "route-table-example"
 *          resource-group-name: $(azure::resource-group resource-group-network-example | resource-group-name)
 *          route
 *              destination-address-prefix: "10.0.1.0/24"
 *              name: "test-route"
 *              next-hop-type: "VirtualAppliance"
 *              next-hop-ip-address: "10.0.2.4"
 *          end
 *          tags: {
 *              Name: "route-table-example"
 *          }
 *     end
 */
@Type("route-table")
public class RouteTableResource extends AzureResource {

    private Boolean bgpRoutePropagationDisabled;
    private String id;
    private String name;
    private String resourceGroupName;
    private List<gyro.azure.network.Route> route;
    private Map<String, String> tags;

    /**
     * Determines whether to disable the routes learned by border gateway protocol on the route table. Defaults to true. (Required)
     */
    @Updatable
    public Boolean getBgpRoutePropagationDisabled() {
        if (bgpRoutePropagationDisabled == null) {
            bgpRoutePropagationDisabled = true;
        }

        return bgpRoutePropagationDisabled;
    }

    public void setBgpRoutePropagationDisabled(Boolean bgpRoutePropagationDisabled) {
        this.bgpRoutePropagationDisabled = bgpRoutePropagationDisabled;
    }

    /**
     * The id of the route table. (Required)
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the route table. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The routes of the route table. (Optional)
     */
    @Updatable
    public List<gyro.azure.network.Route> getRoute() {
        if (route == null) {
            route = new ArrayList<>();
        }

        return route;
    }

    public void setRoute(List<gyro.azure.network.Route> route) {
        this.route = route;
    }

    /**
     * The tags associated with the route table. (Optional)
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        RouteTable routeTable = client.routeTables().getById(getId());

        setBgpRoutePropagationDisabled(routeTable.isBgpRoutePropagationDisabled());

        for (Map.Entry<String, Route> routes : routeTable.routes().entrySet()) {
            getRoute().add(new gyro.azure.network.Route(routes.getValue()));
        }

        setTags(routeTable.tags());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        RouteTable.DefinitionStages.WithCreate withCreate;
        withCreate = client.routeTables().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());


        if (getBgpRoutePropagationDisabled()) {
            withCreate.withDisableBgpRoutePropagation();
        }

        for (gyro.azure.network.Route route : getRoute()) {
            WithNextHopType<RouteTable.DefinitionStages.WithCreate> withCreateWithNextHopType;
            withCreateWithNextHopType = withCreate.defineRoute(route.getName())
                    .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            if (route.getNextHopType().equals("VirtualAppliance")) {
                withCreateWithNextHopType.withNextHopToVirtualAppliance(route.getNextHopIpAddress()).attach();
            } else {
                withCreateWithNextHopType.withNextHop(RouteNextHopType.fromString(route.getNextHopType())).attach();
            }
        }

        RouteTable routeTable = withCreate.withTags(getTags()).create();

        setId(routeTable.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        RouteTableResource currentResource = (RouteTableResource) current;

        List<gyro.azure.network.Route> additions = new ArrayList<>(getRoute());
        additions.removeAll(currentResource.getRoute());

        List<gyro.azure.network.Route> subtractions = new ArrayList<>(currentResource.getRoute());
        subtractions.removeAll(getRoute());

        RouteTable.Update update = client.routeTables()
                .getById(getId())
                .update();

        if (getBgpRoutePropagationDisabled()) {
            update.withDisableBgpRoutePropagation();
        } else {
            update.withEnableBgpRoutePropagation();
        }

        Route.UpdateDefinitionStages.WithNextHopType<RouteTable.Update> updateWithNextHopType;
        for (gyro.azure.network.Route route : additions) {
            updateWithNextHopType =
                    update.defineRoute(route.getName())
                    .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            if (route.getNextHopType().equals("VirtualAppliance")) {
                updateWithNextHopType.withNextHopToVirtualAppliance(route.getNextHopIpAddress()).attach();
            } else {
                updateWithNextHopType.withNextHop(RouteNextHopType.fromString(route.getNextHopType())).attach();
            }
        }

        for (gyro.azure.network.Route route : subtractions) {
            update.withoutRoute(route.getName());
        }

        for (gyro.azure.network.Route route : getRoute()) {
            if (!additions.contains(route) && !subtractions.contains(route)) {
                update.updateRoute(route.getName())
                        .withNextHopToVirtualAppliance(route.getNextHopIpAddress())
                        .withNextHop(RouteNextHopType.fromString(route.getNextHopType()))
                        .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            }
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.routeTables().deleteById(getId());
    }

    @Override
    public String toDisplayString() {return "route table " + getName();}
}
