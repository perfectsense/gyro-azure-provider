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

package gyro.azure.cdn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cdn.models.CdnProfile;
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
import gyro.core.validation.ValidStrings;

/**
 * Creates a cdn profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         azure::cdn-profile cdn-profile-example
 *             name: "cdn-profile-example"
 *             resource-group: $(azure::resource-group resource-group-cdn-profile-example)
 *             sku: "Standard_Akamai"
 *             tags: {
 *                 Name: "cdn-profile-example"
 *             }
 *         end
 */
@Type("cdn-profile")
public class CdnProfileResource extends AzureResource implements Copyable<CdnProfile> {

    private String id;
    private String name;
    private ResourceGroupResource resourceGroup;
    private String sku;
    private Map<String, String> tags;
    private Set<CdnEndpointResource> endpoint;

    /**
     * The ID of the CDN Profile.
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
     * The name of the CDN Profile.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group where the CDN Profile is found.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The sku of the CDN Profile.
     */
    @Required
    @ValidStrings({ "Premium_Verizon", "Standard_Verizon", "Standard_Akamai" })
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * The tags associated with the CDN Profile.
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
     * A set of endpoints for the CDN Profile.
     *
     * @subresource gyro.azure.cdn.CdnEndpointResource
     */
    @Updatable
    public Set<CdnEndpointResource> getEndpoint() {
        if (endpoint == null) {
            endpoint = new HashSet<>();
        }

        return endpoint;
    }

    public void setEndpoint(Set<CdnEndpointResource> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void copyFrom(CdnProfile cdnProfile) {
        setId(cdnProfile.id());
        setName(cdnProfile.name());
        setResourceGroup(findById(ResourceGroupResource.class, cdnProfile.resourceGroupName()));
        setSku(cdnProfile.sku().name().toString());
        setTags(cdnProfile.tags());
        setEndpoint(cdnProfile.endpoints().values().stream().map(o -> {
            CdnEndpointResource endpointResource = newSubresource(CdnEndpointResource.class);
            endpointResource.copyFrom(o);
            return endpointResource;
        }).collect(Collectors.toSet()));
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        CdnProfile cdnProfile = client.cdnProfiles().getById(getId());

        if (cdnProfile == null) {
            return false;
        }

        copyFrom(cdnProfile);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        CdnProfile.DefinitionStages.WithSku withSku = client.cdnProfiles().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        CdnProfile cdnProfile = null;
        if ("Premium_Verizon".equalsIgnoreCase(getSku())) {
            cdnProfile = withSku.withPremiumVerizonSku().withTags(getTags()).create();
        } else if ("Standard_Verizon".equalsIgnoreCase(getSku())) {
            cdnProfile = withSku.withStandardVerizonSku().withTags(getTags()).create();
        } else if ("Standard_Akamai".equalsIgnoreCase(getSku())) {
            cdnProfile = withSku.withStandardAkamaiSku().withTags(getTags()).create();
        }

        copyFrom(cdnProfile);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createResourceManagerClient();

        CdnProfile.Update update = client.cdnProfiles().getById(getId()).update().withTags(getTags());
        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        client.cdnProfiles().deleteById(getId());
    }
}
