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
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueue;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a cloud queue
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-queue cloud-queue-example
 *         name: "cloudqueuename"
 *         storage-account: $(azure::storage-account queue-storage-account-example)
 *     end
 */
@Type("cloud-queue")
public class CloudQueueResource extends AzureResource implements Copyable<CloudQueue> {

    private String name;
    private StorageAccountResource storageAccount;
    private String id;

    /**
     * The name of the Queue
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Storage Account where the queue will be created.
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    /**
     * The ID of the queue.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(CloudQueue queue) {
        setName(queue.getName());
        setStorageAccount(findById(StorageAccountResource.class, queue.getStorageUri().getPrimaryUri().getAuthority().split(".queue.core")[0]));
        setId(String.format("%s/queueServices/default/queues/%s",getStorageAccount().getId(),getName()));
    }

    @Override
    public boolean refresh() {
        try {
            CloudQueue queue = cloudQueue();
            if (!queue.exists()) {
                return false;
            }

            copyFrom(queue);

            return true;
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudQueue queue = cloudQueue();
        queue.create();
        setId(String.format("%s/queueServices/default/queues/%s",getStorageAccount().getId(),getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudQueue queue = cloudQueue();
        queue.delete();
    }

    private CloudQueue cloudQueue() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudQueueClient queueClient = storageAccount.createCloudQueueClient();
        return queueClient.getQueueReference(getName());
    }
}
