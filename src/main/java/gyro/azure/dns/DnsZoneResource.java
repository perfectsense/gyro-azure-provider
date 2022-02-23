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

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.dns.models.DnsZone;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 *         resource-group: $(azure::resource-group resource-group-dns-zone-example)
 *         tags: {
 *            Name: "resource-group-dns-zone-example"
 *         }
 *     end
 */
@Type("dns-zone")
public class DnsZoneResource extends AzureResource implements Copyable<DnsZone> {

    private String id;
    private String name;
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
     * The name of the Dns Zone.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group where the Dns Zone is found.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The tags associated with the Dns Zone.
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
        setName(dnsZone.name());
        setResourceGroup(findById(ResourceGroupResource.class, dnsZone.resourceGroupName()));
        setTags(dnsZone.tags());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        DnsZone dnsZone = client.dnsZones().getById(getId());

        if (dnsZone == null) {
            return false;
        }

        copyFrom(dnsZone);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        DnsZone.DefinitionStages.WithCreate withCreate;

        withCreate = client.dnsZones()
            .define(getName())
            .withExistingResourceGroup(getResourceGroup().getName());

        withCreate.withTags(getTags());

        DnsZone dnsZone = withCreate.create();

        copyFrom(dnsZone);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createResourceManagerClient();

        DnsZone.Update update = client.dnsZones().getById(getId()).update();

        update.withTags(getTags());

        DnsZone dnsZone = update.apply();

        copyFrom(dnsZone);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        client.dnsZones().deleteById(getId());
    }
}
