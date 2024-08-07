/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.accessmanagement;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.fluent.models.OdataErrorMainException;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
    private ActiveDirectoryGroupResource group;
    private ActiveDirectoryUserResource user;
    private String id;

    /**
     * The scope for the role assignment. ID of the level (Subscription, Resource Group etc.).
     */
    @Required
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Role for the role assignment. `See Built In Roles <https://docs.microsoft.com/en-us/azure/role-based-access-control/built-in-roles>`_.
     */
    @Required
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * The principal ID of a identity or user or group on which the role would be assigned. One of 'principal-id' or 'user' or 'group' or 'service-principal' is required.
     */
    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    /**
     * The active directory group which the role would be assigned. One of 'principal-id' or 'user' or 'group' or 'service-principal' is required.
     */
    public ActiveDirectoryGroupResource getGroup() {
        return group;
    }

    public void setGroup(ActiveDirectoryGroupResource group) {
        this.group = group;
    }

    /**
     * The active directory group which the role would be assigned. One of 'principal-id' or 'user' or 'group' or 'service-principal' is required.
     */
    public ActiveDirectoryUserResource getUser() {
        return user;
    }

    public void setUser(ActiveDirectoryUserResource user) {
        this.user = user;
    }

    /**
     * The ID of the role assignment.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the role assignment.
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

        AzureResourceManager client = createClient(AzureResourceManager.class);
        setRole(client.accessManagement().roleDefinitions().getById(roleAssignment.roleDefinitionId()).roleName());

        ActiveDirectoryUser user = null;

        try {
            user = client.accessManagement().activeDirectoryUsers().getById(getPrincipalId());
        } catch (OdataErrorMainException ex) {
            // ignore
        }

        ActiveDirectoryGroup group = null;

        try {
            group = client.accessManagement().activeDirectoryGroups().getById(getPrincipalId());
        } catch (OdataErrorMainException ex) {
            //ignore
        }

        if (user != null) {
            setUser(findById(ActiveDirectoryUserResource.class, user.id()));
        } else if (group != null) {
            setGroup(findById(ActiveDirectoryGroupResource.class, getPrincipalId()));
        }
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        RoleAssignment roleAssignment = client.accessManagement().roleAssignments().getByScope(getScope(), getName());

        if (roleAssignment == null) {
            return false;
        }

        copyFrom(roleAssignment);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        if (Stream.of(getPrincipalId(), getUser(), getGroup()).filter(Objects::nonNull).count() > 1) {
            throw new GyroException("Only one of 'principal-id' or 'user' or 'group' is allowed.");
        }

        RoleAssignment roleAssignment = null;
        setName(UUID.randomUUID().toString());

        try {
            if (getPrincipalId() != null) {
                roleAssignment = client.accessManagement().roleAssignments().define(getName())
                    .forObjectId(getPrincipalId())
                    .withBuiltInRole(BuiltInRole.fromString(getRole()))
                    .withScope(getScope())
                    .create();
            } else if (getGroup() != null) {
                roleAssignment = client.accessManagement().roleAssignments().define(getName())
                    .forGroup(client.accessManagement().activeDirectoryGroups().getById(getGroup().getId()))
                    .withBuiltInRole(BuiltInRole.fromString(getRole()))
                    .withScope(getScope())
                    .create();
            } else if (getUser() != null) {
                roleAssignment = client.accessManagement().roleAssignments().define(getName())
                    .forUser(client.accessManagement().activeDirectoryUsers().getById(getUser().getId()))
                    .withBuiltInRole(BuiltInRole.fromString(getRole()))
                    .withScope(getScope())
                    .create();
            } else {
                throw new GyroException("One of 'principal-id' or 'user' or 'group' is required.");
            }
        } catch (OdataErrorMainException ex) {

            try {
                Wait.atMost(2, TimeUnit.MINUTES)
                    .prompt(false)
                    .checkEvery(20, TimeUnit.SECONDS)
                    .until(() -> client.accessManagement().roleAssignments().getByScope(getScope(), getName()) != null);

                roleAssignment = client.accessManagement().roleAssignments().getByScope(getScope(), getName());

                if (roleAssignment == null) {
                    throw ex;
                }
            } catch (Exception exx) {
                throw ex;
            }
        }

        copyFrom(roleAssignment);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.accessManagement().roleAssignments().deleteById(getId());
    }
}
