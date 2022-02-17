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

import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a blob container
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-blob-container blob-container-example
 *         name: "blobcontainer"
 *         public-access: "CONTAINER"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-blob-container")
public class CloudBlobContainerResource extends AzureResource implements Copyable<BlobContainer> {

    private String name;
    private String publicAccess;
    private StorageAccountResource storageAccount;
    private String id;
    private Map<String, String> metadata;

    /**
     * The name of the container.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The public access of the container. Valid values are ``Blob`` or ``Container`` or ``Off``
     */
    @Required
    @ValidStrings({"Blob", "Container", "Off"})
    @Updatable
    public String getPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * The storage account resource where the blob container will be created.
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * The ID of the blob container.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(BlobContainer container) {
        setPublicAccess(container.publicAccess().toString());
        setName(container.name());
        setId(container.id());
        setMetadata(container.metadata());

        String storageAccountName = getId()
            .split("Microsoft.Storage/storageAccounts/")[1]
            .split("/blobServices")[0];
        setStorageAccount(findById(StorageAccountResource.class, storageAccountName));
    }

    @Override
    public boolean refresh() {
        StorageAccount storageAccount = getStorageAccount().getStorageAccount();

        BlobContainer blobContainer = storageAccount.manager().blobContainers()
            .get(storageAccount.resourceGroupName(), storageAccount.name(), getName());

        if (blobContainer == null) {
            return false;
        }

        copyFrom(blobContainer);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        StorageAccount storageAccount = getStorageAccount().getStorageAccount();

        BlobContainer.DefinitionStages.WithCreate withCreate = storageAccount.manager().blobContainers()
            .defineContainer(getName())
            .withExistingStorageAccount(storageAccount)
            .withPublicAccess(PublicAccess.fromString(getPublicAccess()));

        if (!getMetadata().isEmpty()) {
            withCreate.withMetadata(getMetadata());
        }

        BlobContainer blobContainer = withCreate.create();

        copyFrom(blobContainer);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        StorageAccount storageAccount = getStorageAccount().getStorageAccount();

        BlobContainer blobContainer = storageAccount.manager().blobContainers()
            .get(storageAccount.resourceGroupName(), storageAccount.name(), getName());

        BlobContainer.Update update = blobContainer.update();

        if (changedFieldNames.contains("metadata")) {
            update = update.withMetadata(getMetadata());
        }

        if (changedFieldNames.contains("public-access")) {
            update = update.withPublicAccess(PublicAccess.fromString(getPublicAccess()));
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        StorageAccount storageAccount = getStorageAccount().getStorageAccount();

        storageAccount.manager().blobContainers()
            .delete(storageAccount.resourceGroupName(), storageAccount.name(), getName());
    }
}
