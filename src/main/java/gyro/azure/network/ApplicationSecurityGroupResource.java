package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;

import com.microsoft.azure.management.Azure;
import gyro.core.resource.ResourceOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ResourceName("application-security-group")
public class ApplicationSecurityGroupResource extends AzureResource {

    private String id;
    private String name;
    private String resourceGroupName;
    private Map<String, String> tags;

    /**
     * The id of the application security group.
     */
    @ResourceOutput
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
    public void create() {
        Azure client = createClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName())
                .withTags(getTags())
                .create();

        setId(applicationSecurityGroup.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        client.applicationSecurityGroups().getById(getId()).update().withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.applicationSecurityGroups().deleteById(getId());
    }

    @Override
    public String toDisplayString() {return "application security group " + getName();}
}
