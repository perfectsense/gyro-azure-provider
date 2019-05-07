package gyro.azure.compute;

import gyro.azure.AzureResource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

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
 *         azure::availability-set availability-set-example
 *             fault-domain-count: 2
 *             name: "availability-set-example"
 *             resource-group-name: $(azure::resource-group load-balancer-rg-example | resource-group-name)
 *             sku: "Aligned"
 *             tags: {
 *                   Name: "availability-set-example"
 *             }
 *             update-domain-count: 20
 *         end
 */
@ResourceType("availability-set")
public class AvailabilitySetResource extends AzureResource {

    private Integer faultDomainCount;
    private String id;
    private String name;
    private String resourceGroupName;
    private String sku;
    private Map<String, String> tags;
    private Integer updateDomainCount;

    /**
     * The fault domain count of the availability set.
     */
    public Integer getFaultDomainCount() {
        return faultDomainCount;
    }

    public void setFaultDomainCount(Integer faultDomainCount) {
        this.faultDomainCount = faultDomainCount;
    }

    /**
     * The id of the availability set.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the availability set.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The input resource group name where the availability set is found.
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The availability set sku. Options are Aligned and Classic. Defaults to Classic. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
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
     * The tags associated with the availability set. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
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
    public boolean refresh() {
        Azure client = createClient();

        AvailabilitySet availabilitySet = client.availabilitySets().getById(getId());

        if (availabilitySet == null) {
            return false;
        }

        setFaultDomainCount(availabilitySet.faultDomainCount());
        setId(availabilitySet.id());
        setName(availabilitySet.name());
        setSku(availabilitySet.sku().toString());
        setUpdateDomainCount(availabilitySet.updateDomainCount());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        AvailabilitySet availabilitySet = client.availabilitySets().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName())
                .withFaultDomainCount(getFaultDomainCount())
                .withSku(AvailabilitySetSkuTypes.fromString(getSku()))
                .withUpdateDomainCount(getUpdateDomainCount())
                .withTags(getTags())
                .create();

        setId(availabilitySet.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        client.availabilitySets().getById(getId()).update()
                .withSku(AvailabilitySetSkuTypes.fromString(getSku()))
                .withTags(getTags())
                .apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.availabilitySets().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "availability set " + getName();
    }
}
