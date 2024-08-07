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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.HyperVGenerationTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a virtual machine image
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::virtual-machine-image virtual-machine-image-example
 *         name: "virtual-machine-image-example"
 *         resource-group: $(azure::resource-group resource-group-example-VM)
 *         virtual-machine: $(azure::virtual-machine virtual-machine-example)
 *     end
 */
@Type("virtual-machine-image")
public class VirtualMachineImageResource extends AzureResource implements Copyable<VirtualMachineCustomImage> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private VirtualMachineResource virtualMachine;
    private String hyperVGeneration;
    private Map<String, String> tags;
    private Boolean enableZoneResilience;
    private String id;

    /**
     * The name of the virtual machine image.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the virtual machine image would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The virtual machine from which the image would be generated. The virtual machine needs to be in the *Generalized* state.
     */
    @Required
    public VirtualMachineResource getVirtualMachine() {
        return virtualMachine;
    }

    public void setVirtualMachine(VirtualMachineResource virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    /**
     * The Hyper V Generation for the virtual machine image. Defaults to ``V1``.
     */
    @ValidStrings({ "V1", "V2" })
    public String getHyperVGeneration() {
        if (hyperVGeneration == null) {
            hyperVGeneration = "V1";
        }

        return hyperVGeneration;
    }

    public void setHyperVGeneration(String hyperVGeneration) {
        this.hyperVGeneration = hyperVGeneration;
    }

    /**
     * Tags for the virtual machine image.
     */
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
     * Enable or Disable zone resilience of the virtual machine image.
     */
    public Boolean getEnableZoneResilience() {
        if (enableZoneResilience == null) {
            enableZoneResilience = false;
        }

        return enableZoneResilience;
    }

    public void setEnableZoneResilience(Boolean enableZoneResilience) {
        this.enableZoneResilience = enableZoneResilience;
    }

    /**
     * The ID of the virtual machine image.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(VirtualMachineCustomImage image) {
        setHyperVGeneration(image.hyperVGeneration().toString());
        setVirtualMachine(image.isCreatedFromVirtualMachine() ? findById(
            VirtualMachineResource.class,
            image.sourceVirtualMachineId()) : null);
        setId(image.id());
        setName(image.name());
        setResourceGroup(findById(ResourceGroupResource.class, image.resourceGroupName()));
        setTags(image.tags());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        VirtualMachineCustomImage image = client.virtualMachineCustomImages().getById(getId());

        if (image == null) {
            return false;
        }

        copyFrom(image);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        VirtualMachineCustomImage.DefinitionStages.WithCreate withCreate = client.virtualMachineCustomImages()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withHyperVGeneration(HyperVGenerationTypes.fromString(getHyperVGeneration()))
            .fromVirtualMachine(getVirtualMachine().getId());

        if (getTags().isEmpty()) {
            withCreate = withCreate.withTags(getTags());
        }

        if (getEnableZoneResilience()) {
            withCreate = withCreate.withZoneResilient();
        }

        VirtualMachineCustomImage image = withCreate.create();

        copyFrom(image);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.virtualMachineCustomImages().deleteById(getId());
    }
}
