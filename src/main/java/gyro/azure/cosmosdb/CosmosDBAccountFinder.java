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

package gyro.azure.cosmosdb;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cosmos db.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cosmos-db: $(external-query azure::cosmos-db {})
 */
@Type("cosmos-db")
public class CosmosDBAccountFinder extends AzureFinder<AzureResourceManager, CosmosDBAccount, CosmosDBAccountResource> {
    private String id;

    /**
     * The ID of the Cosmos DB Account.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<CosmosDBAccount> findAllAzure(AzureResourceManager client) {
        return client.cosmosDBAccounts().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CosmosDBAccount> findAzure(AzureResourceManager client, Map<String, String> filters) {
        CosmosDBAccount cosmosDBAccount = client.cosmosDBAccounts().getById(filters.get("id"));
        if (cosmosDBAccount == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(cosmosDBAccount);
        }
    }
}
