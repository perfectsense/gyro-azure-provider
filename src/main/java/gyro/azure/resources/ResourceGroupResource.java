package gyro.azure.resources;

import gyro.azure.AzureResource;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a resource group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::resource-group resource-group-example
 *         resource-group-name: "resource-group-example"
 *
 *         tags: {
 *             Name: "resource-group-example"
 *         }
 *     end
 */
@Type("resource-group")
public class ResourceGroupResource extends AzureResource {

    private String resourceGroupName;
    private String resourceGroupId;

    private Map<String, String> tags;

    /**
     * The name of the resource group. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getResourceGroupId() {
        return resourceGroupId;
    }

    public void setResourceGroupId(String resourceGroupId) {
        this.resourceGroupId = resourceGroupId;
    }

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

    @Override
    public boolean refresh() {
        Azure client = createClient();

        if (!client.resourceGroups().contain(getResourceGroupName())) {
            return false;
        }

        ResourceGroup resourceGroup = client.resourceGroups().getByName(getResourceGroupName());
        setResourceGroupId(resourceGroup.id());
        setTags(resourceGroup.tags());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        ResourceGroup resourceGroup = client.resourceGroups()
            .define(getResourceGroupName())
            .withRegion(Region.fromName(getRegion()))
            .withTags(getTags())
            .create();

        setResourceGroupId(resourceGroup.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        ResourceGroup resourceGroup = client.resourceGroups().getByName(getResourceGroupName());

        resourceGroup.update().withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.resourceGroups().deleteByName(getResourceGroupName());
    }

}
