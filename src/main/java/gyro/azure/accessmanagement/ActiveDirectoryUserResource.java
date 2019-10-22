package gyro.azure.accessmanagement;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.Set;

/**
 * Creates a active directory user.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::active-directory-user user-example
 *         name: "user-name"
 *         email: "user-email"
 *         password: "Pa55w@rd"
 *     end
 */
@Type("active-directory-user")
public class ActiveDirectoryUserResource extends AzureResource implements Copyable<ActiveDirectoryUser> {
    private String name;
    private String email;
    private String password;
    private String principalName;
    private Boolean accountEnabled;
    private String id;

    /**
     * Name of the user. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Email of the user. (Required)
     */
    @Required
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The password for the user. (Required)
     */
    @Required
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Enable or Disable the user account. Defaults to enabled ``true``.
     */
    public Boolean getAccountEnabled() {
        if (accountEnabled == null) {
            accountEnabled = true;
        }

        return accountEnabled;
    }

    public void setAccountEnabled(Boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    /**
     * The principal name of the User.
     */
    @Output
    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    /**
     * The ID of the User.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(ActiveDirectoryUser user) {
        setName(user.name());
        setEmail(user.inner().mailNickname());
        setPrincipalName(user.userPrincipalName());
        setId(user.id());
        setAccountEnabled(user.inner().accountEnabled());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ActiveDirectoryUser user = client.accessManagement().activeDirectoryUsers().getById(getId());

        if (user == null) {
            return false;
        }

        copyFrom(user);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        ActiveDirectoryUser activeDirectoryUser = client.accessManagement().activeDirectoryUsers()
            .define(getName())
            .withEmailAlias(getEmail())
            .withPassword(getPassword())
            .withAccountEnabled(getAccountEnabled())
            .create();

        copyFrom(activeDirectoryUser);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        client.accessManagement().activeDirectoryUsers().deleteById(getId());
    }
}