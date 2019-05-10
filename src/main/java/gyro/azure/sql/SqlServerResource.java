package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.sql.SqlServer;
import gyro.core.resource.ResourceUpdatable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 *         resource-group-name: $(azure::resource-group sql-server-example | resource-group-name)
 *         system-assigned-msi: true
 *         tags: {
 *             Name: "sql-server-example"
 *         }
 *     end
 */
@ResourceType("sql-server")
public class SqlServerResource extends AzureResource {

    private Boolean withAccessFromAzureServices;
    private String administratorLogin;
    private String administratorPassword;
    private String id;
    private String name;
    private String region;
    private String resourceGroupName;
    private Boolean systemAssignedMsi;
    private Map<String, String> tags;

    /**
     * Determines if the azure portal will have access to the server. (Required)
     */
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
     * The administrator login. (Required)
     */
    public String getAdministratorLogin() {
        return administratorLogin;
    }

    public void setAdministratorLogin(String administratorLogin) {
        this.administratorLogin = administratorLogin;
    }

    /**
     * The administrator password. (Required)
     */
    @ResourceUpdatable
    public String getAdministratorPassword() {
        return administratorPassword;
    }

    public void setAdministratorPassword(String administratorPassword) {
        this.administratorPassword = administratorPassword;
    }

    /**
     * The id of the server.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the server. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The server's region. (Required)
     */
    @Override
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    /**
     * Determines if the system will set a local Managed Service Identity (MSI) for the server. (Optional)
     */
    @ResourceUpdatable
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
     * The tags for the sql server. (Optional)
     */
    @ResourceUpdatable
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

        SqlServer sqlServer = client.sqlServers().getById(getId());

        if (sqlServer == null) {
            return false;
        }

        setAdministratorLogin(sqlServer.administratorLogin());
        setId(sqlServer.id());
        setName(sqlServer.name());
        setSystemAssignedMsi(sqlServer.isManagedServiceIdentityEnabled());
        setTags(sqlServer.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        SqlServer.DefinitionStages.WithCreate withCreate = client.sqlServers().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName())
                .withAdministratorLogin(getAdministratorLogin())
                .withAdministratorPassword(getAdministratorPassword())
                .withTags(getTags());

        if (getSystemAssignedMsi()) {
            withCreate.withSystemAssignedManagedServiceIdentity();
        }

        if (!getWithAccessFromAzureServices()) {
            withCreate.withoutAccessFromAzureServices();
        }

        SqlServer sqlServer = withCreate.withTags(getTags()).create();

        setId(sqlServer.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlServer.Update update = client.sqlServers().getById(getId()).update();

        if (getSystemAssignedMsi()) {
            update.withSystemAssignedManagedServiceIdentity();
        }

        update.withAdministratorPassword(getAdministratorPassword())
                .withTags(getTags())
                .apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.sqlServers().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "sql server " + getName();
    }
}
