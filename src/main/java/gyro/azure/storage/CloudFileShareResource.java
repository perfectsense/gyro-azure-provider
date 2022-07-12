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

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

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
public class CloudFileShareResource extends AzureResource implements Copyable<ShareClient> {

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
    public void copyFrom(ShareClient share) {
        setName(share.getShareName());
        setShareQuota(share.getProperties().getQuota());
        setStorageAccount(findById(StorageAccountResource.class, share.getAccountName()));
    }

    @Override
    public boolean refresh() {
        ShareClient share = verifiedCloudFileShare();
        if (share == null) {
            return false;
        }

        copyFrom(share);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        ShareClient share = cloudFileShare();
        share.create();

        ShareSetPropertiesOptions options = new ShareSetPropertiesOptions();
        options.setQuotaInGb(getShareQuota());
        share.setProperties(options);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        ShareClient share = cloudFileShare();

        ShareSetPropertiesOptions options = new ShareSetPropertiesOptions();
        options.setQuotaInGb(getShareQuota());
        share.setProperties(options);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ShareClient share = cloudFileShare();
        share.delete();
    }

    private ShareClient cloudFileShare() {
        ShareServiceClient client = new ShareServiceClientBuilder()
            .connectionString(getStorageAccount().getConnection())
            .buildClient();

        return client.getShareClient(getName());
    }

    private ShareClient verifiedCloudFileShare() {
        ShareClient shareClient = cloudFileShare();

        try {
            shareClient.getProperties();
        } catch (ShareStorageException ex) {
            shareClient = null;
        }

        return shareClient;
    }
}
