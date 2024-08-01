/*
 * Copyright 2022, Brightspot, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query application.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    application: $(external-query azure::application {})
 */
@Type("application")
public class ApplicationFinder extends AzureFinder<AzureResourceManager, ActiveDirectoryApplication, ApplicationResource> {

    private String id;

    /**
     * The id of the application.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ActiveDirectoryApplication> findAllAzure(AzureResourceManager client) {
        return client.accessManagement()
            .activeDirectoryApplications()
            .list().stream()
            .collect(Collectors.toList());
    }

    @Override
    protected List<ActiveDirectoryApplication> findAzure(
        AzureResourceManager client, Map<String, String> filters) {

        List<ActiveDirectoryApplication> applications = new ArrayList<>();

        ActiveDirectoryApplication application = client.accessManagement()
            .activeDirectoryApplications()
            .getById(filters.get("id"));

        if (application != null) {
            applications.add(application);
        }

        return applications;
    }
}
