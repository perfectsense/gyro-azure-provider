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

package gyro.azure.identity;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.msi.Identity;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
public class IdentityFinder extends AzureFinder<Identity, IdentityResource> {
    private String id;

    /**
     * The ID of the identity.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Identity> findAllAzure(Azure client) {
        return client.identities().list();
    }

    @Override
    protected List<Identity> findAzure(Azure client, Map<String, String> filters) {
        Identity identity = client.identities().getById(filters.get("id"));

        if (identity == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(identity);
        }
    }
}
