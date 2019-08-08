package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NicIpConfigurationResource extends AzureResource implements Copyable<NicIPConfiguration> {
    private String name;
    private PublicIpAddressResource publicIpAddress;
    private String privateIpAddress;
    private String privateIpAddressStatic;
    private Boolean ipAllocationStatic;
    private Boolean primary;
    private List<NicBackend> nicBackend;
    private List<NicNatRule> nicNatRule;

    /**
     * Name of the IP Configuration. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Public IP Address to be associated with the IP Configuration.
     */
    @Updatable
    public PublicIpAddressResource getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(PublicIpAddressResource publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * Private ip address to be associated with the IP Configuration.
     */
    @Updatable
    public String getPrivateIpAddressStatic() {
        return privateIpAddressStatic;
    }

    public void setPrivateIpAddressStatic(String privateIpAddressStatic) {
        this.privateIpAddressStatic = privateIpAddressStatic;
    }

    /**
     * Set ip allocation type to be static or dynamic. Defaults to ``false`` i.e dynamic.
     */
    @Updatable
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
     * Marks the IP Configuration as primary.
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
     * The load balancer backends associated with the IP Configuration.
     */
    @Updatable
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
     * The load balancer nat rules associated with the IP Configuration.
     */
    @Updatable
    public List<NicNatRule> getNicNatRule() {
        if (nicNatRule == null) {
            nicNatRule = new ArrayList<>();
        }

        return nicNatRule;
    }

    public void setNicNatRule(List<NicNatRule> nicNatRule) {
        this.nicNatRule = nicNatRule;
    }

    @Override
    public void copyFrom(NicIPConfiguration nicIpConfiguration) {
        setName(nicIpConfiguration.name());
        setPublicIpAddress(nicIpConfiguration.getPublicIPAddress() != null ? findById(PublicIpAddressResource.class, nicIpConfiguration.getPublicIPAddress().id()) : null);
        setPrivateIpAddress(nicIpConfiguration.privateIPAddress());
        setIpAllocationStatic(nicIpConfiguration.privateIPAllocationMethod().equals(IPAllocationMethod.STATIC));
        setPrivateIpAddressStatic(getIpAllocationStatic() ? getPrivateIpAddress() : null);

        getNicBackend().clear();
        for (LoadBalancerBackend backend : nicIpConfiguration.listAssociatedLoadBalancerBackends()) {
            NicBackend nicBackend = newSubresource(NicBackend.class);
            nicBackend.copyFrom(backend);
            getNicBackend().add(nicBackend);
        }

        getNicNatRule().clear();
        for(LoadBalancerInboundNatRule rule : nicIpConfiguration.listAssociatedLoadBalancerInboundNatRules()) {
            NicNatRule nicNatRule = newSubresource(NicNatRule.class);
            nicNatRule.copyFrom(rule);
            getNicNatRule().add(nicNatRule);
        }
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (getPrimary()) {
            return;
        }

        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIPConfiguration.UpdateDefinitionStages.WithPrivateIP<NetworkInterface.Update> updateWithPrivateIP = networkInterface.update()
                .defineSecondaryIPConfiguration(getName())
                .withExistingNetwork(client.networks().getById(parent.getNetwork().getId()))
                .withSubnet(parent.getSubnet());

        NicIPConfiguration.UpdateDefinitionStages.WithAttach<NetworkInterface.Update> updateWithAttach;

        if (getIpAllocationStatic()) {
            updateWithAttach = updateWithPrivateIP.withPrivateIPAddressStatic(getPrivateIpAddressStatic());
        } else {
            updateWithAttach = updateWithPrivateIP.withPrivateIPAddressDynamic();
        }

        if (getPublicIpAddress() != null) {
            updateWithAttach = updateWithAttach.withExistingPublicIPAddress(client.publicIPAddresses().getById(getPublicIpAddress().getId()));
        }

        for (NicBackend backend : getNicBackend()) {
            LoadBalancer loadBalancer = client.loadBalancers().getById(backend.getLoadBalancer().getId());
            updateWithAttach.withExistingLoadBalancerBackend(loadBalancer, backend.getBackendName());
        }

        for (NicNatRule rule : getNicNatRule()) {
            LoadBalancer loadBalancer = client.loadBalancers().getById(rule.getLoadBalancer().getId());
            updateWithAttach.withExistingLoadBalancerInboundNatRule(loadBalancer, rule.getInboundNatRuleName());
        }

        updateWithAttach.attach().apply();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIPConfiguration.Update update = networkInterface.update().updateIPConfiguration(getName())
                .withSubnet(parent.getSubnet());

        if (changedFieldNames.contains("public-ip-address")) {
            if (getPublicIpAddress() == null) {
                update = update.withoutPublicIPAddress();
            } else {
                update = update.withExistingPublicIPAddress(client.publicIPAddresses().getById(getPublicIpAddress().getId()));
            }
        }

        if (getIpAllocationStatic()) {
            update = update.withPrivateIPAddressStatic(getPrivateIpAddressStatic());
        } else {
            update = update.withPrivateIPAddressDynamic();
        }

        update.withoutLoadBalancerBackends();
        for (NicBackend backend : getNicBackend()) {
            LoadBalancer loadBalancer = client.loadBalancers().getById(backend.getLoadBalancer().getId());
            update.withExistingLoadBalancerBackend(loadBalancer, backend.getBackendName());
        }

        update.withoutLoadBalancerInboundNatRules();
        for (NicNatRule rule : getNicNatRule()) {
            LoadBalancer loadBalancer = client.loadBalancers().getById(rule.getLoadBalancer().getId());
            update.withExistingLoadBalancerInboundNatRule(loadBalancer, rule.getInboundNatRuleName());
        }

        NetworkInterface response = update.parent().apply();

        copyFrom(response.ipConfigurations().get(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        if (getPrimary()) {
            return;
        }

        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        networkInterface.update().withoutIPConfiguration(getName()).apply();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
