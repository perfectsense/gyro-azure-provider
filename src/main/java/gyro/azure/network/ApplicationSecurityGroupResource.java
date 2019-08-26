package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
 *    azure::application-security-group application-security-group-example
 *        name: "application-security-group-example"
 *        resource-group: $(azure::resource-group resource-group-app-security-group-example)
 *        tags: {
 *            Name: "application-security-group-example"
 *        }
 *    end
 */
@Type("application-security-group")
public class ApplicationSecurityGroupResource extends AzureResource implements Copyable<ApplicationSecurityGroup> {
    private String id;
    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;

    private String provisioningState;
    private String resourceGuid;
    private String etag;
    private String type;

    /**
     * The ID of the Application Security Group.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Application Security Group. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group where the the Application Security Group is found. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The tags associated with the Application Security Group. (Optional)
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
     * The provisioning state of the Application Security Group.
     */
    @Output
    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * A shortened ID of the Application Security Group.
     */
    @Output
    public String getResourceGuid() {
        return resourceGuid;
    }

    public void setResourceGuid(String resourceGuid) {
        this.resourceGuid = resourceGuid;
    }

    /**
     * The etag value of the Application Security Group.
     */
    @Output
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * The resource type of the Application Security Group.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ApplicationSecurityGroup applicationSecurityGroup) {
        setId(applicationSecurityGroup.id());
        setName(applicationSecurityGroup.name());
        setResourceGroup(findById(ResourceGroupResource.class, applicationSecurityGroup.resourceGroupName()));
        setTags(applicationSecurityGroup.tags());

        setProvisioningState(applicationSecurityGroup.provisioningState());
        setResourceGuid(applicationSecurityGroup.resourceGuid());
        setEtag(applicationSecurityGroup.inner().etag());
        setType(applicationSecurityGroup.type());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().getById(getId());

        if (applicationSecurityGroup == null) {
            return false;
        }

        copyFrom(applicationSecurityGroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withTags(getTags())
            .create();

        copyFrom(applicationSecurityGroup);
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
