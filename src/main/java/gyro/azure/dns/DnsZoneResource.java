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

package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.network.NetworkResource;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.ZoneType;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a DNS Zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::dns-zone dns-zone-example-zones
 *         name: "zones.example.com"
 *         public-access: false
 *         resource-group: $(azure::resource-group resource-group-dns-zone-example)
 *         tags: {
 *            Name: "resource-group-dns-zone-example"
 *         }
 *     end
 */
@Type("dns-zone")
public class DnsZoneResource extends AzureResource implements Copyable<DnsZone> {

    private String id;
    private Boolean publicAccess;
    private String name;
    private Set<NetworkResource> registrationNetwork;
    private Set<NetworkResource> resolutionNetwork;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;

    /**
     * The ID of the Dns Zone.
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
     * Determines if the Dns Zone is public or private. Defaults to public ``true``. (Optional)
     */
    public Boolean getPublicAccess() {
        if (publicAccess == null) {
            publicAccess = true;
        }

        return publicAccess;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * The name of the Dns Zone. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of virtual network id's that register hostnames in a private Dns Zone. Can be used when the access is private. (Optional)
     */
    @Updatable
    public Set<NetworkResource> getRegistrationNetwork() {
        if (registrationNetwork == null) {
            registrationNetwork = new HashSet<>();
        }

        return registrationNetwork;
    }

    public void setRegistrationNetwork(Set<NetworkResource> registrationNetwork) {
        this.registrationNetwork = registrationNetwork;
    }

    /**
     * A list of virtual network id's that resolve records in a private Dns Zone. Can be used when the access is private. (Optional)
     */
    @Updatable
    public Set<NetworkResource> getResolutionNetwork() {
        if (resolutionNetwork == null) {
            resolutionNetwork = new HashSet<>();
        }

        return resolutionNetwork;
    }

    public void setResolutionNetwork(Set<NetworkResource> resolutionNetwork) {
        this.resolutionNetwork = resolutionNetwork;
    }

    /**
     * The Resource Group where the Dns Zone is found. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The tags associated with the Dns Zone. (Optional)
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

    @Override
    public void copyFrom(DnsZone dnsZone) {
        setId(dnsZone.id());
        setPublicAccess(dnsZone.accessType() == ZoneType.PUBLIC);
        setName(dnsZone.name());
        setRegistrationNetwork(dnsZone.registrationVirtualNetworkIds().stream().map(o -> findById(NetworkResource.class, o)).collect(Collectors.toSet()));
        setResolutionNetwork(dnsZone.resolutionVirtualNetworkIds().stream().map(o -> findById(NetworkResource.class, o)).collect(Collectors.toSet()));
        setResourceGroup(findById(ResourceGroupResource.class, dnsZone.resourceGroupName()));
        setTags(dnsZone.tags());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        DnsZone dnsZone = client.dnsZones().getById(getId());

        if (dnsZone == null) {
            return false;
        }

        copyFrom(dnsZone);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        DnsZone.DefinitionStages.WithCreate withCreate;

        withCreate = client.dnsZones().define(getName()).withExistingResourceGroup(getResourceGroup().getName());

        if (getPublicAccess() != null && !getPublicAccess()) {
            if (getRegistrationNetwork().isEmpty() && getResolutionNetwork().isEmpty()) {
                withCreate.withPrivateAccess(getRegistrationNetwork().stream().map(NetworkResource::getId).collect(Collectors.toList()),
                    getResolutionNetwork().stream().map(NetworkResource::getId).collect(Collectors.toList()));
            } else {
                withCreate.withPrivateAccess();
            }
        } else {
            withCreate.withPublicAccess();
        }

        withCreate.withTags(getTags());

        DnsZone dnsZone = withCreate.create();

        copyFrom(dnsZone);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsZone.Update update = client.dnsZones().getById(getId()).update();

        update.withTags(getTags());

        DnsZone dnsZone = update.apply();

        copyFrom(dnsZone);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().deleteById(getId());
    }
}
