package gyro.azure.accessmanagement;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.Set;
import java.util.UUID;

/**
 * Creates a role assignment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::role-assignment role-assignment-example
 *         principal-id: "ae4d20c4-31ce-42b9-8418-2dcdc8ba1f68"
 *         scope: $(azure::resource-group resource-group-example-role).id
 *         role: "Reader"
 *     end
 */
@Type("role-assignment")
public class RoleAssignmentResource extends AzureResource implements Copyable<RoleAssignment> {
    private String name;
    private String scope;
    private String role;
    private String principalId;
    private String id;

    /**
     * The scope for the Role Assignment. ID of the level (Subscription, Resource Group etc.). (Required)
     */
    @Required
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Role for the Role Assignment. `See Built In Roles <https://docs.microsoft.com/en-us/azure/role-based-access-control/built-in-roles>`_. (Required)
     */
    @Required
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * The Principal ID of a Identity or User or Group on which teh role would be assigned. (Required)
     */
    @Required
    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    /**
     * The ID of the Role Assignment.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Role Assignment.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(RoleAssignment roleAssignment) {
        setName(roleAssignment.name());
        setPrincipalId(roleAssignment.principalId());
        setScope(roleAssignment.scope());
        setId(roleAssignment.id());

        Azure client = createClient();
        setRole(client.accessManagement().roleDefinitions().getById(roleAssignment.roleDefinitionId()).roleName());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        RoleAssignment roleAssignment = client.accessManagement().roleAssignments().getByScope(getScope(), getName());

        if (roleAssignment == null) {
            return false;
        }

        copyFrom(roleAssignment);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        RoleAssignment roleAssignment = client.accessManagement().roleAssignments().define(UUID.randomUUID().toString())
            .forObjectId(getPrincipalId())
            .withBuiltInRole(BuiltInRole.fromString(getRole()))
            .withScope(getScope())
            .create();

        copyFrom(roleAssignment);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        client.accessManagement().roleAssignments().deleteById(getId());
    }
}
