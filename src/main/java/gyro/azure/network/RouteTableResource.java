package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Route;
import com.microsoft.azure.management.network.RouteNextHopType;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.Route.DefinitionStages.WithNextHopType;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

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
 * .. code-block:: beam
 *
 *     azure::route-table route-table-example
 *          bgp-route-propagation-disabled: true
 *          name: "route-table-example"
 *          resource-group-name: $(azure::resource-group resource-group-network-example | resource-group-name)
 *          routes
 *              destination-address-prefix: "10.0.1.0/24"
 *              name: "test-route"
 *              next-hop-type: "VirtualAppliance"
 *              next-hop-ip-address: "10.0.2.4"
 *          end
 *          subnet-ids: {
 *              subnet2: $(azure::network network-example | network-id)
 *          }
 *          tags: {
 *              Name: "route-table-example"
 *          }
 *     end
 */
@ResourceName("route-table")
public class RouteTableResource extends AzureResource {

    private Boolean bgpRoutePropagationDisabled;
    private String id;
    private String name;
    private String resourceGroupName;
    private List<Routes> routes;
    private Map<String, String> subnets;
    private Map<String, String> tags;

    /**
     * Determines whether to disable the routes learned by border gateway protocol on the route table. Defaults to true. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getBgpRoutePropagationDisabled() {
        if (bgpRoutePropagationDisabled == null) {
            bgpRoutePropagationDisabled = true;
        }

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

    /**
     * The routes of the route table. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<Routes> getRoutes() {
        if (routes == null) {
            routes = new ArrayList<>();
        }

        return routes;
    }

    public void setRoutes(List<Routes> routes) {
        this.routes = routes;
    }

    /**
     * The subnets and their network ids associated with the route table.
     * The subnet name is the key and the associated network id is the value for each entry (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public Map<String, String> getSubnets() {
        if (subnets == null) {
            subnets = new HashMap<>();
        }

        return subnets;
    }

    public void setSubnets(Map<String, String> subnets) {
        this.subnets = subnets;
    }

    /**
     * The tags associated with the route table. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
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


        if (getBgpRoutePropagationDisabled() == true) {
            withCreate.withDisableBgpRoutePropagation();
        }

        for (Routes route : getRoutes()) {
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

        for (Map.Entry<String, String> entry : getSubnets().entrySet()) {
            Network.Update update = client.networks().getById(entry.getValue()).update();
            update.updateSubnet(entry.getKey()).withExistingRouteTable(getId());
            update.apply();
        }
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

        if (getBgpRoutePropagationDisabled() == true) {
            update.withDisableBgpRoutePropagation();
        } else {
            update.withEnableBgpRoutePropagation();
        }

        Route.UpdateDefinitionStages.WithNextHopType<RouteTable.Update> updateWithNextHopType;
        for (Routes route : additions) {
            updateWithNextHopType =
                    update.defineRoute(route.getName())
                    .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            if (route.getNextHopType().equals("VirtualAppliance")) {
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
                        .withNextHopToVirtualAppliance(route.getNextHopIpAddress())
                        .withNextHop(RouteNextHopType.fromString(route.getNextHopType()))
                        .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            }
        }

        Map<String, String> subnetAdditions = new HashMap<>(getSubnets());
        currentResource.getSubnets().forEach((key, value) -> subnetAdditions.remove(key, value));

        Map<String, String> subnetSubtractions = new HashMap<>(currentResource.getSubnets());
        getSubnets().forEach((key, value) -> subnetSubtractions.remove(key, value));

        for (Map.Entry<String, String> entry : subnetAdditions.entrySet()) {
            Network.Update subnetUpdate = client.networks().getById(entry.getValue()).update();
            subnetUpdate.updateSubnet(entry.getKey()).withExistingRouteTable(getId());
            subnetUpdate.apply();
        }

        for (Map.Entry<String, String> entry : subnetSubtractions.entrySet()) {
            Network.Update subnetUpdate = client.networks().getById(entry.getValue()).update();
            subnetUpdate.updateSubnet(entry.getKey()).withoutRouteTable();
            subnetUpdate.apply();
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        for (Map.Entry<String, String> entry : getSubnets().entrySet()) {
            Network.Update subnetUpdate = client.networks().getById(entry.getValue()).update();
            subnetUpdate.updateSubnet(entry.getKey()).withoutRouteTable();
            subnetUpdate.apply();
        }

        client.routeTables().deleteById(getId());
    }

    @Override
    public String toDisplayString() {return "route table " + getName();}
}
