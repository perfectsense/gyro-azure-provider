package gyro.azure.sql;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.sql.SqlServer;

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
@ResourceName("sql-server")
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

    public Boolean getWithAccessFromAzureServices() {
        if (withAccessFromAzureServices == null) {
            withAccessFromAzureServices = true;
        }

        return withAccessFromAzureServices;
    }

    public void setWithAccessFromAzureServices(Boolean withAccessFromAzureServices) {
        this.withAccessFromAzureServices = withAccessFromAzureServices;
    }

    public String getAdministratorLogin() {
        return administratorLogin;
    }

    public void setAdministratorLogin(String administratorLogin) {
        this.administratorLogin = administratorLogin;
    }

    @ResourceDiffProperty(updatable = true)
    public String getAdministratorPassword() {
        return administratorPassword;
    }

    public void setAdministratorPassword(String administratorPassword) {
        this.administratorPassword = administratorPassword;
    }

    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    @ResourceDiffProperty(updatable = true)
    public Boolean getSystemAssignedMsi() {
        return systemAssignedMsi;
    }

    public void setSystemAssignedMsi(Boolean systemAssignedMsi) {
        this.systemAssignedMsi = systemAssignedMsi;
    }

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

        if (getSystemAssignedMsi() != null) {
            if (getSystemAssignedMsi()) {
                withCreate.withSystemAssignedManagedServiceIdentity();
            }
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

        if (getSystemAssignedMsi() != null) {
            if (getSystemAssignedMsi()) {
                update.withSystemAssignedManagedServiceIdentity();
            }
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
