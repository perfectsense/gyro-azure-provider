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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("storage-account")
public class StorageAccountFinder extends AzureFinder<StorageAccount, StorageAccountResource> {
    private String id;

    /**
     * The ID of the Storage Account.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<StorageAccount> findAllAzure(Azure client) {
        return client.storageAccounts().list();
    }

    @Override
    protected List<StorageAccount> findAzure(Azure client, Map<String, String> filters) {
        StorageAccount storageAccount = client.storageAccounts().getById(filters.get("id"));
        if (storageAccount == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(storageAccount);
        }
    }
}
