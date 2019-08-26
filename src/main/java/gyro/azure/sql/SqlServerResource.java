package gyro.azure.sql;

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
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a sql server.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::sql-server sql-server-example
 *         administrator-login: "TestAdmin18"
 *         administrator-password: "TestPass18!"
 *         name: "sql-server-example"
 *         region: "westus"
 *         resource-group: $(azure::resource-group sql-server-example)
 *         system-assigned-msi: true
 *         tags: {
 *             Name: "sql-server-example"
 *         }
 *     end
 */
@Type("sql-server")
public class SqlServerResource extends AzureResource implements Copyable<SqlServer> {

    private Boolean withAccessFromAzureServices;
    private String administratorLogin;
    private String administratorPassword;
    private String id;
    private String name;
    private String region;
    private ResourceGroupResource resourceGroup;
    private Boolean systemAssignedMsi;
    private Map<String, String> tags;
    private Set<SqlFirewallRuleResource> firewallRule;
    private Set<SqlElasticPoolResource> elasticPool;
    private Set<SqlVirtualNetworkRuleResource> virtualNetworkRule;

    /**
     * Determines if the azure portal will have access to the Sql Server. Defaults to ``true``.
     */
    @Updatable
    public Boolean getWithAccessFromAzureServices() {
        if (withAccessFromAzureServices == null) {
            withAccessFromAzureServices = true;
        }

        return withAccessFromAzureServices;
    }

    public void setWithAccessFromAzureServices(Boolean withAccessFromAzureServices) {
        this.withAccessFromAzureServices = withAccessFromAzureServices;
    }

    /**
     * The administrator login for the Sql Server. (Required)
     */
    @Required
    public String getAdministratorLogin() {
        return administratorLogin;
    }

    public void setAdministratorLogin(String administratorLogin) {
        this.administratorLogin = administratorLogin;
    }

    /**
     * The administrator password for the Sql Server. (Required)
     */
    @Required
    @Updatable
    public String getAdministratorPassword() {
        return administratorPassword;
    }

    public void setAdministratorPassword(String administratorPassword) {
        this.administratorPassword = administratorPassword;
    }

    /**
     * The ID of the Sql Server.
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
     * The name of the Sql Server. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Sql Server's region. (Required)
     */
    @Required
    @Override
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Name of the resource group under which the Sql Server would reside. (Required)
     */
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Determines if the system will set a local Managed Service Identity (MSI) for the Sql Server. (Optional)
     */
    @Required
    @Updatable
    public Boolean getSystemAssignedMsi() {
        if (systemAssignedMsi == null) {
            systemAssignedMsi = false;
        }
        return systemAssignedMsi;
    }

    public void setSystemAssignedMsi(Boolean systemAssignedMsi) {
        this.systemAssignedMsi = systemAssignedMsi;
    }

    /**
     * The tags for the Sql Server. (Optional)
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
     * A set of Firewall Rules for the Sql Server.
     */
    @Updatable
    public Set<SqlFirewallRuleResource> getFirewallRule() {
        if (firewallRule == null) {
            firewallRule = new HashSet<>();
        }

        return firewallRule;
    }

    public void setFirewallRule(Set<SqlFirewallRuleResource> firewallRule) {
        this.firewallRule = firewallRule;
    }

    /**
     * A set of Elastic Pools for the Sql Server.
     */
    @Updatable
    public Set<SqlElasticPoolResource> getElasticPool() {
        if (elasticPool == null) {
            elasticPool = new HashSet<>();
        }

        return elasticPool;
    }

    public void setElasticPool(Set<SqlElasticPoolResource> elasticPool) {
        this.elasticPool = elasticPool;
    }

    /**
     * A set of Virtual Network Rules for the Sql Server.
     */
    @Updatable
    public Set<SqlVirtualNetworkRuleResource> getVirtualNetworkRule() {
        if (virtualNetworkRule == null) {
            virtualNetworkRule = new HashSet<>();
        }

        return virtualNetworkRule;
    }

    public void setVirtualNetworkRule(Set<SqlVirtualNetworkRuleResource> virtualNetworkRule) {
        this.virtualNetworkRule = virtualNetworkRule;
    }

    @Override
    public void copyFrom(SqlServer sqlServer) {
        setAdministratorLogin(sqlServer.administratorLogin());
        setId(sqlServer.id());
        setName(sqlServer.name());
        setSystemAssignedMsi(sqlServer.isManagedServiceIdentityEnabled());
        setTags(sqlServer.tags());
        setResourceGroup(findById(ResourceGroupResource.class, sqlServer.resourceGroupName()));
        setWithAccessFromAzureServices(sqlServer.firewallRules().list().stream().anyMatch(o -> o.name().equals("AllowAllWindowsAzureIps")));
        setFirewallRule(sqlServer.firewallRules().list().stream().filter(o -> !o.name().equals("AllowAllWindowsAzureIps")).map(o -> {
            SqlFirewallRuleResource firewallRule = newSubresource(SqlFirewallRuleResource.class);
            firewallRule.copyFrom(o);
            return firewallRule;
        }).collect(Collectors.toSet()));
        setVirtualNetworkRule(sqlServer.virtualNetworkRules().list().stream().map(o -> {
            SqlVirtualNetworkRuleResource virtualNetworkRule = newSubresource(SqlVirtualNetworkRuleResource.class);
            virtualNetworkRule.copyFrom(o);
            return virtualNetworkRule;
        }).collect(Collectors.toSet()));
        setElasticPool(sqlServer.elasticPools().list().stream().map(o -> {
            SqlElasticPoolResource elasticPool = newSubresource(SqlElasticPoolResource.class);
            elasticPool.copyFrom(o);
            //tags arn't refreshed
            //api fails to provide.
            getElasticPool().stream().filter(oo -> oo.getName().equals(o.name())).findFirst().ifPresent(pool -> elasticPool.setTags(pool.getTags()));
            return elasticPool;
        }).collect(Collectors.toSet()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SqlServer sqlServer = client.sqlServers().getById(getId());

        if (sqlServer == null) {
            return false;
        }

        copyFrom(sqlServer);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        SqlServer.DefinitionStages.WithCreate withCreate = client.sqlServers().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroup().getName())
                .withAdministratorLogin(getAdministratorLogin())
                .withAdministratorPassword(getAdministratorPassword())
                .withTags(getTags());

        if (getSystemAssignedMsi()) {
            withCreate.withSystemAssignedManagedServiceIdentity();
        }

        if (!getWithAccessFromAzureServices()) {
            withCreate.withoutAccessFromAzureServices();
        }

        SqlServer sqlServer = withCreate.create();

        copyFrom(sqlServer);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        if (changedProperties.contains("with-access-from-azure-services")) {
            SqlServer server = client.sqlServers().getById(getId());
            if (getWithAccessFromAzureServices()) {
                server.firewallRules().define("AllowAllWindowsAzureIps").withIPAddress("0.0.0.0").create();
            } else {
                server.firewallRules().delete("AllowAllWindowsAzureIps");
            }
        }

        SqlServer.Update update = client.sqlServers().getById(getId()).update();

        if (getSystemAssignedMsi()) {
            update.withSystemAssignedManagedServiceIdentity();
        }

        update.withAdministratorPassword(getAdministratorPassword());
        update.withTags(getTags());
        SqlServer sqlServer = update.apply();

        copyFrom(sqlServer);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.sqlServers().deleteById(getId());
    }
}
