package gyro.azure.network;

import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.ServiceEndpointType;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Creates a subnet.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     subnet
 *         address-prefix: "10.0.0.0/28"
 *         name: "subnet1"
 *     end
 */
public class SubnetResource extends AzureResource implements Copyable<Subnet> {

    private String addressPrefix;
    private String name;
    private NetworkSecurityGroupResource networkSecurityGroup;
    private RouteTableResource routeTable;
    private Map<String, List<String>> serviceEndpoints;

    /**
     * The address prefix in CIDR notation. (Required)
     */
    @Updatable
    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    /**
     * The name of the Subnet. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The associated Network Security Group. (Optional)
     */
    @Updatable
    public NetworkSecurityGroupResource getNetworkSecurityGroup() {
        return networkSecurityGroup;
    }

    public void setNetworkSecurityGroup(NetworkSecurityGroupResource networkSecurityGroup) {
        this.networkSecurityGroup = networkSecurityGroup;
    }

    /**
     * The associated Route Table. (Optional)
     */
    @Updatable
    public RouteTableResource getRouteTable() {
        return routeTable;
    }

    public void setRouteTable(RouteTableResource routeTable) {
        this.routeTable = routeTable;
    }

    /**
     * The service endpoints associated with the Subnet. (Optional)
     */
    @Updatable
    public Map<String, List<String>> getServiceEndpoints() {
        if (serviceEndpoints == null) {
            serviceEndpoints = new HashMap<>();
        }

        return serviceEndpoints;
    }

    public void setServiceEndpoints(Map<String, List<String>> serviceEndpoints) {
        this.serviceEndpoints = serviceEndpoints;
    }

    @Override
    public void copyFrom(Subnet subnet) {
        setAddressPrefix(subnet.addressPrefix());
        setName(subnet.name());
        setNetworkSecurityGroup(!ObjectUtils.isBlank(subnet.networkSecurityGroupId()) ? findById(NetworkSecurityGroupResource.class, subnet.networkSecurityGroupId()) : null);
        setRouteTable(!ObjectUtils.isBlank(subnet.routeTableId()) ? findById(RouteTableResource.class, subnet.routeTableId()) : null);
        setServiceEndpoints(toServiceEndpoints(subnet.servicesWithAccess()));
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkResource parent = (NetworkResource) parent();

        Network network = parent.getNetwork(client);

        Subnet.UpdateDefinitionStages.WithAttach<Network.Update> updateWithAttach;
        updateWithAttach = network.update().defineSubnet(getName())
            .withAddressPrefix(getAddressPrefix());

        if (getNetworkSecurityGroup() != null) {
            updateWithAttach.withExistingNetworkSecurityGroup(getNetworkSecurityGroup().getId());
        }

        if (getRouteTable() != null) {
            updateWithAttach.withExistingRouteTable(getRouteTable().getId());
        }

        if (getServiceEndpoints() != null) {
            for (String endpoint : getServiceEndpoints().keySet()) {
                updateWithAttach.withAccessFromService(ServiceEndpointType.fromString(endpointType(endpoint)));
            }
        }

        updateWithAttach.attach().apply();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkResource parent = (NetworkResource) parent();

        Network network = parent.getNetwork(client);

        Subnet.Update update = network.update().updateSubnet(getName())
            .withAddressPrefix(getAddressPrefix());

        if (getNetworkSecurityGroup() != null) {
            update.withExistingNetworkSecurityGroup(getNetworkSecurityGroup().getId());
        } else {
            update.withoutNetworkSecurityGroup();
        }

        if (getRouteTable() != null) {
            update.withExistingRouteTable(getRouteTable().getId());
        } else {
            update.withoutRouteTable();
        }

        if (changedFieldNames.contains("service-endpoints")) {
            SubnetResource oldResource = (SubnetResource) current;

            List<String> addServiceEndpoints = getServiceEndpoints().keySet().stream()
                .filter(((Predicate<String>) new HashSet<>(oldResource.getServiceEndpoints().keySet())::contains).negate())
                .collect(Collectors.toList());

            for (String endpoint : addServiceEndpoints) {
                update.withAccessFromService(ServiceEndpointType.fromString(endpointType(endpoint)));
            }

            List<String> removeServiceEndpoints = oldResource.getServiceEndpoints().keySet().stream()
                .filter(((Predicate<String>) new HashSet<>(getServiceEndpoints().keySet())::contains).negate())
                .collect(Collectors.toList());

            for (String endpoint : removeServiceEndpoints) {
                update.withoutAccessFromService(ServiceEndpointType.fromString(endpointType(endpoint)));
            }
        }

        update.parent().apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkResource parent = (NetworkResource) parent();

        Network network = parent.getNetwork(client);

        network.update().withoutSubnet(getName()).apply();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    private Map<String, List<String>> toServiceEndpoints(Map<ServiceEndpointType, List<Region>> serviceEndpointMap) {
        Map<String, List<String>> endpoints = new HashMap<>();

        for (Map.Entry<ServiceEndpointType, List<Region>> entry : serviceEndpointMap.entrySet()) {
            List<String> regions  = new ArrayList<>();
            for (Region region : entry.getValue()) {
                regions.add(region.toString());
            }
            endpoints.put(entry.getKey().toString().split("[.]")[1], regions);
        }
        return endpoints;
    }

    private String endpointType(String endpoint) {
        if (endpoint.equalsIgnoreCase("AzureCosmosDB")) {
            return "Microsoft.AzureCosmosDB";
        } else if (endpoint.equalsIgnoreCase("Sql")) {
            return "Microsoft.Sql";
        } else if (endpoint.equalsIgnoreCase("Storage")) {
            return "Microsoft.Storage";
        } else {
            throw new GyroException("Invalid endpoint type. Valid values are AzureCosmosDB, Sql, and Storage");
        }
    }
}
