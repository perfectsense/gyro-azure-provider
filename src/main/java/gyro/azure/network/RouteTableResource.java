package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Route;
import com.microsoft.azure.management.network.RouteNextHopType;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.network.Route.DefinitionStages.WithNextHopType;
import gyro.azure.AzureResource;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ResourceName("route-table")
public class RouteTableResource extends AzureResource {

    private Boolean bgpRoutePropagationDisabled;
    private String id;
    private String name;
    private String resourceGroupName;
    private List<Routes> routes;
    private List<String> subnetIds;
    private Map<String, String> tags;

    public Boolean getBgpRoutePropagationDisabled() {
        return bgpRoutePropagationDisabled;
    }

    public void setBgpRoutePropagationDisabled(Boolean bgpRoutePropagationDisabled) {
        this.bgpRoutePropagationDisabled = bgpRoutePropagationDisabled;
    }

    @ResourceOutput
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

    public List<Routes> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Routes> routes) {
        this.routes = routes;
    }

    public List<String> getSubnetIds() {
        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    public Map<String, String> getTags() {
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
            getRoutes().add(new Routes(routes.getValue()));
        }

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        RouteTable.DefinitionStages.WithCreate withCreate;
        withCreate = client.routeTables().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());

        for (Routes route : getRoutes()) {
            WithNextHopType<RouteTable.DefinitionStages.WithCreate> withCreateWithNextHopType;
            withCreateWithNextHopType = withCreate.defineRoute(route.getName())
                    .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            if (route.getNextHopType().equals("VIRTUAL_APPLIANCE")) {
                withCreateWithNextHopType.withNextHopToVirtualAppliance(route.getNextHopIpAddress()).attach();
            } else {
                withCreateWithNextHopType.withNextHop(RouteNextHopType.fromString(route.getNextHopType())).attach();
            }
        }

        RouteTable routeTable = withCreate.withTags(getTags()).create();
        setId(routeTable.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        RouteTableResource currentResource = (RouteTableResource) current;

        List<Routes> additions = new ArrayList<>(getRoutes());
        additions.removeAll(currentResource.getRoutes());

        List<Routes> subtractions = new ArrayList<>(currentResource.getRoutes());
        subtractions.removeAll(getRoutes());

        //add and delete routes
        RouteTable.Update update = client.routeTables()
                .getById(getId())
                .update();

        Route.UpdateDefinitionStages.WithNextHopType<RouteTable.Update> updateWithNextHopType;
        for (Routes route : additions) {
            updateWithNextHopType =
                    update.defineRoute(route.getName())
                    .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            if (route.getNextHopType().equals("VIRTUAL_APPLIANCE")) {
                updateWithNextHopType.withNextHopToVirtualAppliance(route.getNextHopIpAddress()).attach();
            } else {
                updateWithNextHopType.withNextHop(RouteNextHopType.fromString(route.getNextHopType())).attach();
            }
        }

        for (Routes route : subtractions) {
            update.withoutRoute(route.getName());
        }

        for (Routes route : getRoutes()) {
            if (!additions.contains(route) && !subtractions.contains(route)) {
                update.updateRoute(route.getName())
                        .withDestinationAddressPrefix(route.getDestinationAddressPrefix())
                .withNextHop(RouteNextHopType.fromString(route.getNextHopType()))
                .withNextHopToVirtualAppliance(route.getNextHopIpAddress());
            }
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.routeTables().deleteById(getId());
    }

    @Override
    public String toDisplayString() {return "route table " + getName();}
}
