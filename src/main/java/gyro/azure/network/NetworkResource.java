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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.Subnet;
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

/**
 * Creates a virtual network.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::network network-example
 *          name: "network-example"
 *          resource-group: $(azure::resource-group resource-group-network-example)
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
 *          end
 *
 *          tags: {
 *              Name: "resource-group-network-example"
 *          }
 *     end
 */
@Type("network")
public class NetworkResource extends AzureResource implements Copyable<Network> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private Set<String> addressSpaces;
    private Set<SubnetResource> subnet;
    private Map<String, String> tags;
    private String id;

    private Boolean isVmProtectionEnabled;
    private Boolean isDdosProtectionEnabled;
    private String ddosProtectionPlanId;
    private Set<String> dnsServerIPs;

    /**
     * Name of the Network.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group under which the Network would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Address spaces for the Network.
     */
    @Required
    @Updatable
    public Set<String> getAddressSpaces() {
        if (addressSpaces == null) {
            addressSpaces = new HashSet<>();
        }

        return addressSpaces;
    }

    public void setAddressSpaces(Set<String> addressSpaces) {
        this.addressSpaces = addressSpaces;
    }

    /**
     * Subnets for the Network.
     *
     * @subresource gyro.azure.network.SubnetResource
     */
    @Required
    public Set<SubnetResource> getSubnet() {
        if (subnet == null) {
            subnet = new HashSet<>();
        }

        return subnet;
    }

    public void setSubnet(Set<SubnetResource> subnet) {
        this.subnet = subnet;
    }

    /**
     * The tags for the Network.
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
     * The ID of the Network.
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
     * Is VM protection enabled for the Network.
     */
    @Output
    public Boolean getVmProtectionEnabled() {
        return isVmProtectionEnabled;
    }

    public void setVmProtectionEnabled(Boolean vmProtectionEnabled) {
        isVmProtectionEnabled = vmProtectionEnabled;
    }

    /**
     * Is DDos protection enabled for the Network.
     */
    @Output
    public Boolean getDdosProtectionEnabled() {
        return isDdosProtectionEnabled;
    }

    public void setDdosProtectionEnabled(Boolean ddosProtectionEnabled) {
        isDdosProtectionEnabled = ddosProtectionEnabled;
    }

    /**
     * The Ddos protection ID if present for the Network.
     */
    @Output
    public String getDdosProtectionPlanId() {
        return ddosProtectionPlanId;
    }

    public void setDdosProtectionPlanId(String ddosProtectionPlanId) {
        this.ddosProtectionPlanId = ddosProtectionPlanId;
    }

    /**
     * The DNS Server IPs for the Network.
     */
    @Output
    public Set<String> getDnsServerIPs() {
        return dnsServerIPs;
    }

    public void setDnsServerIPs(Set<String> dnsServerIPs) {
        this.dnsServerIPs = dnsServerIPs;
    }

    @Override
    public void copyFrom(Network network) {
        setId(network.id());
        setTags(network.tags());
        setAddressSpaces(new HashSet<>(network.addressSpaces()));
        setName(network.name());
        setResourceGroup(findById(ResourceGroupResource.class, network.resourceGroupName()));
        setVmProtectionEnabled(network.isVmProtectionEnabled());
        setDdosProtectionEnabled(network.isDdosProtectionEnabled());
        setDdosProtectionPlanId(network.ddosProtectionPlanId());
        setDnsServerIPs(new HashSet<>(network.dnsServerIPs()));

        getSubnet().clear();
        if (!network.subnets().isEmpty()) {
            for (Subnet subnet : network.subnets().values()) {
                SubnetResource subnetResource = newSubresource(SubnetResource.class);
                subnetResource.copyFrom(subnet);
                getSubnet().add(subnetResource);
            }
        }
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Network network = client.networks().getById(getId());

        if (network == null) {
            return false;
        }

        copyFrom(network);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Network.DefinitionStages.WithCreate networkDefWithoutAddress = client.networks()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        Network.DefinitionStages.WithCreateAndSubnet withAddressSpace = null;

        for (String addressSpace : getAddressSpaces()) {
            withAddressSpace = networkDefWithoutAddress.withAddressSpace(addressSpace);
        }

        withAddressSpace = withAddressSpace.withSubnets(getSubnet().stream()
            .collect(Collectors.toMap(SubnetResource::getName, SubnetResource::getAddressPrefix)));

        Network network = withAddressSpace
            .withTags(getTags())
            .create();

        copyFrom(network);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Network network = client.networks().getById(getId());

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
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.networks().deleteById(getId());
    }

    Network getNetwork(AzureResourceManager client) {
        return client.networks().getById(getId());
    }
}
