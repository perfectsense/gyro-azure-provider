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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.resourcemanager.communication.CommunicationManager;
import com.azure.resourcemanager.communication.fluent.models.CommunicationServiceResourceInner;
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
 * Creates a communication service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         azure::communication-service service-example
 *             resource-group: $(azure::resource-group resource-group-example)
 *             name: "service-example-test"
 *             data-location: "United States"
 *             domains: [
 *                 $(azure::domain domain-example)
 *             ]
 *
 *             identity
 *                 user-assigned-identity: [$(azure::identity identity-example)]
 *             end
 *
 *             tags: {
 *                 Name: "service-example-test"
 *             }
 *         end
 */
@Type("communication-service")
public class CommunicationServiceResource extends AzureResource
    implements Copyable<com.azure.resourcemanager.communication.models.CommunicationServiceResource> {

    private ResourceGroupResource resourceGroup;
    private String name;
    private CommunicationServiceManagedServiceIdentity identity;
    private String dataLocation;
    private List<DomainResource> domains;
    private Map<String, String> tags;

    // Read-only
    private String id;
    private String hostName;

    /**
     * The resource group in which to build the service
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The name of the service
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The managed service identity for the communication service
     */
    public CommunicationServiceManagedServiceIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(CommunicationServiceManagedServiceIdentity identity) {
        this.identity = identity;
    }

    /**
     * The tags for the service
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
     * The location where the service stores its data at rest
     */
    @Required
    public String getDataLocation() {
        return dataLocation;
    }

    public void setDataLocation(String dataLocation) {
        this.dataLocation = dataLocation;
    }

    /**
     * List of email Domain resources. These domain have to be verified for them to be connected ot the service.
     */
    @Updatable
    public List<DomainResource> getDomains() {
        if (domains == null) {
            domains = new ArrayList<>();
        }

        return domains;
    }

    public void setDomains(List<DomainResource> domains) {
        this.domains = domains;
    }

    /**
     * The FQDN of the service instance.
     */
    @Output
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * The Id of the service
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(com.azure.resourcemanager.communication.models.CommunicationServiceResource model) {
        setResourceGroup(findById(ResourceGroupResource.class, model.resourceGroupName()));
        setName(model.name());
        setDataLocation(model.dataLocation());
        setHostName(model.hostname());
        setId(model.id());

        getDomains().clear();
        if (model.linkedDomains() != null) {
            getDomains().addAll(model.linkedDomains().stream().map(r -> findById(DomainResource.class, r)).collect(
                Collectors.toList()));
        }

        getTags().clear();
        if (model.tags() != null) {
            getTags().putAll(model.tags());
        }

        setIdentity(null);
        if (model.identity() != null) {
            CommunicationServiceManagedServiceIdentity serviceIdentity =
                newSubresource(CommunicationServiceManagedServiceIdentity.class);
            serviceIdentity.copyFrom(model.identity());
            setIdentity(serviceIdentity);
        }
    }

    @Override
    public boolean refresh() {
        CommunicationManager client = createCommunicationClient();

        com.azure.resourcemanager.communication.models.CommunicationServiceResource service =
            client.communicationServices().getById(getId());

        if (service == null) {
            return false;
        }

        copyFrom(service);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CommunicationManager client = createCommunicationClient();

        CommunicationServiceResourceInner service = new CommunicationServiceResourceInner();

        if (getIdentity() != null) {
            service.withIdentity(getIdentity().toManagedServiceIdentity());
        }

        if (!getTags().isEmpty()) {
            service.withTags(getTags());
        }

        if (getDataLocation() != null) {
            service.withDataLocation(getDataLocation());
        }

        service.withLocation("global");
        setId(client.serviceClient().getCommunicationServices()
            .createOrUpdate(getResourceGroup().getName(), getName(), service).id());

        state.save();

        // Add domains in the update call
        // If domains are not verified but are added to the create call, the api errors out at the service is not saved to the state
        if (!getDomains().isEmpty()) {
            service.withLinkedDomains(getDomains().stream().map(DomainResource::getId).collect(Collectors.toList()));
        }

        client.serviceClient().getCommunicationServices()
            .createOrUpdate(getResourceGroup().getName(), getName(), service);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CommunicationManager client = createCommunicationClient();

        CommunicationServiceResourceInner service = client.serviceClient().getCommunicationServices()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        service.withTags(getTags());
        service.withLinkedDomains(getDomains().stream().map(DomainResource::getId).collect(Collectors.toList()));

        client.serviceClient().getCommunicationServices()
            .createOrUpdate(getResourceGroup().getName(), getName(), service);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CommunicationManager client = createCommunicationClient();

        client.communicationServices().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }
}
