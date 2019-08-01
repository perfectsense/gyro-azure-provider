package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;

import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

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
 *     azure::network-interface network-interface-example
 *          network-interface-name: "network-interface-example"
 *          resource-group-name: $(azure::resource-group resource-group-network-interface-example | resource-group-name)
 *          network-id: $(azure::network network-example-interface | network-id)
 *          subnet: "subnet2"
 *          security-group-id: $(azure::network-security-group network-security-group-example-interface | network-security-group-id)
 *
 *          nic-ip-configuration
 *              ip-allocation-static: false
 *              ip-configuration-name: 'primary'
 *              primary: true
 *          end
 *
 *          nic-ip-configuration
 *              ip-configuration-name: "nic-ip-configuration-1"
 *          end
 *
 *          tags: {
 *              Name: "network-interface-example"
 *          }
 *     end
 */
@Type("network-interface")
public class NetworkInterfaceResource extends AzureResource {
    private String networkInterfaceName;
    private String resourceGroupName;
    private String networkId;
    private String subnet;
    private String staticIpAddress;
    private String securityGroupId;
    private String networkInterfaceId;
    private Map<String, String> tags;
    private List<NicIpConfigurationResource> nicIpConfiguration;

    /**
     * Name of the network interface. (Required)
     */
    @Id
    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    public void setNetworkInterfaceName(String networkInterfaceName) {
        this.networkInterfaceName = networkInterfaceName;
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
     * The id of the virtual network the interface is going be assigned with. (Required)
     */
    @Output
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * One of the subnet name from the assigned virtual network. (Required)
     */
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Choose to assign a static ip to the interface. Leave blank for dynamic ip.
     */
    public String getStaticIpAddress() {
        return staticIpAddress;
    }

    public void setStaticIpAddress(String staticIpAddress) {
        this.staticIpAddress = staticIpAddress;
    }

    /**
     * The id of a security group to be assigned with the interface.
     */
    @Updatable
    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
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

    /**
     * A list of ip configurations for the network interface.
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
    public boolean refresh() {
        Azure client = createClient();

        NetworkInterface networkInterface = getNetworkInterface(client);

        setNetworkInterfaceName(networkInterface.name());
        setSecurityGroupId(networkInterface.getNetworkSecurityGroup() != null ? networkInterface.getNetworkSecurityGroup().id() : null);
        setTags(networkInterface.tags());

        getNicIpConfiguration().clear();
        for (NicIPConfiguration nicIpConfiguration : networkInterface.ipConfigurations().values()) {
            NicIpConfigurationResource nicIpConfigurationResource = new NicIpConfigurationResource(nicIpConfiguration);

            if (nicIpConfiguration.isPrimary()) {
                nicIpConfigurationResource.setPrimary(true);
            }

            getNicIpConfiguration().add(nicIpConfigurationResource);
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkInterface.DefinitionStages.WithPrimaryPrivateIP withPrimaryPrivateIP = client.networkInterfaces()
            .define(getNetworkInterfaceName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroupName())
            .withExistingPrimaryNetwork(client.networks().getById(getNetworkId()))
            .withSubnet(getSubnet());

        NetworkInterface.DefinitionStages.WithCreate withCreate;

        if (!ObjectUtils.isBlank(getStaticIpAddress())) {
            withCreate = withPrimaryPrivateIP.withPrimaryPrivateIPAddressStatic(getStaticIpAddress());
        } else {
            withCreate = withPrimaryPrivateIP.withPrimaryPrivateIPAddressDynamic();
        }

        if (!ObjectUtils.isBlank(getSecurityGroupId())) {
            withCreate = withCreate.withExistingNetworkSecurityGroup(client.networkSecurityGroups().getById(getSecurityGroupId()));
        }

        NicIpConfigurationResource primary = null;
        for (NicIpConfigurationResource nic : getNicIpConfiguration()) {
            if (nic.getPrimary()) {
                primary = nic;
            }
        }

        if (primary.getNicBackend() != null) {
            for (NicBackend backend : primary.getNicBackend()) {
                withCreate.withExistingLoadBalancerBackend(client.loadBalancers().getByResourceGroup(getResourceGroupName(),
                        backend.getLoadBalancerName()), backend.getBackendPoolName());
            }
        }

        if (primary.getNicNatRule() != null) {
            for (NicNatRule rule : primary.getNicNatRule()) {
                withCreate.withExistingLoadBalancerInboundNatRule(client.loadBalancers().getByResourceGroup(getResourceGroupName(),
                        rule.getLoadBalancerName()), rule.getNatRuleName());
            }
        }

        NetworkInterface networkInterface = withCreate.withTags(getTags()).create();

        setNetworkInterfaceId(networkInterface.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkInterface networkInterface = getNetworkInterface(client);

        NetworkInterface.Update update = networkInterface.update();

        if (changedFieldNames.contains("security-group-id")) {
            if (ObjectUtils.isBlank(getSecurityGroupId())) {
                update = update.withoutNetworkSecurityGroup();
            } else {
                update = update.withExistingNetworkSecurityGroup(client.networkSecurityGroups().getById(getSecurityGroupId()));
            }
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.networkInterfaces().deleteByResourceGroup(getResourceGroupName(), getNetworkInterfaceName());
    }

    NetworkInterface getNetworkInterface(Azure client) {
        return client.networkInterfaces().getByResourceGroup(getResourceGroupName(), getNetworkInterfaceName());
    }
}
