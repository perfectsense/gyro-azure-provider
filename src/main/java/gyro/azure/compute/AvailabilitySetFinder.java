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

package gyro.azure.compute;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import gyro.azure.AzureResourceManagerFinder;
import gyro.core.Type;

/**
 * Query availability set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    availability-set: $(external-query azure::availability-set {})
 */
@Type("availability-set")
public class AvailabilitySetFinder extends AzureResourceManagerFinder<AvailabilitySet, AvailabilitySetResource> {

    private String id;

    /**
     * The ID of the Availability Set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<AvailabilitySet> findAllAzure(AzureResourceManager client) {
        return client.availabilitySets().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<AvailabilitySet> findAzure(AzureResourceManager client, Map<String, String> filters) {
        AvailabilitySet availabilitySet = client.availabilitySets().getById(filters.get("id"));
        if (availabilitySet == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(availabilitySet);
        }
    }
}
