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
import com.azure.resourcemanager.msi.models.Identity;
import gyro.azure.AzureResourceManagerFinder;
import gyro.core.Type;

/**
 * Query identity.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    identity: $(external-query azure::identity {})
 */
@Type("identity")
public class IdentityFinder extends AzureResourceManagerFinder<Identity, IdentityResource> {

    private String id;

    /**
     * The id of the identity.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Identity> findAllAzure(AzureResourceManager client) {
        return client.identities()
            .list()
            .stream()
            .collect(Collectors.toList());
    }

    @Override
    protected List<Identity> findAzure(
        AzureResourceManager client, Map<String, String> filters) {
        List<Identity> identities = new ArrayList<>();

        Identity identity = client.identities().getById(filters.get("id"));

        if (identity != null) {
            identities.add(identity);
        }

        return identities;
    }
}
