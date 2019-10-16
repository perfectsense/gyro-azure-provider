package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;

import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NicIpConfigurationResource extends AzureResource implements Copyable<NicIPConfiguration> {
    private String name;
    private PublicIpAddressResource publicIpAddress;
    private String privateIpAddress;
    private Set<NicBackend> nicBackend;
    private Set<NicNatRule> nicNatRule;

    /**
     * Name of the IP Configuration. (Required)
     */
    @Required
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

    /**
     * The Private IP Address to be associated with the IP Configuration.
     */
    @Updatable
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The Load Balancer Backends associated with the IP Configuration.
     *
     * @subresource gyro.azure.network.NicBackend
     */
    @Updatable
    public Set<NicBackend> getNicBackend() {
        if (nicBackend == null) {
            nicBackend = new HashSet<>();
        }

        return nicBackend;
    }

    public void setNicBackend(Set<NicBackend> nicBackend) {
        this.nicBackend = nicBackend;
    }

    /**
     * The Load Balancer Nat Rules associated with the IP Configuration.
     *
     * @subresource gyro.azure.network.NicNatRule
     */
    @Updatable
    public Set<NicNatRule> getNicNatRule() {
        if (nicNatRule == null) {
            nicNatRule = new HashSet<>();
        }

        return nicNatRule;
    }

    public void setNicNatRule(Set<NicNatRule> nicNatRule) {
        this.nicNatRule = nicNatRule;
    }

    @Override
    public void copyFrom(NicIPConfiguration nicIpConfiguration) {
        setName(nicIpConfiguration.name());
        setPublicIpAddress(nicIpConfiguration.getPublicIPAddress() != null ? findById(PublicIpAddressResource.class, nicIpConfiguration.getPublicIPAddress().id()) : null);
        setPrivateIpAddress(nicIpConfiguration.privateIPAddress());

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
        if (isPrimary()) {
            //Update nic ip, as primary nic ip already present when nic was created.
            update(ui, state, this, Collections.singleton("public-ip-address"));
        }

        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIPConfiguration.UpdateDefinitionStages.WithPrivateIP<NetworkInterface.Update> updateWithPrivateIP = networkInterface.update()
            .defineSecondaryIPConfiguration(getName())
            .withExistingNetwork(client.networks().getById(parent.getNetwork().getId()))
            .withSubnet(parent.getSubnet());

        NicIPConfiguration.UpdateDefinitionStages.WithAttach<NetworkInterface.Update> updateWithAttach;

        if (!ObjectUtils.isBlank(getPrivateIpAddress())) {
            updateWithAttach = updateWithPrivateIP.withPrivateIPAddressStatic(getPrivateIpAddress());
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

        if (!ObjectUtils.isBlank(getPrivateIpAddress())) {
            update = update.withPrivateIPAddressStatic(getPrivateIpAddress());
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
        if (isPrimary()) {
            return;
        }

        Azure client = createClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        networkInterface.update().withoutIPConfiguration(getName()).apply();
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    boolean isPrimary() {
        return getName().equals("primary");
    }
}
