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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroInputStream;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.Set;

/**
 * Creates a cloud file
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-file cloud-file-example
 *         cloud-file-directory: $(azure::cloud-file-directory cloud-file-directory)
 *         cloud-file-share: $(azure::cloud-file-share cloud-file-share-example)
 *         file-path: "test-cloud-file.txt"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-file")
public class CloudFileResource extends AzureResource implements Copyable<CloudFile> {

    private CloudFileDirectoryResource cloudFileDirectory;
    private CloudFileShareResource cloudFileShare;
    private String filePath;
    private String fileName;
    private StorageAccountResource storageAccount;

    /**
     * The Cloud File Directory for the file.
     */
    @Required
    public CloudFileDirectoryResource getCloudFileDirectory() {
        return cloudFileDirectory;
    }

    public void setCloudFileDirectory(CloudFileDirectoryResource cloudFileDirectory) {
        this.cloudFileDirectory = cloudFileDirectory;
    }

    /**
     * The Cloud File Share for the file.
     */
    @Required
    public CloudFileShareResource getCloudFileShare() {
        return cloudFileShare;
    }

    public void setCloudFileShare(CloudFileShareResource cloudFileShare) {
        this.cloudFileShare = cloudFileShare;
    }

    /**
     * The path of the file to upload.
     */
    @Required
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * The name of the file.
     */
    @Output
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * The Storage Account where the file will be created.
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    public void copyFrom(CloudFile file) {
        try {
            setStorageAccount(findById(StorageAccountResource.class, file.getStorageUri().getPrimaryUri().getAuthority().split(".file.core")[0]));
            setCloudFileDirectory(findById(CloudFileDirectoryResource.class, file.getParent().getStorageUri().getPrimaryUri().getPath().split(file.getParent().getShare().getName())[1]));
            setCloudFileShare(findById(CloudFileShareResource.class, file.getShare().getName()));
            setFileName(file.getName());
        } catch (Exception ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public boolean refresh() {
        try {
            CloudFile file = cloudFile();
            if (!file.exists()) {
                return false;
            }

            copyFrom(file);

            return true;
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException, IOException {
        GyroInputStream inputStream = openInput(getFilePath());
        CloudFile file = cloudFile();
        file.upload(inputStream, inputStream.available());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudFile file = cloudFile();
        file.delete();
    }

    private CloudFile cloudFile() throws StorageException, URISyntaxException, InvalidKeyException {
        String name = Paths.get(getFilePath()).getFileName().toString();
        CloudFileDirectory root = cloudFileDirectory();
        return root.getFileReference(name);
    }

    private CloudFileDirectory cloudFileDirectory() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudFileClient fileClient = storageAccount.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference(getCloudFileShare().getName());

        CloudFileDirectory rootDirectory = share.getRootDirectoryReference();

        Path cloudFilePath = Paths.get(getCloudFileDirectory().getPath()).getParent();
        String finalDirectory = Paths.get(getCloudFileDirectory().getPath()).getFileName().toString();
        for (Path path : cloudFilePath) {
            String currentDirectory = path.toString();
            rootDirectory = rootDirectory.getDirectoryReference(currentDirectory);
            rootDirectory.createIfNotExists();
        }
        return rootDirectory.getDirectoryReference(finalDirectory);
    }
}
