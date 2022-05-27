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
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
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
 * Creates an application security group
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::application-security-group application-security-group-example
 *        name: "application-security-group-example"
 *        resource-group: $(azure::resource-group resource-group-app-security-group-example)
 *        tags: {
 *            Name: "application-security-group-example"
 *        }
 *    end
 */
@Type("application-security-group")
public class ApplicationSecurityGroupResource extends AzureResource implements Copyable<ApplicationSecurityGroup> {

    private String id;
    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;

    private String provisioningState;
    private String resourceGuid;
    private String etag;
    private String type;

    /**
     * The ID of the Application Security Group.
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
     * The name of the Application Security Group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group where the the Application Security Group is found.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The tags associated with the Application Security Group.
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
     * The provisioning state of the Application Security Group.
     */
    @Output
    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * A shortened ID of the Application Security Group.
     */
    @Output
    public String getResourceGuid() {
        return resourceGuid;
    }

    public void setResourceGuid(String resourceGuid) {
        this.resourceGuid = resourceGuid;
    }

    /**
     * The etag value of the Application Security Group.
     */
    @Output
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * The resource type of the Application Security Group.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ApplicationSecurityGroup applicationSecurityGroup) {
        setId(applicationSecurityGroup.id());
        setName(applicationSecurityGroup.name());
        setResourceGroup(findById(ResourceGroupResource.class, applicationSecurityGroup.resourceGroupName()));
        setTags(applicationSecurityGroup.tags());

        setProvisioningState(applicationSecurityGroup.provisioningState());
        setResourceGuid(applicationSecurityGroup.resourceGuid());
        setEtag(applicationSecurityGroup.innerModel().etag());
        setType(applicationSecurityGroup.type());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().getById(getId());

        if (applicationSecurityGroup == null) {
            return false;
        }

        copyFrom(applicationSecurityGroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withTags(getTags())
            .create();

        copyFrom(applicationSecurityGroup);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createResourceManagerClient();

        client.applicationSecurityGroups().getById(getId()).update().withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        client.applicationSecurityGroups().deleteById(getId());
    }
}
