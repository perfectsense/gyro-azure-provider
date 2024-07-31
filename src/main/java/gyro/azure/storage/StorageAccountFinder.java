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

package gyro.azure.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

@Type("storage-account")
public class StorageAccountFinder extends AzureFinder<AzureResourceManager, StorageAccount, StorageAccountResource> {

    private String id;
    private String resourceGroup;
    private String name;

    /**
     * The ID of the Storage Account.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the resource group the Storage Account belongs.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The name of the Storage Account.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<StorageAccount> findAllAzure(AzureResourceManager client) {
        return client.storageAccounts().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<StorageAccount> findAzure(AzureResourceManager client, Map<String, String> filters) {
        StorageAccount storageAccount;
        if (filters.containsKey("id")) {
            storageAccount = client.storageAccounts().getById(filters.get("id"));
        } else if (filters.containsKey("resource-group") && filters.containsKey("name")) {
            storageAccount = client.storageAccounts()
                .getByResourceGroup(filters.get("resource-group"), filters.get("name"));
        } else {
            throw new GyroException("Either 'id' or both of 'resource-group' and 'name' is required");
        }

        if (storageAccount == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(storageAccount);
        }
    }
}
