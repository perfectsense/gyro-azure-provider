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

package gyro.azure.registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerregistry.models.Registry;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query registry.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    registry: $(external-query azure::registry {})
 */
@Type("registry")
public class RegistryFinder extends AzureFinder<AzureResourceManager, Registry, RegistryResource> {

    private String id;

    /**
     * The id of the registry.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Registry> findAllAzure(AzureResourceManager client) {
        return client.containerRegistries().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<Registry> findAzure(
        AzureResourceManager client, Map<String, String> filters) {

        List<Registry> registries = new ArrayList<>();

        Registry registry = client.containerRegistries().getById(filters.get("id"));

        if (registry != null) {
            registries.add(registry);
        }

        return registries;
    }
}
