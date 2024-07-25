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

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.azure.storage.queue.models.QueueStorageException;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
public class CloudQueueResource extends AzureResource implements Copyable<QueueClient> {

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
    public void copyFrom(QueueClient queue) {
        setName(queue.getQueueName());
        setStorageAccount(findById(StorageAccountResource.class, queue.getAccountName()));
        setId(String.format("%s/queueServices/default/queues/%s", getStorageAccount().getId(), getName()));
    }

    @Override
    public boolean refresh() {
        QueueClient queue = verifiedCloudQueue();
        if (queue == null) {
            return false;
        }

        copyFrom(queue);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        QueueClient queueClient = cloudQueue();
        queueClient.create();
        setId(String.format("%s/queueServices/default/queues/%s", getStorageAccount().getId(), getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        QueueClient queueClient = cloudQueue();
        queueClient.delete();
    }

    private QueueClient cloudQueue() {
        QueueServiceClient client = new QueueServiceClientBuilder()
            .connectionString(getStorageAccount().getConnection())
            .buildClient();

        return client.getQueueClient(getName());
    }

    private QueueClient verifiedCloudQueue() {
        QueueClient queueClient = cloudQueue();

        try {
            queueClient.getProperties();
        } catch (QueueStorageException ex) {
            queueClient = null;
        }

        return queueClient;
    }
}
