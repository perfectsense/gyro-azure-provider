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

import com.microsoft.azure.management.compute.ProximityPlacementGroup;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.Set;
import java.util.stream.Collectors;

public class ProximityPlacementGroupResource extends Diffable implements Copyable<ProximityPlacementGroup> {
    private String name;
    private String type;
    private String id;
    private String location;
    private ResourceGroupResource resourceGroup;
    private Set<AvailabilitySetResource> availabilitySets;

    /**
     * The name of the Proximity Placement Group. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the Proximity Placement Group. Valid Values ``STANDARD`` or ``ULTRA``. Defaults to ``STANDARD``.
     */
    @ValidStrings({"STANDARD", "ULTRA"})
    public String getType() {
        if (type != null) {
            type = type.toUpperCase();
        }

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The ID of the Proximity Placement Group.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The Location of the Proximity Placement Group.
     */
    @Output
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * The Resource Group of the Proximity Placement Group.
     */
    @Output
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * A set of associated availability set.
     */
    @Output
    public Set<AvailabilitySetResource> getAvailabilitySets() {
        return availabilitySets;
    }

    public void setAvailabilitySets(Set<AvailabilitySetResource> availabilitySets) {
        this.availabilitySets = availabilitySets;
    }

    @Override
    public void copyFrom(ProximityPlacementGroup proximityPlacementGroup) {
        setAvailabilitySets(proximityPlacementGroup.availabilitySetIds() != null ? proximityPlacementGroup.availabilitySetIds().stream().map(o -> findById(AvailabilitySetResource.class, o)).collect(Collectors.toSet()) : null);
        setId(proximityPlacementGroup.id());
        setLocation(proximityPlacementGroup.location());
        setResourceGroup(findById(ResourceGroupResource.class, proximityPlacementGroup.resourceGroupName()));
        setType(proximityPlacementGroup.proximityPlacementGroupType().toString());
        setName(proximityPlacementGroup.inner().name());
    }

    @Override
    public String primaryKey() {
        return getName();
    }
}
