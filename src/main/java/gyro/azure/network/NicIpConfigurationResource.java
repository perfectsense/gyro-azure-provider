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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.fluent.models.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

public class NicIpConfigurationResource extends AzureResource implements Copyable<NicIpConfiguration> {

    private String name;
    private PublicIpAddressResource publicIpAddress;
    private String privateIpAddress;
    private Set<NicBackend> nicBackend;
    private Set<NicNatRule> nicNatRule;
    private Set<ApplicationSecurityGroupResource> applicationSecurityGroups;

    /**
     * Name of the IP Configuration.
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

    /**
     * The set of application security groups to attach to the IP configuration
     *
     * @Resource gyro.azure.network.ApplicationSecurityGroupResource
     */
    @Updatable
    public Set<ApplicationSecurityGroupResource> getApplicationSecurityGroups() {
        if (applicationSecurityGroups == null) {
            applicationSecurityGroups = new HashSet<>();
        }

        return applicationSecurityGroups;
    }

    public void setApplicationSecurityGroups(Set<ApplicationSecurityGroupResource> applicationSecurityGroups) {
        this.applicationSecurityGroups = applicationSecurityGroups;
    }

    @Override
    public void copyFrom(NicIpConfiguration nicIpConfiguration) {
        setName(nicIpConfiguration.name());
        setPublicIpAddress(nicIpConfiguration.getPublicIpAddress() != null ? findById(
            PublicIpAddressResource.class,
            nicIpConfiguration.getPublicIpAddress().id()) : null);
        setPrivateIpAddress(nicIpConfiguration.privateIpAddress());

        getNicBackend().clear();
        for (LoadBalancerBackend backend : nicIpConfiguration.listAssociatedLoadBalancerBackends()) {
            NicBackend nicBackend = newSubresource(NicBackend.class);
            nicBackend.copyFrom(backend);
            getNicBackend().add(nicBackend);
        }

        getNicNatRule().clear();
        for (LoadBalancerInboundNatRule rule : nicIpConfiguration.listAssociatedLoadBalancerInboundNatRules()) {
            NicNatRule nicNatRule = newSubresource(NicNatRule.class);
            nicNatRule.copyFrom(rule);
            getNicNatRule().add(nicNatRule);
        }

        getApplicationSecurityGroups().clear();
        if (nicIpConfiguration.innerModel().applicationSecurityGroups() != null) {
            setApplicationSecurityGroups(nicIpConfiguration.innerModel()
                .applicationSecurityGroups()
                .stream()
                .map(o -> findById(ApplicationSecurityGroupResource.class, o.id()))
                .collect(
                    Collectors.toSet()));
        }
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (isPrimary()) {
            // If a primary nic ip configuration has modified fields, then gyro needs to update it.
            // This is because the primary nic is automatically created on Network interface resource creation.
            update(ui, state, this, Collections.singleton("public-ip-address"));

            return;
        }

        AzureResourceManager client = createResourceManagerClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIpConfiguration.UpdateDefinitionStages.WithPrivateIP<NetworkInterface.Update> updateWithPrivateIP = networkInterface
            .update()
            .defineSecondaryIPConfiguration(getName())
            .withExistingNetwork(client.networks().getById(parent.getNetwork().getId()))
            .withSubnet(parent.getSubnet());

        NicIpConfiguration.UpdateDefinitionStages.WithAttach<NetworkInterface.Update> updateWithAttach;

        if (!ObjectUtils.isBlank(getPrivateIpAddress())) {
            updateWithAttach = updateWithPrivateIP.withPrivateIpAddressStatic(getPrivateIpAddress());
        } else {
            updateWithAttach = updateWithPrivateIP.withPrivateIpAddressDynamic();
        }

        if (getPublicIpAddress() != null) {
            updateWithAttach = updateWithAttach.withExistingPublicIpAddress(client.publicIpAddresses()
                .getById(getPublicIpAddress().getId()));
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

        if (!getApplicationSecurityGroups().isEmpty()) {
            addRemoveApplicationSecurityGroups(client, networkInterface);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createResourceManagerClient();

        NetworkInterfaceResource parent = (NetworkInterfaceResource) parent();

        NetworkInterface networkInterface = parent.getNetworkInterface(client);

        NicIpConfiguration.Update update = networkInterface.update().updateIPConfiguration(getName())
            .withSubnet(parent.getSubnet());

        if (changedFieldNames.contains("public-ip-address")) {
            if (getPublicIpAddress() == null) {
                update = update.withoutPublicIpAddress();
            } else {
                update = update.withExistingPublicIpAddress(client.publicIpAddresses()
                    .getById(getPublicIpAddress().getId()));
            }
        }

        if (!ObjectUtils.isBlank(getPrivateIpAddress())) {
            update = update.withPrivateIpAddressStatic(getPrivateIpAddress());
        } else {
            update = update.withPrivateIpAddressDynamic();
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

        update.parent().apply();

        addRemoveApplicationSecurityGroups(client, networkInterface);
    }

    private void addRemoveApplicationSecurityGroups(AzureResourceManager client, NetworkInterface networkInterface) {
        NetworkInterfaceIpConfigurationInner nicIPConfigurationInner = networkInterface.innerModel()
            .ipConfigurations()
            .stream()
            .filter(o -> o.name().equals(getName()))
            .findFirst()
            .get();

        boolean doUpdate = false;

        if (!getApplicationSecurityGroups().isEmpty()) {
            nicIPConfigurationInner.withApplicationSecurityGroups(getApplicationSecurityGroups().stream()
                .map(o -> new ApplicationSecurityGroupInner().withId(o.getId()))
                .collect(
                    Collectors.toList()));
            doUpdate = true;
        } else if (nicIPConfigurationInner.applicationSecurityGroups() != null
            && nicIPConfigurationInner.applicationSecurityGroups().size() > 0) {
            nicIPConfigurationInner.withApplicationSecurityGroups(Collections.emptyList());
            doUpdate = true;
        }

        if (doUpdate) {
            networkInterface.update().apply();
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        if (isPrimary()) {
            return;
        }

        AzureResourceManager client = createResourceManagerClient();

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
