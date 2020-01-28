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

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates an availability set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::availability-set availability-set-example
 *        fault-domain-count: 2
 *        name: "availability-set-example"
 *        resource-group: $(azure::resource-group load-balancer-rg-example)
 *        sku: "Aligned"
 *        tags: {
 *              Name: "availability-set-example"
 *        }
 *        update-domain-count: 20
 *    end
 */
@Type("availability-set")
public class AvailabilitySetResource extends AzureResource implements Copyable<AvailabilitySet> {

    private Integer faultDomainCount;
    private String id;
    private String name;
    private ResourceGroupResource resourceGroup;
    private String sku;
    private Map<String, String> tags;
    private Integer updateDomainCount;

    /**
     * The fault domain count of the Availability Set.
     */
    @Required
    public Integer getFaultDomainCount() {
        return faultDomainCount;
    }

    public void setFaultDomainCount(Integer faultDomainCount) {
        this.faultDomainCount = faultDomainCount;
    }

    /**
     * The ID of the Availability Set.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Availability Set. (Required)
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group under which the Availability Set would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The Availability Set sku. Valid values are ``Aligned`` or ``Classic``. Defaults to ``Classic``. (Optional)
     */
    @ValidStrings({"Aligned", "Classic"})
    @Updatable
    public String getSku() {
        if (sku == null) {
            sku = "Classic";
        }

         return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * The tags associated with the Availability Set. (Optional)
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The update domain count of the availability set. (Optional)
     */
    public Integer getUpdateDomainCount() {
        return updateDomainCount;
    }

    public void setUpdateDomainCount(Integer updateDomainCount) {
        this.updateDomainCount = updateDomainCount;
    }

    @Override
    public void copyFrom(AvailabilitySet availabilitySet) {
        setFaultDomainCount(availabilitySet.faultDomainCount());
        setId(availabilitySet.id());
        setName(availabilitySet.name());
        setSku(availabilitySet.sku().toString());
        setUpdateDomainCount(availabilitySet.updateDomainCount());
        setResourceGroup(findById(ResourceGroupResource.class, availabilitySet.resourceGroupName()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        AvailabilitySet availabilitySet = client.availabilitySets().getById(getId());

        if (availabilitySet == null) {
            return false;
        }

        copyFrom(availabilitySet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        AvailabilitySet availabilitySet = client.availabilitySets().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroup().getName())
                .withFaultDomainCount(getFaultDomainCount())
                .withSku(AvailabilitySetSkuTypes.fromString(getSku()))
                .withUpdateDomainCount(getUpdateDomainCount())
                .withTags(getTags())
                .create();

        copyFrom(availabilitySet);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        if (changedFieldNames.contains("sku") && AvailabilitySetSkuTypes.fromString(getSku()).equals(AvailabilitySetSkuTypes.CLASSIC)) {
            throw new GyroException("Changing param SKU from 'Aligned' to 'Classic' is not allowed");
        }

        AvailabilitySet availabilitySet = client.availabilitySets().getById(getId()).update()
            .withSku(AvailabilitySetSkuTypes.fromString(getSku()))
            .withTags(getTags())
            .apply();

        copyFrom(availabilitySet);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.availabilitySets().deleteById(getId());
    }
}
