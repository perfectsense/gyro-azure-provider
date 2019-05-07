package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.ServiceEndpointType;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

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
@ResourceName(parent = "network", value = "subnet")
public class SubnetResource extends AzureResource {

    private String addressPrefix;
    private String name;
    private String networkSecurityGroupId;
    private String routeTableId;
    private Map<String, List<String>> serviceEndpoints;

    public SubnetResource() {

    }

    public SubnetResource(Subnet subnet) {
        setAddressPrefix(subnet.addressPrefix());
        setName(subnet.name());
        setNetworkSecurityGroupId(subnet.networkSecurityGroupId());
        setRouteTableId(subnet.routeTableId());
        setServiceEndpoints(toServiceEndpoints(subnet.servicesWithAccess()));
    }

    /**
     * The address prefix in CIDR notation. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    /**
     * The name of the subnet. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource id of the associated network security group. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public String getNetworkSecurityGroupId() {
        return networkSecurityGroupId;
    }

    public void setNetworkSecurityGroupId(String networkSecurityGroupId) {
        this.networkSecurityGroupId = networkSecurityGroupId;
    }

    /**
     * The resource id of the associated route table. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public String getRouteTableId() {
        return routeTableId;
    }

    public void setRouteTableId(String routeTableId) {
        this.routeTableId = routeTableId;
    }

    /**
     * The service endpoints associated with the subnet. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
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
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        Azure client = createClient();

        NetworkResource parent = (NetworkResource) parent();

        Network network = parent.getNetwork(client);

        Subnet.UpdateDefinitionStages.WithAttach<Network.Update> updateWithAttach;
        updateWithAttach = network.update().defineSubnet(getName())
                .withAddressPrefix(getAddressPrefix());

        if (getNetworkSecurityGroupId() != null) {
            updateWithAttach.withExistingNetworkSecurityGroup(getNetworkSecurityGroupId());
        }

        if (getRouteTableId() != null) {
            updateWithAttach.withExistingRouteTable(getRouteTableId());
        }

        if (getServiceEndpoints() != null) {
            for (String endpoint : getServiceEndpoints().keySet()) {
                updateWithAttach.withAccessFromService(ServiceEndpointType.fromString(endpointType(endpoint)));
            }
        }

        updateWithAttach.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        NetworkResource parent = (NetworkResource) parent();

        Network network = parent.getNetwork(client);

        Subnet.Update update = network.update().updateSubnet(getName())
                .withAddressPrefix(getAddressPrefix());

        if (getNetworkSecurityGroupId() != null) {
            update.withExistingNetworkSecurityGroup(getNetworkSecurityGroupId());
        } else {
            update.withoutNetworkSecurityGroup();
        }

        if (getRouteTableId() != null) {
            update.withExistingRouteTable(getRouteTableId());
        } else {
            update.withoutRouteTable();
        }

        SubnetResource oldResource = (SubnetResource) current;

        if (getServiceEndpoints() != null || changedProperties.contains("service-endpoints")) {

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
    public void delete() {
        Azure client = createClient();

        NetworkResource parent = (NetworkResource) parent();

        Network network = parent.getNetwork(client);

        network.update().withoutSubnet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "subnet " + getName();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public String resourceIdentifier() {
        return null;
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
            throw new GyroException("Invalid enpoint type. Values are AzureCosmosDB, Sql, and Storage");
        }
    }
}
