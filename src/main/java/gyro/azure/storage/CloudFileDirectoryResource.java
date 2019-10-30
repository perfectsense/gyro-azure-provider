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
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;

import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Creates a cloud file directory
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-file-directory cloud-file-directory
 *         path: "/example/directory/path"
 *         cloud-file-share: $(azure::cloud-file-share cloud-file-share-example)
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-file-directory")
public class CloudFileDirectoryResource extends AzureResource implements Copyable<CloudFileDirectory> {

    private String path;
    private String name;
    private CloudFileShareResource cloudFileShare;
    private StorageAccountResource storageAccount;

    /**
     * The Cloud File Directory path. (Required)
     */
    @Required
    @Id
    public String getPath() {
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }

        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The name of the Cloud File Directory.
     */
    @Output
    public String getName() {
        return Paths.get(getPath()).getFileName().toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Cloud File Share under which the Cloud File Directory resides. (Required)
     */
    @Required
    public CloudFileShareResource getCloudFileShare() {
        return cloudFileShare;
    }

    public void setCloudFileShare(CloudFileShareResource cloudFileShare) {
        this.cloudFileShare = cloudFileShare;
    }

    /**
     * The Storage Account where the Cloud File Directory will be created. (Required)
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(CloudFileDirectory directory) {
        try {
            setStorageAccount(findById(StorageAccountResource.class, directory.getStorageUri().getPrimaryUri().getAuthority().split(".file.core")[0]));
            setPath(directory.getStorageUri().getPrimaryUri().getPath().split(directory.getShare().getName())[1]);
            setName(directory.getName());
            setCloudFileShare(findById(CloudFileShareResource.class, directory.getShare().getName()));
        } catch (Exception ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public boolean refresh() {
        try {
            CloudFileDirectory directory = cloudFileDirectory();
            if (!directory.exists()) {
                return false;
            }

            copyFrom(directory);

            return true;
        }  catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudFileDirectory directory = cloudFileDirectory();
        directory.create();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudFileDirectory directory = cloudFileDirectory();
        directory.delete();
    }

    private CloudFileDirectory cloudFileDirectory() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudFileClient fileClient = storageAccount.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference(getCloudFileShare().getName());

        CloudFileDirectory rootDirectory = share.getRootDirectoryReference();

        Path cloudFilePath = Paths.get(getPath()).getParent();
        String finalDirectory = Paths.get(getPath()).getFileName().toString();
        for (Path path : cloudFilePath) {
            String currentDirectory = path.toString();
            rootDirectory = rootDirectory.getDirectoryReference(currentDirectory);
            rootDirectory.createIfNotExists();
        }
        return rootDirectory.getDirectoryReference(finalDirectory);
    }
}
