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

package gyro.azure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import gyro.core.finder.Finder;

public abstract class AzureResourceManagerFinder<M, R extends AzureResource> extends Finder<R> {
    protected abstract List<M> findAllAzure(AzureResourceManager client);

    protected abstract List<M> findAzure(AzureResourceManager client, Map<String, String> filters);

    @Override
    public List<R> find(Map<String, Object> filters) {
        return findAzure(newClient(), convertFilters(filters)).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    @Override
    public List<R> findAll() {
        return findAllAzure(newClient()).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    private AzureResourceManager newClient() {
        return AzureResource.createResourceManagerClient(credentials(AzureCredentials.class));
    }

    @SuppressWarnings("unchecked")
    private R newResource(M model) {
        R resource = newResource();

        if (resource instanceof Copyable) {
            ((Copyable<M>) resource).copyFrom(model);
        }

        return resource;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertFilters(Map<String, Object> query) {
        Map<String, String> filters = new HashMap<>();

        for (Map.Entry<String, Object> e : query.entrySet()) {
            filters.put(e.getKey(), e.getValue().toString());
        }

        return filters;
    }
}
