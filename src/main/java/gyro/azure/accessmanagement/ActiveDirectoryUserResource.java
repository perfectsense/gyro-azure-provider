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

import java.util.Set;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
     * Name of the user.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Email of the user.
     */
    @Required
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The password for the user.
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
        setEmail(user.innerModel().mailNickname());
        setPrincipalName(user.userPrincipalName());
        setId(user.id());
        setAccountEnabled(user.innerModel().accountEnabled());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        ActiveDirectoryUser user = client.accessManagement().activeDirectoryUsers().getById(getId());

        if (user == null) {
            return false;
        }

        copyFrom(user);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

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
        AzureResourceManager client = createResourceManagerClient();

        client.accessManagement().activeDirectoryUsers().deleteById(getId());
    }
}
