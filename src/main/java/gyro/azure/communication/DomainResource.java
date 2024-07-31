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
import com.azure.resourcemanager.communication.fluent.models.DomainResourceInner;
import com.azure.resourcemanager.communication.models.DomainManagement;
import com.azure.resourcemanager.communication.models.DomainPropertiesVerificationRecords;
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

@Type("domain")
public class DomainResource extends AzureResource
    implements Copyable<com.azure.resourcemanager.communication.models.DomainResource> {

    private ResourceGroupResource resourceGroup;
    private EmailServiceResource emailService;
    private String domainManagement;
    private String name;
    private Map<String, String> tags;

    // Read-only
    private String dataLocation;
    private String id;

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
     * The email service where the domain is
     */
    @Required
    public EmailServiceResource getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailServiceResource emailService) {
        this.emailService = emailService;
    }

    /**
     * The domain name
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The domain management type. Defaults to ``CUSTOMER_MANAGED``.
     */
    @ValidStrings({"AzureManaged", "CustomerManaged", "CustomerManagedInExchangeOnline"})
    public String getDomainManagement() {
        if (domainManagement == null) {
            domainManagement = DomainManagement.CUSTOMER_MANAGED.toString();
        }

        return domainManagement;
    }

    public void setDomainManagement(String domainManagement) {
        this.domainManagement = domainManagement;
    }

    /**
     * The tags associated with this resource
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
     * The location where the email service stores its data at rest
     */
    @Output
    public String getDataLocation() {
        return dataLocation;
    }

    public void setDataLocation(String dataLocation) {
        this.dataLocation = dataLocation;
    }

    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(com.azure.resourcemanager.communication.models.DomainResource model) {
        setDomainManagement(model.domainManagement().toString());
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

        com.azure.resourcemanager.communication.models.DomainResource domain = client.domains().getById(getId());

        if (domain == null) {
            return false;
        }

        copyFrom(domain);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CommunicationManager client = createCommunicationClient();

        DomainResourceInner service = new DomainResourceInner();
        service.withLocation("global");

        if (!getTags().isEmpty()) {
            service.withTags(getTags());
        }

        if (getDomainManagement() != null) {
            service.withDomainManagement(DomainManagement.fromString(getDomainManagement()));
        }

        setId(client.serviceClient().getDomains()
            .createOrUpdate(getResourceGroup().getName(), getEmailService().getName(), getName(), service).id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CommunicationManager client = createCommunicationClient();

        DomainResourceInner service = client.serviceClient().getDomains()
            .get(getResourceGroup().getName(), getEmailService().getName(), getName());

        service.withTags(getTags());

        client.serviceClient().getDomains()
            .createOrUpdate(getResourceGroup().getName(), getEmailService().getName(), getName(), service);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CommunicationManager client = createCommunicationClient();

        client.serviceClient().getDomains()
            .delete(getResourceGroup().getName(), getEmailService().getName(), getName());
    }
}
