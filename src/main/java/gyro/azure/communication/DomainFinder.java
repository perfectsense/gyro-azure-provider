/*
 * Copyright 2024, Brightspot, Inc.
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.communication.CommunicationManager;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

/**
 * Query domains.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    domain: $(external-query azure::domain {id: "/subscriptions/26c9ce65-e0ea-42e8-9e5e-22d5ccd58343/resourceGroups/resource-group-example-test/providers/Microsoft.Communication/emailServices/example-email-test/domains/cloud.brightspot.dev"})
 */
@Type("domain")
public class DomainFinder extends
    AzureFinder<CommunicationManager, com.azure.resourcemanager.communication.models.DomainResource, DomainResource> {
    private String resourceGroup;
    private String emailService;
    private String id;

    /**
     * The resource group of the service
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The email service
     */
    public String getEmailService() {
        return emailService;
    }

    public void setEmailService(String emailService) {
        this.emailService = emailService;
    }

    /**
     * The ID of the domain
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<com.azure.resourcemanager.communication.models.DomainResource> findAllAzure(
        CommunicationManager client) {
        throw new GyroException("Cannot list All domains in subscription.");
    }

    @Override
    protected List<com.azure.resourcemanager.communication.models.DomainResource> findAzure(CommunicationManager client,
        Map<String, String> filters) {
        if (filters.containsKey("id")) {
            return Collections.singletonList(client.domains().getById(filters.get("id")));
        }

        if (filters.containsKey("resource-group") && filters.containsKey("email-service")) {
            return client.domains()
                .listByEmailServiceResource(filters.get("resource-group"), filters.get("email-service")).stream()
                .collect(Collectors.toList());
        }

        throw new GyroException("Both `resource-group` and `email-service` are required");
    }
}
