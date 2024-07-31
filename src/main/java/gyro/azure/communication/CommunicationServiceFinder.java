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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.communication.CommunicationManager;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query communication service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    service: $(external-query azure::communication-service {})
 */
@Type("communication-service")
public class CommunicationServiceFinder extends
    AzureFinder<CommunicationManager, com.azure.resourcemanager.communication.models.CommunicationServiceResource, CommunicationServiceResource> {

    private String resourceGroup;
    private String name;
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
     * The communication service
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the service
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<com.azure.resourcemanager.communication.models.CommunicationServiceResource> findAllAzure(
        CommunicationManager client) {
        return client.communicationServices().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<com.azure.resourcemanager.communication.models.CommunicationServiceResource> findAzure(
        CommunicationManager client, Map<String, String> filters) {


        if (filters.containsKey("id")) {
            return Collections.singletonList(client.communicationServices().getById(filters.get("id")));
        }

        if (filters.containsKey("resource-group")) {
            if (filters.containsKey("name")) {
                return Collections.singletonList(client.communicationServices()
                    .getByResourceGroup(filters.get("resource-group"), filters.get("name")));
            }

            return client.communicationServices().listByResourceGroup(filters.get("resource-group")).stream()
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
