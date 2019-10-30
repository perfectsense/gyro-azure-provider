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

package gyro.azure.resources;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query resource group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    resource-group: $(external-query azure::resource-group {})
 */
@Type("resource-group")
public class ResourceGroupFinder extends AzureFinder<ResourceGroup, ResourceGroupResource> {
    private String name;

    /**
     * The name of the Resource Group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<ResourceGroup> findAllAzure(Azure client) {
        return client.resourceGroups().list();
    }

    @Override
    protected List<ResourceGroup> findAzure(Azure client, Map<String, String> filters) {
        if (client.resourceGroups().contain(filters.get("name"))) {
            return Collections.singletonList(client.resourceGroups().getByName(filters.get("name")));
        } else {
            return Collections.emptyList();
        }
    }
}
