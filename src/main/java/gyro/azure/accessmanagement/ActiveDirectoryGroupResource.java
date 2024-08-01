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
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
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
        AzureResourceManager client = createClient(AzureResourceManager.class);

        ActiveDirectoryGroup group = client.accessManagement().activeDirectoryGroups().getById(getId());

        if (group == null) {
            return false;
        }

        copyFrom(group);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

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
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.accessManagement().activeDirectoryGroups().deleteById(getId());
    }
}
