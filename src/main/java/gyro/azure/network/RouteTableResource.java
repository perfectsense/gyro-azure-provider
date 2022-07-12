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

package gyro.azure.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.Route;
import com.azure.resourcemanager.network.models.Route.DefinitionStages.WithNextHopType;
import com.azure.resourcemanager.network.models.RouteNextHopType;
import com.azure.resourcemanager.network.models.RouteTable;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
 *          resource-group: $(azure::resource-group resource-group-network-example)
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
public class RouteTableResource extends AzureResource implements Copyable<RouteTable> {

    private Boolean bgpRoutePropagationDisabled;
    private String id;
    private String name;
    private ResourceGroupResource resourceGroup;
    private Set<RouteResource> route;
    private Map<String, String> tags;

    /**
     * Determines whether to disable the routes learned by border gateway protocol on the Route Table. Defaults to ``true``.
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
     * The ID of the Route Table.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Route Table.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group where the the Route Table resides.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The routes of the Route Table.
     */
    @Updatable
    public Set<RouteResource> getRoute() {
        if (route == null) {
            route = new HashSet<>();
        }

        return route;
    }

    public void setRoute(Set<RouteResource> route) {
        this.route = route;
    }

    /**
     * The tags associated with the Route Table.
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
    public void copyFrom(RouteTable routeTable) {
        setName(routeTable.name());
        setId(routeTable.id());
        setResourceGroup(findById(ResourceGroupResource.class, routeTable.resourceGroupName()));
        setBgpRoutePropagationDisabled(routeTable.isBgpRoutePropagationDisabled());

        getRoute().clear();
        for (Map.Entry<String, Route> routes : routeTable.routes().entrySet()) {
            RouteResource route = newSubresource(RouteResource.class);
            route.copyFrom(routes.getValue());
            getRoute().add(route);
        }

        setTags(routeTable.tags());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        RouteTable routeTable = client.routeTables().getById(getId());

        if (routeTable == null) {
            return false;
        }

        copyFrom(routeTable);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        RouteTable.DefinitionStages.WithCreate withCreate;
        withCreate = client.routeTables().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        if (getBgpRoutePropagationDisabled()) {
            withCreate.withDisableBgpRoutePropagation();
        }

        for (RouteResource route : getRoute()) {
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
        AzureResourceManager client = createClient();

        RouteTableResource currentResource = (RouteTableResource) current;

        RouteTable.Update update = client.routeTables()
            .getById(getId())
            .update();

        for (RouteResource route : currentResource.getRoute()) {
            update.withoutRoute(route.getName());
        }

        Route.UpdateDefinitionStages.WithNextHopType<RouteTable.Update> updateWithNextHopType;
        for (RouteResource route : getRoute()) {
            updateWithNextHopType =
                update.defineRoute(route.getName())
                    .withDestinationAddressPrefix(route.getDestinationAddressPrefix());
            if (route.getNextHopType().equals("VirtualAppliance")) {
                updateWithNextHopType.withNextHopToVirtualAppliance(route.getNextHopIpAddress()).attach();
            } else {
                updateWithNextHopType.withNextHop(RouteNextHopType.fromString(route.getNextHopType())).attach();
            }
        }

        if (getBgpRoutePropagationDisabled()) {
            update = update.withDisableBgpRoutePropagation();
        } else {
            update = update.withEnableBgpRoutePropagation();
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        client.routeTables().deleteById(getId());
    }
}
