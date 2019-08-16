package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;

import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a network interface.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::network-interface network-interface-example
 *         network-interface-name: "network-interface-example"
 *         resource-group: $(azure::resource-group resource-group-network-interface-example)
 *         network: $(azure::network network-example-interface)
 *         subnet: "subnet2"
 *         security-group: $(azure::network-security-group network-security-group-example-interface)
 *
 *         nic-ip-configuration
 *             ip-allocation-static: false
 *             name: 'primary'
 *             primary: true
 *         end
 *
 *         nic-ip-configuration
 *             name: "nic-ip-configuration-1"
 *         end
 *
 *         tags: {
 *             Name: "network-interface-example"
 *         }
 *    end
 */
@Type("network-interface")
public class NetworkInterfaceResource extends AzureResource implements Copyable<NetworkInterface> {
    private String name;
    private ResourceGroupResource resourceGroup;
    private NetworkResource network;
    private String subnet;
    private String staticIpAddress;
    private NetworkSecurityGroupResource securityGroup;
    private String id;
    private Map<String, String> tags;
    private List<NicIpConfigurationResource> nicIpConfiguration;

    /**
     * Name of the Network Interface. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group under which the Network Interface would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The Virtual Network the Network Interface is going be assigned with. (Required)
     */
    @Output
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * One of the subnet name from the assigned Virtual Network. (Required)
     */
    @Required
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Choose to assign a static ip to the Network Interface. Leave blank for dynamic ip.
     */
    public String getStaticIpAddress() {
        return staticIpAddress;
    }

    public void setStaticIpAddress(String staticIpAddress) {
        this.staticIpAddress = staticIpAddress;
    }

    /**
     * The Network Security Group to be assigned with the Network Interface.
     */
    @Updatable
    public NetworkSecurityGroupResource getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(NetworkSecurityGroupResource securityGroup) {
        this.securityGroup = securityGroup;
    }

    /**
     * The ID of the Network Interface.
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
     * The Tags for the Network Interface.
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

    /**
     * A list of IP Configurations for the Network Interface.
     *
     * @subresource gyro.azure.network.NicIpConfigurationResource
     */
    @Updatable
    public List<NicIpConfigurationResource> getNicIpConfiguration() {
        if (nicIpConfiguration == null) {
            nicIpConfiguration = new ArrayList<>();
        }
        return nicIpConfiguration;
    }

    public void setNicIpConfiguration(List<NicIpConfigurationResource> nicIpConfiguration) {
        this.nicIpConfiguration = nicIpConfiguration;
    }

    @Override
    public void copyFrom(NetworkInterface networkInterface) {
        setId(networkInterface.id());
        setName(networkInterface.name());
        setResourceGroup(findById(ResourceGroupResource.class, networkInterface.resourceGroupName()));
        setSecurityGroup(networkInterface.getNetworkSecurityGroup() != null ? findById(NetworkSecurityGroupResource.class, networkInterface.getNetworkSecurityGroup().id()) : null);
        setTags(networkInterface.tags());

        getNicIpConfiguration().clear();
        for (NicIPConfiguration nicIpConfiguration : networkInterface.ipConfigurations().values()) {
            NicIpConfigurationResource nicIpConfigurationResource = newSubresource(NicIpConfigurationResource.class);
            nicIpConfigurationResource.copyFrom(nicIpConfiguration);

            if (nicIpConfiguration.isPrimary()) {
                nicIpConfigurationResource.setPrimary(true);
            }

            getNicIpConfiguration().add(nicIpConfigurationResource);
        }
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        NetworkInterface networkInterface = getNetworkInterface(client);

        if (networkInterface == null) {
            return false;
        }

        copyFrom(networkInterface);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkInterface.DefinitionStages.WithPrimaryPrivateIP withPrimaryPrivateIP = client.networkInterfaces()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withExistingPrimaryNetwork(client.networks().getById(getNetwork().getId()))
            .withSubnet(getSubnet());

        NetworkInterface.DefinitionStages.WithCreate withCreate;

        if (!ObjectUtils.isBlank(getStaticIpAddress())) {
            withCreate = withPrimaryPrivateIP.withPrimaryPrivateIPAddressStatic(getStaticIpAddress());
        } else {
            withCreate = withPrimaryPrivateIP.withPrimaryPrivateIPAddressDynamic();
        }

        if (getSecurityGroup() != null) {
            withCreate = withCreate.withExistingNetworkSecurityGroup(client.networkSecurityGroups().getById(getSecurityGroup().getId()));
        }

        NicIpConfigurationResource primary = getNicIpConfiguration().stream().filter(NicIpConfigurationResource::getPrimary).findFirst().orElse(null);

        if (primary != null) {
            for (NicBackend backend : primary.getNicBackend()) {
                withCreate.withExistingLoadBalancerBackend(client.loadBalancers().getById(backend.getLoadBalancer().getId()), backend.getBackendName());
            }

            for (NicNatRule rule : primary.getNicNatRule()) {
                withCreate.withExistingLoadBalancerInboundNatRule(client.loadBalancers().getById(rule.getLoadBalancer().getId()), rule.getInboundNatRuleName());
            }
        }

        NetworkInterface networkInterface = withCreate.withTags(getTags()).create();

        copyFrom(networkInterface);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkInterface networkInterface = getNetworkInterface(client);

        NetworkInterface.Update update = networkInterface.update();

        if (changedFieldNames.contains("security-group")) {
            if (getSecurityGroup() == null) {
                update = update.withoutNetworkSecurityGroup();
            } else {
                update = update.withExistingNetworkSecurityGroup(client.networkSecurityGroups().getById(getSecurityGroup().getId()));
            }
        }

        NetworkInterface response = update.withTags(getTags()).apply();

        copyFrom(response);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.networkInterfaces().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }

    NetworkInterface getNetworkInterface(Azure client) {
        return client.networkInterfaces().getById(getId());
    }
}