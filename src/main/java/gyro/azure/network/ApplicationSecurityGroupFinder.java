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

package gyro.azure.network;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query application security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    application-security-group: $(external-query azure::application-security-group {})
 */
@Type("application-security-group")
public class ApplicationSecurityGroupFinder
    extends AzureFinder<ApplicationSecurityGroup, ApplicationSecurityGroupResource> {

    private String id;

    /**
     * The ID of the Application Security Group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ApplicationSecurityGroup> findAllAzure(AzureResourceManager client) {
        return client.applicationSecurityGroups().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<ApplicationSecurityGroup> findAzure(AzureResourceManager client, Map<String, String> filters) {
        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups()
            .getById(filters.get("id"));
        if (applicationSecurityGroup == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(applicationSecurityGroup);
        }
    }
}
