package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;

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
 * .. code-block:: beam
 *
 *     azure::network network-example
 *          network-name: "network-example"
 *          resource-group-name: $(azure::resource-group resource-group-network-example | resource-group-name)
 *          address-spaces:  [
 *               "10.0.0.0/27",
 *               "10.1.0.0/27"
 *          ]
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
@ResourceName("network")
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
    @ResourceDiffProperty(updatable = true)
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

    @ResourceOutput
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        Network network = client.networks().getById(getNetworkId());

        setTags(network.tags());
        setAddressSpaces(network.addressSpaces()); // change to list
        setNetworkName(network.name());

        getSubnet().clear();
        if (!network.subnets().isEmpty()) {
            for (Subnet key : network.subnets().values()) {
                getSubnet().add(new SubnetResource(key));
            }
        }

        return true;
    }

    @Override
    public void create() {
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

        Network network = withAddressSpace.withSubnets(subnetMap())
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
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        Network network = client.networks().getById(getNetworkId());

        Network.Update update = network.update();

        if (changedProperties.contains("address-spaces")) {
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

            update.withSubnets(subnetMap());
        }

        if (changedProperties.contains("subnets")) {
            update = update.withSubnets(subnetMap());
        }

        if (changedProperties.contains("tags")) {
            update = update.withTags(getTags());
        }

        if (!changedProperties.isEmpty()) {
            update.apply();
        }

    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.networks().deleteById(getNetworkId());
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("network");

        if (!ObjectUtils.isBlank(getNetworkName())) {
            sb.append(" - ").append(getNetworkName());
        }

        if (!ObjectUtils.isBlank(getNetworkId())) {
            sb.append(" - ").append(getNetworkId());
        }

        return sb.toString();
    }

    Network getNetwork(Azure client) {
        return client.networks().getByResourceGroup(getResourceGroupName(), getNetworkName());
    }

    private Map<String, String> subnetMap() {
        Map<String, String> subnets = new HashMap<>();
        for (SubnetResource subnet : getSubnet()) {
            subnets.put(subnet.getName(), subnet.getAddressPrefix());
        }
        return subnets;
    }
}
