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

    /**
     * Determines if the azure portal will have access to the SQL Server. (Required)
     */
    @Required
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
    @Required
    public String getAdministratorLogin() {
        return administratorLogin;
    }

    public void setAdministratorLogin(String administratorLogin) {
        this.administratorLogin = administratorLogin;
    }

    /**
     * The administrator password. (Required)
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
     * The ID of the SQL Server.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the SQL Server. (Required)
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The region of the SQL Server. (Required)
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
     * The Resource Group under which this SQL Server would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Determines if the system will set a local Managed Service Identity (MSI) for the server. (Optional)
     */
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
     * The tags for the sql server. (Optional)
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
    public void copyFrom(SqlServer sqlServer) {
        setAdministratorLogin(sqlServer.administratorLogin());
        setId(sqlServer.id());
        setName(sqlServer.name());
        setSystemAssignedMsi(sqlServer.isManagedServiceIdentityEnabled());
        setTags(sqlServer.tags());
        findById(ResourceGroupResource.class, sqlServer.resourceGroupName());
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

        setId(sqlServer.id());
        copyFrom(sqlServer);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        SqlServer.Update update = client.sqlServers().getById(getId()).update();

        if (getSystemAssignedMsi()) {
            update.withSystemAssignedManagedServiceIdentity();
        }

        update.withAdministratorPassword(getAdministratorPassword());
        update.withTags(getTags());
        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.sqlServers().deleteById(getId());
    }
}
