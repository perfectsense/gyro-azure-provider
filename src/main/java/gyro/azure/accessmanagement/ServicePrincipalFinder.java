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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query service principal.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    service-principal: $(external-query azure::service-principal {})
 */
@Type("service-principal")
public class ServicePrincipalFinder extends AzureFinder<ServicePrincipal, ServicePrincipalResource> {

    private String id;

    /**
     * The id of the service principal.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ServicePrincipal> findAllAzure(AzureResourceManager client) {
        return client.accessManagement()
            .servicePrincipals()
            .list()
            .stream()
            .collect(Collectors.toList());
    }

    @Override
    protected List<ServicePrincipal> findAzure(
        AzureResourceManager client, Map<String, String> filters) {

        List<ServicePrincipal> servicePrincipals = new ArrayList<>();
        ServicePrincipal servicePrincipal = client.accessManagement()
            .servicePrincipals()
            .getById(filters.get("id"));

        if (servicePrincipal != null) {
            servicePrincipals.add(servicePrincipal);
        }

        return servicePrincipals;
    }
}
