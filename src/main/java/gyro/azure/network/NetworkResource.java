package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.azure.AzureResource;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
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
 * Creates a virtual network.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::network network-example
 *          network-name: "network-example"
 *          resource-group-name: $(azure::resource-group resource-group-network-example | resource-group-name)
 *          address-spaces:  [
 *               "10.0.0.0/27",
 *               "10.1.0.0/27"
 *          ]
 *
 *          subnet
 *              address-prefix: "10.0.0.0/28"
 *              name: "subnet1"
 *          end
 *
 *          subnet
 *              address-prefix: "10.0.0.16/28"
 *              name: "subnet2"
 *     end
 *
 *          tags: {
 *              Name: "resource-group-network-example"
 *          }
 *     end
 */
@Type("network")
public class NetworkResource extends AzureResource {
    private String networkName;
    private String resourceGroupName;
    private List<String> addressSpaces;
    private List<SubnetResource> subnet;
    private Map<String, String> tags;
    private String networkId;

    /**
     * Name of the network. (Required)
     */
    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    /**
     * Name of the resource group under which this would reside. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * Address spaces for the network. (Required)
     */
    @Updatable
    public List<String> getAddressSpaces() {
        if (addressSpaces == null) {
            addressSpaces = new ArrayList<>();
        }

        return addressSpaces;
    }

    public void setAddressSpaces(List<String> addressSpaces) {
        this.addressSpaces = addressSpaces;
    }

    /**
     * Subnets for the network.
     *
     * @subresource gyro.azure.network.SubnetResource
     */
    public List<SubnetResource> getSubnet() {
        if (subnet == null) {
            subnet = new ArrayList<>();
        }

        return subnet;
    }

    public void setSubnet(List<SubnetResource> subnet) {
        this.subnet = subnet;
    }

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

    @Output
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @Override
    public boolean doRefresh() {
        Azure client = createClient();

        Network network = client.networks().getById(getNetworkId());

        setTags(network.tags());
        setAddressSpaces(network.addressSpaces()); // change to list
        setNetworkName(network.name());

        getSubnet().clear();
        if (!network.subnets().isEmpty()) {
            for (Subnet key : network.subnets().values()) {
                SubnetResource subnetResource = new SubnetResource(key);
                getSubnet().add(subnetResource);
            }
        }

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Azure client = createClient();

        Network.DefinitionStages.WithCreate networkDefWithoutAddress = client.networks()
            .define(getNetworkName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroupName());

        Network.DefinitionStages.WithCreateAndSubnet withAddressSpace = null;

        for (String addressSpace : getAddressSpaces()) {
            withAddressSpace = networkDefWithoutAddress.withAddressSpace(addressSpace);
        }

        //other options

        Network network = withAddressSpace
            .withTags(getTags())
            .create();

        network.addressSpaces();
        network.ddosProtectionPlanId();
        network.dnsServerIPs();
        network.isDdosProtectionEnabled();
        network.isVmProtectionEnabled();
        network.peerings();
        network.subnets();
        setNetworkId(network.id());

    }

    @Override
    public void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        Network network = client.networks().getById(getNetworkId());

        Network.Update update = network.update();

        if (changedFieldNames.contains("address-spaces")) {
            NetworkResource oldResource = (NetworkResource) current;

            List<String> removeAddressSpaces = oldResource.getAddressSpaces().stream()
                .filter(((Predicate<String>) new HashSet<>(getAddressSpaces())::contains).negate())
                .collect(Collectors.toList());

            for (String addressSpace : removeAddressSpaces) {
                update = update.withoutAddressSpace(addressSpace);
            }

            List<String> addAddressSpaces = getAddressSpaces().stream()
                .filter(((Predicate<String>) new HashSet<>(oldResource.getAddressSpaces())::contains).negate())
                .collect(Collectors.toList());

            for (String addressSpace : addAddressSpaces) {
                update = update.withAddressSpace(addressSpace);
            }
        }

        if (changedFieldNames.contains("tags")) {
            update = update.withTags(getTags());
        }

        if (!changedFieldNames.isEmpty()) {
            update.apply();
        }

    }

    @Override
    public void doDelete(GyroUI ui, State state) {
        Azure client = createClient();

        client.networks().deleteById(getNetworkId());
    }

    Network getNetwork(Azure client) {
        return client.networks().getByResourceGroup(getResourceGroupName(), getNetworkName());
    }
}
