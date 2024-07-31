/*
 * Copyright 2024, Perfect Sense, Inc.
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

package gyro.azure.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.azure.resourcemanager.communication.CommunicationManager;
import com.azure.resourcemanager.communication.fluent.models.EmailServiceResourceInner;
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

@Type("email-service")
public class EmailServiceResource extends AzureResource
    implements Copyable<com.azure.resourcemanager.communication.models.EmailServiceResource> {

    private ResourceGroupResource resourceGroup;
    private String name;
    private String dataLocation;
    private Map<String, String> tags;

    // Read-Only
    private String id;

    /**
     * The resource group in which to build the client
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The name of the email client
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The location where the email service stores its data at rest
     */
    @Required
    public String getDataLocation() {
        return dataLocation;
    }

    public void setDataLocation(String dataLocation) {
        this.dataLocation = dataLocation;
    }

    /**
     * The tags associated to the email service
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
     * The ID of the email service
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(com.azure.resourcemanager.communication.models.EmailServiceResource model) {
        setId(model.id());
        setName(model.name());
        setDataLocation(model.dataLocation());

        getTags().clear();
        if (model.tags() != null) {
            getTags().putAll(model.tags());
        }
    }

    @Override
    public boolean refresh() {
        CommunicationManager client = createCommunicationClient();

        com.azure.resourcemanager.communication.models.EmailServiceResource service = client.emailServices()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        if (service == null) {
            return false;
        }

        copyFrom(service);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CommunicationManager client = createCommunicationClient();

        EmailServiceResourceInner service = new EmailServiceResourceInner();
        service.withLocation("global");

        if (getDataLocation() != null) {
            service.withDataLocation(getDataLocation());
        }

        if (!getTags().isEmpty()) {
            service.withTags(getTags());
        }

        client.serviceClient().getEmailServices().createOrUpdate(getResourceGroup().getName(), getName(), service);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CommunicationManager client = createCommunicationClient();

        EmailServiceResourceInner service = client.serviceClient().getEmailServices()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        service.withTags(getTags());

        client.serviceClient().getEmailServices().createOrUpdate(getResourceGroup().getName(), getName(), service);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CommunicationManager client = createCommunicationClient();

        client.serviceClient().getEmailServices().delete(getResourceGroup().getName(), getName());
    }
}
