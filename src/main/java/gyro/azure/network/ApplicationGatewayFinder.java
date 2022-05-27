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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import gyro.azure.AzureResourceManagerFinder;
import gyro.core.Type;

/**
 * Query application gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    application-gateway: $(external-query azure::application-gateway {})
 */
@Type("application-gateway")
public class ApplicationGatewayFinder
    extends AzureResourceManagerFinder<ApplicationGateway, ApplicationGatewayResource> {

    private String id;

    /**
     * The ID of the Application Gateway.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ApplicationGateway> findAllAzure(AzureResourceManager client) {
        return client.applicationGateways().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<ApplicationGateway> findAzure(AzureResourceManager client, Map<String, String> filters) {
        ApplicationGateway applicationGateway = client.applicationGateways().getById(filters.get("id"));
        if (applicationGateway == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(applicationGateway);
        }
    }
}
