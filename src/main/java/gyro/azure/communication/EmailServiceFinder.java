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
import gyro.core.Type;

/**
 * Query email service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    service: $(external-query azure::email-service {id: "/subscriptions/26c9ce65-e0ea-42e8-9e5e-22d5ccd58343/resourceGroups/resource-group-example-test/providers/Microsoft.Communication/emailServices/example-email-test"})
 */
@Type("email-service")
public class EmailServiceFinder extends
    AzureFinder<CommunicationManager, com.azure.resourcemanager.communication.models.EmailServiceResource, EmailServiceResource> {

    private String id;
    private String resourceGroup;
    private String name;

    /**
     * The Id of the service
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
     * The name of the service
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<com.azure.resourcemanager.communication.models.EmailServiceResource> findAllAzure(
        CommunicationManager client) {
        return client.emailServices().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<com.azure.resourcemanager.communication.models.EmailServiceResource> findAzure(
        CommunicationManager client, Map<String, String> filters) {
        if (filters.containsKey("id")) {
            return Collections.singletonList(client.emailServices().getById(filters.get("id")));
        }

        if (filters.containsKey("resource-group")) {
            if (filters.containsKey("name")) {
                return Collections.singletonList(client.emailServices().getByResourceGroup(
                    filters.get("resource-group"), filters.get("name")));
            }

            return client.emailServices().listByResourceGroup(filters.get("resource-group")).stream()
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
