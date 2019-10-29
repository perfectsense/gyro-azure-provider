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
import gyro.core.GyroInputStream;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.Set;


/**
 * Creates a cloud blob
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-blob blob-example
 *         blob-directory-path: "/path/to/blob"
 *         container: $(azure::cloud-blob-container blob-container-example)
 *         file-path: "test-blob-doc.txt"
 *         storage-account: $(azure::storage-account blob-storage-account-example)
 *     end
 */
@Type("cloud-blob")
public class CloudBlobResource extends AzureResource implements Copyable<CloudBlockBlob> {

    private String blobDirectoryPath;
    private CloudBlobContainerResource container;
    private String filePath;
    private StorageAccountResource storageAccount;
    private String uri;

    /**
     * The directory path of the Blob. (Required)
     */
    @Required
    public String getBlobDirectoryPath() {
        if (blobDirectoryPath != null && !blobDirectoryPath.startsWith("/")) {
            blobDirectoryPath = "/" + blobDirectoryPath;
        }

        return blobDirectoryPath;
    }

    public void setBlobDirectoryPath(String blobDirectoryPath) {
        this.blobDirectoryPath = blobDirectoryPath;
    }

    /**
     * The container where the Blob is found. (Required)
     */
    @Required
    public CloudBlobContainerResource getContainer() {
        return container;
    }

    public void setContainer(CloudBlobContainerResource container) {
        this.container = container;
    }

    /**
     * The path of the file to upload. (Required)
     */
    @Required
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * The Storage Account where the Blob will be created. (Required)
     */
    @Required
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    /**
     * The fully qualified uri of the Blob.
     */
    @Output
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public void copyFrom(CloudBlockBlob blob) {
        try {
            setUri(blob.getUri().toString());
            setBlobDirectoryPath(blob.getName());
            setContainer(findById(CloudBlobContainerResource.class, blob.getContainer().getName()));
            setStorageAccount(findById(StorageAccountResource.class, blob.getContainer().getStorageUri().getPrimaryUri().getAuthority().split(".blob.core")[0]));
        } catch (Exception ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public boolean refresh() {
        try {
            CloudBlockBlob blob = cloudBlobBlob();
            if (!blob.exists()) {
                return false;
            }

            copyFrom(blob);

            return true;
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException, IOException {
        CloudBlockBlob blob = cloudBlobBlob();
        GyroInputStream file = openInput(getFilePath());
        blob.upload(file, file.available());
        setUri(blob.getUri().toString());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudBlockBlob blob = cloudBlobBlob();
        blob.delete();
    }

    private CloudBlockBlob cloudBlobBlob() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudBlobClient client = account.createCloudBlobClient();
        CloudBlobContainer container = client.getContainerReference(getContainer().getName());

        if (StringUtils.countMatches(getBlobDirectoryPath(), "/") > 1) {
            CloudBlobDirectory directory = createDirectories();
            return directory.getBlockBlobReference(getBlobDirectoryPath().substring(getBlobDirectoryPath().lastIndexOf("/") + 1));
        } else {
            return container.getBlockBlobReference(getBlobDirectoryPath().substring(getBlobDirectoryPath().lastIndexOf("/") + 1));
        }
    }

    private CloudBlobDirectory createDirectories() throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount account = CloudStorageAccount.parse(getStorageAccount().getConnection());
        CloudBlobClient client = account.createCloudBlobClient();
        CloudBlobContainer container = client.getContainerReference(getContainer().getName());
        Path directoryPath = Paths.get(getBlobDirectoryPath()).getParent();
        Iterator<Path> iter = directoryPath.iterator();
        CloudBlobDirectory directory = container.getDirectoryReference(iter.next().toString());
        while (iter.hasNext()) {
            directory = directory.getDirectoryReference(iter.next().toString());
        }
        return directory;
    }
}
