/*
 * Copyright 2022, Brightspot, Inc.
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

package gyro.azure.accessmanagement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

/**
 * Creates an identity.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::identity identity-example
 *         name: "identity-example"
 *         resource-group: $(azure::resource-group identity-example)
 *
 *         tags: {
 *             Name: "identity-example"
 *         }
 *     end
 *
 */
@Type("identity")
public class IdentityResource extends AzureResource implements Copyable<Identity> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;

    private String id;
    private String tenantId;
    private String clientId;
    private String principalId;

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
     * The resource group associated with the identity.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * A set of tags for the identity.
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
     * The id of the identity.
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
     * The associated tenant id of the identity.
     */
    @Output
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * The client id of the identity.
     */
    @Output
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * The principal id of the identity.
     */
    @Output
    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    @Override
    public void copyFrom(Identity model) {
        setName(model.name());
        setResourceGroup(findById(ResourceGroupResource.class, model.resourceGroupName()));
        setTags(model.tags());
        setId(model.id());
        setClientId(model.clientId());
        setTenantId(model.tenantId());
        setPrincipalId(model.principalId());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        Identity identity = client.identities().getById(getId());

        if (identity == null) {
            return false;
        }

        copyFrom(identity);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

        Identity.DefinitionStages.WithCreate withCreate = client.identities()
            .define(getName())
            .withRegion(getRegion())
            .withExistingResourceGroup(getResourceGroup().getName());

        if (!getTags().isEmpty()) {
            withCreate = withCreate.withTags(getTags());
        }

        Identity identity = withCreate.create();

        copyFrom(identity);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

        IdentityResource currentResource = (IdentityResource) current;

        Identity identity = client.identities().getById(getId());

        Identity.Update update = identity.update();

        if (!currentResource.getTags().isEmpty()) {
            for (String key : currentResource.getTags().keySet()) {
                update = update.withoutTag(key);
            }
        }

        if (!getTags().isEmpty()) {
            update.withTags(getTags());
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

        client.identities().deleteById(getId());
    }
}
