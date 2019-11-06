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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query active directory user.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    active-directory-user: $(external-query azure::active-directory-user {name: "gyro"})
 */
@Type("active-directory-user")
public class ActiveDirectoryUserFinder extends AzureFinder<ActiveDirectoryUser, ActiveDirectoryUserResource> {
    private String id;
    private String name;

    /**
     * The ID of the user.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the user.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<ActiveDirectoryUser> findAllAzure(Azure client) {
        return client.accessManagement().activeDirectoryUsers().list();
    }

    @Override
    protected List<ActiveDirectoryUser> findAzure(Azure client, Map<String, String> filters) {
        ActiveDirectoryUser user = null;

        if (filters.containsKey("id")) {
            user = client.accessManagement().activeDirectoryUsers().getById(filters.get("id"));
        } else if (filters.containsKey("name")) {
            user = client.accessManagement().activeDirectoryUsers().getByName(filters.get("name"));
        } else {
            throw new GyroException("Either 'id' or 'name' is required");
        }

        if (user != null) {
            return Collections.singletonList(user);
        } else {
            return Collections.emptyList();
        }
    }
}
