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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

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
 *         public-access: "container"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-blob-container")
public class CloudBlobContainerResource extends AzureResource implements Copyable<BlobContainerClient> {

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
     * The public access of the container.
     */
    @ValidStrings({ "blob", "container" })
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

    @Updatable
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
    public void copyFrom(BlobContainerClient container) {
        setPublicAccess(Optional.ofNullable(container).map(BlobContainerClient::getAccessPolicy).map(
            BlobContainerAccessPolicies::getBlobAccessType).map(PublicAccessType::toString).orElse(null));
        setName(container.getBlobContainerName());
        setMetadata(container.getProperties().getMetadata());
        setStorageAccount(findById(StorageAccountResource.class, container.getAccountName()));
    }

    @Override
    public boolean refresh() {
        BlobContainerClient blobContainer = blobContainer();

        if (!blobContainer.exists()) {
            return false;
        }

        copyFrom(blobContainer);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        BlobContainerClient blobContainer = blobContainer();

        try {
            blobContainer.create();

        } catch (BlobStorageException ex) {
            throw new GyroException(
                String.format("Could not create container [%s] as it already exists", getName()), ex);
        }

        state.save();

        if (!getMetadata().isEmpty()) {
            blobContainer.setMetadata(getMetadata());
        }

        state.save();

        if (getPublicAccess() != null) {
            StorageAccount refreshedStorageAccount = getStorageAccount().getStorageAccount();

            if (refreshedStorageAccount != null && refreshedStorageAccount.isBlobPublicAccessAllowed()) {
                Wait.atMost(2, TimeUnit.MINUTES)
                    .prompt(false)
                    .checkEvery(10, TimeUnit.SECONDS)
                    .until(() -> {
                        try {
                            blobContainer.setAccessPolicy(PublicAccessType.fromString(getPublicAccess()), null);
                            return true;

                        } catch (BlobStorageException ex) {
                            if (BlobErrorCode.fromString("PublicAccessNotPermitted").equals(ex.getErrorCode())) {
                                // Retrying as the storage account has not transitioned to public yet
                                return false;
                            }
                            throw ex;
                        }
                    });
            }
        }

        copyFrom(blobContainer);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        BlobContainerClient blobContainer = blobContainer();

        if (changedFieldNames.contains("metadata")) {
            blobContainer.setMetadata(getMetadata());
        }

        if (changedFieldNames.contains("public-access")) {
            blobContainer.setAccessPolicy(PublicAccessType.fromString(getPublicAccess()), null);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        BlobContainerClient blobContainer = blobContainer();

        blobContainer.delete();
    }

    protected BlobContainerClient blobContainer() {
        BlobServiceClient client = new BlobServiceClientBuilder()
            .connectionString(getStorageAccount().getConnection())
            .buildClient();

        return client.getBlobContainerClient(getName());
    }
}
