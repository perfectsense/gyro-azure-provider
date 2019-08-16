package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Creates a network security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::network-security-group network-security-group-example
 *          name: "network-security-group-example"
 *          resource-group: $(azure::resource-group resource-group-network-security-group-example)
 *
 *          rule
 *              name: "Port_8080"
 *              inbound-rule: true
 *              allow-rule: true
 *              from-addresses: [
 *                  "8080"
 *              ]
 *              from-ports: [
 *                  "*"
 *
 *              ]
 *              to-addresses: [
 *                  "8080"
 *              ]
 *              to-ports: [
 *                  "*"
 *              ]
 *              priority: 100
 *              protocol: "all"
 *          end
 *
 *          tags: {
 *              Name: "network-security-group-example"
 *          }
 *     end
 */
@Type("network-security-group")
public class NetworkSecurityGroupResource extends AzureResource implements Copyable<NetworkSecurityGroup> {
    private String name;
    private ResourceGroupResource resourceGroup;
    private String id;
    private Set<NetworkSecurityGroupRuleResource> rule;
    private Map<String, String> tags;

    /**
     * Name of the Network Security Group. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Name of the resource group under which the Network Security Group would reside. (Required)
     */
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The ID of the Network Security Group.
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
     * Inbound and Outbound rules for the Network Security Group.
     *
     * @subresource gyro.azure.network.NetworkSecurityGroupRuleResource
     */
    @Updatable
    public Set<NetworkSecurityGroupRuleResource> getRule() {
        if (rule == null) {
            rule = new HashSet<>();
        }

        return rule;
    }

    public void setRule(Set<NetworkSecurityGroupRuleResource> rule) {
        this.rule = rule;
    }

    /**
     * The associated tags for the Network Security Group.
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
    public void copyFrom(NetworkSecurityGroup networkSecurityGroup) {
        setName(networkSecurityGroup.name());
        setId(networkSecurityGroup.id());
        setResourceGroup(findById(ResourceGroupResource.class, networkSecurityGroup.resourceGroupName()));
        setTags(networkSecurityGroup.tags());

        getRule().clear();
        for (String key : networkSecurityGroup.securityRules().keySet()) {
            NetworkSecurityGroupRuleResource ruleResource = newSubresource(NetworkSecurityGroupRuleResource.class);
            ruleResource.copyFrom(networkSecurityGroup.securityRules().get(key));
            getRule().add(ruleResource);
        }
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(getId());

        if (networkSecurityGroup == null) {
            return false;
        }

        copyFrom(networkSecurityGroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withTags(getTags())
            .create();

        setId(networkSecurityGroup.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(getId());

        networkSecurityGroup.update().withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.networkSecurityGroups().deleteById(getId());
    }
}
