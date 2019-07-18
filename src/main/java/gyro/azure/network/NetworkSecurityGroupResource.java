package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.azure.AzureResource;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 *          network-security-group-name: "network-security-group-example"
 *          resource-group-name: $(azure::resource-group resource-group-network-security-group-example | resource-group-name)
 *
 *          rule
 *              security-group-rule-name: "Port_8080"
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
public class NetworkSecurityGroupResource extends AzureResource {
    private String networkSecurityGroupName;
    private String resourceGroupName;
    private String networkSecurityGroupId;
    private List<NetworkSecurityGroupRuleResource> rule;
    private Map<String, String> tags;

    /**
     * Name of the security group. (Required)
     */
    public String getNetworkSecurityGroupName() {
        return networkSecurityGroupName;
    }

    public void setNetworkSecurityGroupName(String networkSecurityGroupName) {
        this.networkSecurityGroupName = networkSecurityGroupName;
    }

    /**
     * Name of the resource group under which this would reside. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    @Output
    public String getNetworkSecurityGroupId() {
        return networkSecurityGroupId;
    }

    public void setNetworkSecurityGroupId(String networkSecurityGroupId) {
        this.networkSecurityGroupId = networkSecurityGroupId;
    }

    /**
     * Inbound and Outbound rules for the security group.
     *
     * @subresource gyro.azure.network.NetworkSecurityGroupRuleResource
     */
    @Updatable
    public List<NetworkSecurityGroupRuleResource> getRule() {
        if (rule == null) {
            rule = new ArrayList<>();
        }

        return rule;
    }

    public void setRule(List<NetworkSecurityGroupRuleResource> rule) {
        this.rule = rule;
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

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(getNetworkSecurityGroupId());

        setNetworkSecurityGroupName(networkSecurityGroup.name());
        setTags(networkSecurityGroup.tags());

        getRule().clear();
        for (String key : networkSecurityGroup.securityRules().keySet()) {
            NetworkSecurityGroupRuleResource ruleResource = new NetworkSecurityGroupRuleResource(
                networkSecurityGroup.securityRules().get(key)
            );
            getRule().add(ruleResource);
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups()
            .define(getNetworkSecurityGroupName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroupName())
            .withTags(getTags())
            .create();

        setNetworkSecurityGroupId(networkSecurityGroup.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(getNetworkSecurityGroupId());

        networkSecurityGroup.update().withTags(getTags()).apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.networkSecurityGroups().deleteById(getNetworkSecurityGroupId());
    }

}
