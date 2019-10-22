package gyro.azure.accessmanagement;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
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
 * Creates a active directory group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::active-directory-group group-example
 *         name: "group-name"
 *         email-nick: "group-email"
 *     end
 */
@Type("active-directory-group")
public class ActiveDirectoryGroupResource extends AzureResource implements Copyable<ActiveDirectoryGroup> {
    private String name;
    private String emailNick;
    private String id;

    /**
     * The name of the group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The mail nick name for the group.
     */
    @Required
    public String getEmailNick() {
        return emailNick;
    }

    public void setEmailNick(String emailNick) {
        this.emailNick = emailNick;
    }

    /**
     * The name of the group.
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
    public void copyFrom(ActiveDirectoryGroup group) {
        setName(group.name());
        setId(group.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ActiveDirectoryGroup group = client.accessManagement().activeDirectoryGroups().getById(getId());

        if (group == null) {
            return false;
        }

        copyFrom(group);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        ActiveDirectoryGroup group = client.accessManagement().activeDirectoryGroups()
            .define(getName())
            .withEmailAlias(getEmailNick())
            .create();

        copyFrom(group);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        client.accessManagement().activeDirectoryGroups().deleteById(getId());
    }
}
