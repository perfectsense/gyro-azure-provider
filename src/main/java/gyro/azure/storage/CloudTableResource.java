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

import java.util.Set;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableServiceException;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
public class CloudTableResource extends AzureResource implements Copyable<TableClient> {

    private String name;
    private StorageAccountResource storageAccount;

    /**
     * The name of the Table
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Storage Account where the table will be created.
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(TableClient cloudTable) {
        setName(cloudTable.getTableName());
        setStorageAccount(findById(StorageAccountResource.class, cloudTable.getAccountName()));
    }

    @Override
    public boolean refresh() {
        TableClient tableClient = verifiedCloudTable();
        if (tableClient == null) {
            return false;
        }

        copyFrom(tableClient);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        TableClient tableClient = cloudTable();
        tableClient.createTable();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        TableClient tableClient = cloudTable();
        tableClient.deleteTable();
    }

    private TableClient cloudTable() {
        TableServiceClient client = new TableServiceClientBuilder()
            .connectionString(getStorageAccount().getConnection())
            .buildClient();

        return client.getTableClient(getName());
    }

    private TableClient verifiedCloudTable() {
        TableClient tableClient = cloudTable();

        try {
            tableClient.getTableEndpoint();
        } catch (TableServiceException ex) {
            tableClient = null;
        }

        return tableClient;
    }
}
