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
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileShareProperties;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a cloud file share
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-file-share cloud-file-share-example
 *         name: "example-cloud-file-share"
 *         share-quota: 10
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-file-share")
public class CloudFileShareResource extends AzureResource implements Copyable<CloudFileShare> {

    private String name;
    private Integer shareQuota;
    private StorageAccountResource storageAccount;

    /**
     * The name of the Cloud Share.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The limit on the size of files in GB.
     */
    @Updatable
    public Integer getShareQuota() {
        return shareQuota;
    }

    public void setShareQuota(Integer shareQuota) {
        this.shareQuota = shareQuota;
    }

    /**
     * The Storage Account where the file share will be created.
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(CloudFileShare share) {
        setName(share.getName());
        setShareQuota(share.getProperties().getShareQuota());
        setStorageAccount(findById(StorageAccountResource.class, share.getStorageUri().getPrimaryUri().getAuthority().split(".file.core")[0]));
    }

    @Override
    public boolean refresh() {
        try {
            CloudFileShare share = cloudFileShare();
            if (!share.exists()) {
                return false;
            }

            copyFrom(share);

            return true;
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudFileShare share = cloudFileShare();
        share.create();
        FileShareProperties fileShareProperties = new FileShareProperties();
        fileShareProperties.setShareQuota(getShareQuota());
        share.setProperties(fileShareProperties);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudFileShare share = cloudFileShare();
        FileShareProperties fileShareProperties = new FileShareProperties();
        fileShareProperties.setShareQuota(getShareQuota());
        share.setProperties(fileShareProperties);
    }

    @Override
    public void delete(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudFileShare share = cloudFileShare();
        share.delete();
    }

    private CloudFileShare cloudFileShare() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudFileClient fileClient = storageAccount.createCloudFileClient();
        return fileClient.getShareReference(getName());
    }
}
