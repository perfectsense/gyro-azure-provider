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

package gyro.azure.resources;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a resource group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::resource-group resource-group-example
 *         name: "resource-group-example"
 *
 *         tags: {
 *             Name: "resource-group-example"
 *         }
 *     end
 */
@Type("resource-group")
public class ResourceGroupResource extends AzureResource implements Copyable<ResourceGroup> {
    private String name;
    private String id;

    private Map<String, String> tags;

    /**
     * The name of the resource group. (Required)
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the Resource Group.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Tags for the Resource Group.
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
    public void copyFrom(ResourceGroup resourceGroup) {
        setName(resourceGroup.name());
        setId(resourceGroup.id());
        setTags(resourceGroup.tags());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        if (!client.resourceGroups().contain(getName())) {
            return false;
        }

        ResourceGroup resourceGroup = client.resourceGroups().getByName(getName());
        copyFrom(resourceGroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        ResourceGroup resourceGroup = client.resourceGroups()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withTags(getTags())
            .create();

        setId(resourceGroup.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        ResourceGroup resourceGroup = client.resourceGroups().getByName(getName());

        resourceGroup.update().withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.resourceGroups().deleteByName(getName());
    }
}
