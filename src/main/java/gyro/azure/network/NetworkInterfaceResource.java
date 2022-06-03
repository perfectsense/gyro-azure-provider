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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;

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
    private NetworkSecurityGroupResource securityGroup;
    private String id;
    private Boolean ipForwarding;
    private Map<String, String> tags;
    private Set<NicIpConfigurationResource> nicIpConfiguration;

    /**
     * Name of the Network Interface.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group under which the Network Interface would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The Virtual Network the Network Interface is going be assigned with.
     */
    @Required
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * One of the subnet name from the assigned Virtual Network.
     */
    @Required
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
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
     * Enables IP forwarded. Used for NAT functionality. Defaults to ``false``
     */
    @Updatable
    public Boolean getIpForwarding() {
        return ipForwarding != null && ipForwarding;
    }

    public void setIpForwarding(Boolean ipForwarding) {
        this.ipForwarding = ipForwarding;
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
    public Set<NicIpConfigurationResource> getNicIpConfiguration() {
        if (nicIpConfiguration == null) {
            nicIpConfiguration = new HashSet<>();
        }
        return nicIpConfiguration;
    }

    public void setNicIpConfiguration(Set<NicIpConfigurationResource> nicIpConfiguration) {
        this.nicIpConfiguration = nicIpConfiguration;
    }

    @Override
    public void copyFrom(NetworkInterface networkInterface) {
        setId(networkInterface.id());
        setName(networkInterface.name());
        setResourceGroup(findById(ResourceGroupResource.class, networkInterface.resourceGroupName()));
        setSecurityGroup(networkInterface.getNetworkSecurityGroup() != null ? findById(
            NetworkSecurityGroupResource.class,
            networkInterface.getNetworkSecurityGroup().id()) : null);
        setTags(networkInterface.tags());
        setIpForwarding(networkInterface.isIPForwardingEnabled());

        getNicIpConfiguration().clear();
        for (NicIpConfiguration nicIpConfiguration : networkInterface.ipConfigurations().values()) {
            NicIpConfigurationResource nicIpConfigurationResource = newSubresource(NicIpConfigurationResource.class);
            nicIpConfigurationResource.copyFrom(nicIpConfiguration);

            getNicIpConfiguration().add(nicIpConfigurationResource);
        }
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        NetworkInterface networkInterface = getNetworkInterface(client);

        if (networkInterface == null) {
            return false;
        }

        copyFrom(networkInterface);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        NetworkInterface.DefinitionStages.WithPrimaryPrivateIP withPrimaryPrivateIP = client.networkInterfaces()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withExistingPrimaryNetwork(client.networks().getById(getNetwork().getId()))
            .withSubnet(getSubnet());

        NetworkInterface.DefinitionStages.WithCreate withCreate;

        NicIpConfigurationResource primary = getNicIpConfiguration().stream()
            .filter(NicIpConfigurationResource::isPrimary)
            .findFirst()
            .get();

        if (!ObjectUtils.isBlank(primary.getPrivateIpAddress())) {
            withCreate = withPrimaryPrivateIP.withPrimaryPrivateIPAddressStatic(primary.getPrivateIpAddress());
        } else {
            withCreate = withPrimaryPrivateIP.withPrimaryPrivateIPAddressDynamic();
        }

        if (getSecurityGroup() != null) {
            withCreate = withCreate.withExistingNetworkSecurityGroup(client.networkSecurityGroups()
                .getById(getSecurityGroup().getId()));
        }

        for (NicBackend backend : primary.getNicBackend()) {
            withCreate.withExistingLoadBalancerBackend(client.loadBalancers()
                .getById(backend.getLoadBalancer().getId()), backend.getBackendName());
        }

        for (NicNatRule rule : primary.getNicNatRule()) {
            withCreate.withExistingLoadBalancerInboundNatRule(client.loadBalancers()
                .getById(rule.getLoadBalancer().getId()), rule.getInboundNatRuleName());
        }

        if (getIpForwarding()) {
            withCreate.withIPForwarding();
        }

        NetworkInterface networkInterface = withCreate.withTags(getTags()).create();

        copyFrom(networkInterface);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createClient();

        NetworkInterface networkInterface = getNetworkInterface(client);

        NetworkInterface.Update update = networkInterface.update();

        if (changedFieldNames.contains("security-group")) {
            if (getSecurityGroup() == null) {
                update = update.withoutNetworkSecurityGroup();
            } else {
                update = update.withExistingNetworkSecurityGroup(client.networkSecurityGroups()
                    .getById(getSecurityGroup().getId()));
            }
        }

        if (changedFieldNames.contains("ip-forwarding")) {
            if (getIpForwarding()) {
                update.withIPForwarding();
            } else {
                update.withoutIPForwarding();
            }
        }

        NetworkInterface response = update.withTags(getTags()).apply();

        copyFrom(response);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        client.networkInterfaces().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }

    NetworkInterface getNetworkInterface(AzureResourceManager client) {
        return client.networkInterfaces().getById(getId());
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getNicIpConfiguration().stream().filter(NicIpConfigurationResource::isPrimary).count() != 1) {
            errors.add(new ValidationError(
                this,
                "nic-ip-configuration",
                "One and only one Ip configuration named as primary is required."));
        }

        return errors;
    }
}
