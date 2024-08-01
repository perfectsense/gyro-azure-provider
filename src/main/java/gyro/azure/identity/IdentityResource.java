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

package gyro.azure.identity;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.msi.models.Identity;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a Identity.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::identity identity-example
 *         name: "identity-example"
 *         resource-group: $(azure::resource-group resource-group-example-identity)
 *
 *         tags: {
 *             Name: "identity-example"
 *         }
 *     end
 */
@Type("identity")
public class IdentityResource extends AzureResource implements Copyable<Identity> {
    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;
    private String id;
    private String clientId;
    private String principalId;
    private String tenantId;

    /**
     * The name of the identity.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the identity would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Tags for the identity.
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
     * The ID of the identity.
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
     * The client ID of the identity.
     */
    @Output
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * The principal ID of the identity.
     */
    @Output
    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    /**
     * The tenant ID of the identity.
     */
    @Output
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void copyFrom(Identity identity) {
        setClientId(identity.clientId());
        setPrincipalId(identity.principalId());
        setTenantId(identity.tenantId());
        setId(identity.id());
        setName(identity.name());
        setResourceGroup(findById(ResourceGroupResource.class, identity.resourceGroupName()));
        setTags(identity.tags());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Identity identity = client.identities().getById(getId());

        if (identity == null) {
            return false;
        }

        copyFrom(identity);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Identity identity = client.identities().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withTags(getTags())
            .create();

        copyFrom(identity);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Identity.Update update = client.identities().getById(getId()).update();
        IdentityResource oldResource = (IdentityResource) current;

        for (String key : oldResource.getTags().keySet()) {
            update = update.withoutTag(key);
        }

        if (!getTags().isEmpty()) {
            update = update.withTags(getTags());
        }

        Identity identity = update.apply();

        copyFrom(identity);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.identities().deleteById(getId());
    }
}
