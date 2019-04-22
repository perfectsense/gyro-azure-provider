package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;

import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName(parent = "network-interface", value = "nic-ip-configuration")
public class NicIpConfigurationResource extends AzureResource {

    private String ipConfigurationName;
    private String publicIpAddressName;
    private String privateIpAddress;
    private String privateIpAddressStatic;
    private Boolean ipAllocationStatic;
    private Boolean primary;
    private List<NicBackend> nicBackend;
    private List<NicNatRule> nicNatRule;

    /**
     * Name of the ip configuration. (Required)
     */
    public String getIpConfigurationName() {
        return ipConfigurationName;
    }

    public void setIpConfigurationName(String ipConfigurationName) {
        this.ipConfigurationName = ipConfigurationName;
    }

    /**
     * Public ip address name to be associated with the ip config.
     */
    @ResourceDiffProperty(updatable = true)
    public String getPublicIpAddressName() {
        return publicIpAddressName;
    }

    public void setPublicIpAddressName(String publicIpAddressName) {
        this.publicIpAddressName = publicIpAddressName;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * Private ip address to be associated with the ip config.
     */
    @ResourceDiffProperty(updatable = true)
    public String getPrivateIpAddressStatic() {
        return privateIpAddressStatic;
    }

    public void setPrivateIpAddressStatic(String privateIpAddressStatic) {
        this.privateIpAddressStatic = privateIpAddressStatic;
    }

    /**
     * Set ip allocation type to be static or dynamic. Defaults to false i.e dynamic.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getIpAllocationStatic() {
        if (ipAllocationStatic == null) {
            ipAllocationStatic = false;
        }

        return ipAllocationStatic;
    }

    public void setIpAllocationStatic(Boolean ipAllocationStatic) {
        this.ipAllocationStatic = ipAllocationStatic;
    }

    /**
     * Marks the ip configuration as primary.
     */
    public Boolean getPrimary() {
        if (primary == null) {
            primary = false;
        }

        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    /**
     * The load balancer backends associated with the configuration.
     */
    @ResourceDiffProperty(updatable = true)
    public List<NicBackend> getNicBackend() {
        if (nicBackend == null) {
            nicBackend = new ArrayList<>();
        }

        return nicBackend;
    }

    public void setNicBackend(List<NicBackend> nicBackend) {
        this.nicBackend = nicBackend;
    }

    /**
     * The load balancer nat rules associated with the configuration.
     */
    @ResourceDiffProperty(updatable = true)
    public List<NicNatRule> getNicNatRule() {
        if (nicNatRule == null) {
            nicNatRule = new ArrayList<>();
        }

        return nicNatRule;
    }


    public void setNicNatRule(List<NicNatRule> nicNatRule) {
        this.nicNatRule = nicNatRule;
    }

    public NicIpConfigurationResource() {

    }

    public NicIpConfigurationResource(String ipConfigurationName) {
        setIpConfigurationName(ipConfigurationName);
        setPrimary(true);
    }

    public NicIpConfigurationResource(NicIPConfiguration nicIpConfiguration) {
        setIpConfigurationName(nicIpConfiguration.name());
        setPublicIpAddressName(nicIpConfiguration.getPublicIPAddress() != null ? nicIpConfiguration.getPublicIPAddress().name() : null);
        setPrivateIpAddress(nicIpConfiguration.privateIPAddress());
        setIpAllocationStatic(nicIpConfiguration.privateIPAllocationMethod().equals(IPAllocationMethod.STATIC));
        setPrivateIpAddressStatic(getIpAllocationStatic() ? getPrivateIpAddress() : null);
        refreshBackendsAndRules(nicIpConfiguration);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        if (getPrimary()) {
            return;
        }

        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIPConfiguration.UpdateDefinitionStages.WithPrivateIP<NetworkInterface.Update> updateWithPrivateIP = networkInterface.update()
                .defineSecondaryIPConfiguration(getIpConfigurationName())
                .withExistingNetwork(client.networks().getById(parent.getNetworkId()))
                .withSubnet(parent.getSubnet());

        NicIPConfiguration.UpdateDefinitionStages.WithAttach<NetworkInterface.Update> updateWithAttach;

        if (getIpAllocationStatic()) {
            updateWithAttach = updateWithPrivateIP.withPrivateIPAddressStatic(getPrivateIpAddressStatic());
        } else {
            updateWithAttach = updateWithPrivateIP.withPrivateIPAddressDynamic();
        }

        if (!ObjectUtils.isBlank(getPublicIpAddressName())) {
            updateWithAttach = updateWithAttach.withExistingPublicIPAddress(
                    client.publicIPAddresses()
                            .getByResourceGroup(parent.getResourceGroupName(),getPublicIpAddressName())
            );
        }

        if (getNicBackend() != null) {
            for (NicBackend backend : getNicBackend()) {
                LoadBalancer loadBalancer = client.loadBalancers().getByResourceGroup(parent.getResourceGroupName(), backend.getLoadBalancerName());
                updateWithAttach.withExistingLoadBalancerBackend(loadBalancer, backend.getBackendPoolName());
            }
        }

        if (getNicNatRule() != null) {
            for (NicNatRule rule : getNicNatRule()) {
                LoadBalancer loadBalancer = client.loadBalancers().getByResourceGroup(parent.getResourceGroupName(), rule.getLoadBalancerName());
                updateWithAttach.withExistingLoadBalancerInboundNatRule(loadBalancer, rule.getNatRuleName());
            }
        }

        updateWithAttach.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIPConfiguration.Update update = networkInterface.update().updateIPConfiguration(getIpConfigurationName())
                .withSubnet(parent.getSubnet());

        if (changedProperties.contains("public-ip-address-name")) {
            if (ObjectUtils.isBlank(getPublicIpAddressName())) {
                update = update.withoutPublicIPAddress();
            } else {
                update = update.withExistingPublicIPAddress(
                        client.publicIPAddresses()
                                .getByResourceGroup(parent.getResourceGroupName(),getPublicIpAddressName())
                );
            }
        }

        if (getIpAllocationStatic()) {
            update = update.withPrivateIPAddressStatic(getPrivateIpAddressStatic());
        } else {
            update = update.withPrivateIPAddressDynamic();
        }

        if (getNicBackend() != null) {
            update.withoutLoadBalancerBackends();
            for (NicBackend backend : getNicBackend()) {
                LoadBalancer loadBalancer = client.loadBalancers().getByResourceGroup(parent.getResourceGroupName(), backend.getLoadBalancerName());
                update.withExistingLoadBalancerBackend(loadBalancer, backend.getBackendPoolName());
            }
        }

        if (getNicNatRule() != null) {
            update.withoutLoadBalancerInboundNatRules();
            for (NicNatRule rule : getNicNatRule()) {
                LoadBalancer loadBalancer = client.loadBalancers().getByResourceGroup(parent.getResourceGroupName(), rule.getLoadBalancerName());
                update.withExistingLoadBalancerInboundNatRule(loadBalancer, rule.getNatRuleName());
            }
        }

        update.parent().apply();
    }

    @Override
    public void delete() {
        if (getPrimary()) {
            return;
        }

        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        networkInterface.update().withoutIPConfiguration(getIpConfigurationName()).apply();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("nic ip config");

        if (!ObjectUtils.isBlank(getIpConfigurationName())) {
            sb.append(" - ").append(getIpConfigurationName());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getIpConfigurationName());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private void refreshBackendsAndRules(NicIPConfiguration configuration) {
        getNicBackend().clear();
        for (LoadBalancerBackend backend : configuration.listAssociatedLoadBalancerBackends()) {
            getNicBackend().add(new NicBackend(backend));
        }

        getNicNatRule().clear();
        for(LoadBalancerInboundNatRule rule : configuration.listAssociatedLoadBalancerInboundNatRules()) {
            getNicNatRule().add(new NicNatRule(rule));
        }
    }
}
