package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates an application security group
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::application-security-group application-security-group-example
 *         name: "application-security-group-example"
 *         resource-group-name: $(azure::resource-group resource-group-app-security-group-example | resource-group-name)
 *         tags: {
 *                Name: "application-security-group-example"
 *         }
 *     end
 */
@Type("application-security-group")
public class ApplicationSecurityGroupResource extends AzureResource {

    private String id;
    private String name;
    private String resourceGroupName;
    private Map<String, String> tags;

    /**
     * The id of the application security group.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the application security group. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the resource group where the the application security group is found. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The tags associated with the application security group. (Optional)
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


    @Override
    public boolean refresh() {
        Azure client = createClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().getById(getId());

        if (applicationSecurityGroup == null) {
            return false;
        }

        setId(applicationSecurityGroup.id());
        setName(applicationSecurityGroup.name());
        setResourceGroupName(applicationSecurityGroup.resourceGroupName());
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName())
                .withTags(getTags())
                .create();

        setId(applicationSecurityGroup.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        client.applicationSecurityGroups().getById(getId()).update().withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.applicationSecurityGroups().deleteById(getId());
    }

}
