package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.HyperVGenerationTypes;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
     * The name of the virtual machine image. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the virtual machine image would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The virtual machine from which the image would be generated. The virtual machine needs to be in the *Generalized* state. (Required)
     */
    @Required
    public VirtualMachineResource getVirtualMachine() {
        return virtualMachine;
    }

    public void setVirtualMachine(VirtualMachineResource virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    /**
     * The Hyper V Generation for the virtual machine image. Valid values are ``V1`` or ``V2``. Defaults to ``V1``.
     */
    @ValidStrings({"V1", "V2"})
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(VirtualMachineCustomImage image) {
        setHyperVGeneration(image.hyperVGeneration().toString());
        setVirtualMachine(image.isCreatedFromVirtualMachine() ? findById(VirtualMachineResource.class, image.sourceVirtualMachineId()) : null);
        setId(image.id());
        setName(image.name());
        setResourceGroup(findById(ResourceGroupResource.class, image.resourceGroupName()));
        setTags(image.tags());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        VirtualMachineCustomImage image = client.virtualMachineCustomImages().getById(getId());

        if (image == null) {
            return false;
        }

        copyFrom(image);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        VirtualMachineCustomImage.DefinitionStages.WithHyperVGeneration withHyperVGeneration = client.virtualMachineCustomImages()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

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
        Azure client = createClient();

        client.virtualMachineCustomImages().deleteById(getId());
    }
}
