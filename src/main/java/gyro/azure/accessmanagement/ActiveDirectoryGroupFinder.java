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
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query active directory group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    active-directory-group: $(external-query azure::active-directory-group {name: "gyro"})
 */
@Type("active-directory-group")
public class ActiveDirectoryGroupFinder extends AzureFinder<ActiveDirectoryGroup, ActiveDirectoryGroupResource> {
    private String name;
    private String id;

    /**
     * The name of the group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ActiveDirectoryGroup> findAllAzure(Azure client) {
        return client.accessManagement().activeDirectoryGroups().list();
    }

    @Override
    protected List<ActiveDirectoryGroup> findAzure(Azure client, Map<String, String> filters) {
        ActiveDirectoryGroup group = null;
        if (filters.containsKey("id")) {
            group = client.accessManagement().activeDirectoryGroups().getById(filters.get("id"));
        } else if (filters.containsKey("name")) {
            group = client.accessManagement().activeDirectoryGroups().getByName(filters.get("name"));
        } else {
            throw new GyroException("Either 'id' or 'name' is required");
        }

        if (group != null) {
            return Collections.singletonList(group);
        } else {
            return Collections.emptyList();
        }
    }
}
