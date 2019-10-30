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

import gyro.azure.AzureResource;

import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a cloud table
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-table cloud-table-example
 *         name: "cloudtablename"
 *         storage-account: $(azure::storage-account queue-storage-account-example)
 *     end
 */
@Type("cloud-table")
public class CloudTableResource extends AzureResource implements Copyable<CloudTable> {

    private String name;
    private StorageAccountResource storageAccount;

    /**
     * The name of the Table (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Storage Account where the table will be created. (Required)
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(CloudTable cloudTable) {
        setName(cloudTable.getName());
        setStorageAccount(findById(StorageAccountResource.class, cloudTable.getStorageUri().getPrimaryUri().getAuthority().split(".table.core")[0]));
    }

    @Override
    public boolean refresh() {
        try {
            CloudTable cloudTable = cloudTable();
            if (!cloudTable.exists()) {
                return false;
            }

            copyFrom(cloudTable);

            return true;
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudTable cloudTable = cloudTable();
        cloudTable.create();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudTable cloudTable = cloudTable();
        cloudTable.delete();
    }

    private CloudTable cloudTable() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudTableClient tableClient = account.createCloudTableClient();
        return tableClient.getTableReference(getName());
    }
}
