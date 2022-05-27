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

import java.io.BufferedInputStream;
import java.util.Set;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroInputStream;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

/**
 * Creates a cloud blob
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cloud-blob blob-example
 *         blob-path: "/path/to/blob"
 *         container: $(azure::cloud-blob-container blob-container-example)
 *         file-path: "test-blob-doc.txt"
 *     end
 */
@Type("cloud-blob")
public class CloudBlobResource extends AzureResource implements Copyable<BlobClient> {

    private String blobPath;
    private CloudBlobContainerResource container;
    private String filePath;
    private String uri;

    /**
     * The directory path of the Blob.
     */
    @Required
    public String getBlobPath() {
        return blobPath;
    }

    public void setBlobPath(String blobPath) {
        this.blobPath = blobPath;
    }

    /**
     * The container where the Blob is found.
     */
    @Required
    public CloudBlobContainerResource getContainer() {
        return container;
    }

    public void setContainer(CloudBlobContainerResource container) {
        this.container = container;
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
    public void copyFrom(BlobClient blob) {
        try {
            setUri(blob.getBlobUrl());
            setBlobPath(blob.getBlobName());
            setContainer(findById(CloudBlobContainerResource.class, blob.getContainerName()));
        } catch (Exception ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public boolean refresh() {
        BlobClient blob = blob();
        if (!blob.exists()) {
            return false;
        }

        copyFrom(blob);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        BlobClient blob = blob();

        try (GyroInputStream inputStream = openInput(getFilePath())) {
            blob.upload(new BufferedInputStream(inputStream), inputStream.available());
        }

        blob = blob();
        setUri(blob.getBlobUrl());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        BlobClient blob = blob();
        blob.delete();
    }

    private BlobClient blob() {
        BlobContainerClient client = getContainer().blobContainer();

        return client.getBlobClient(getBlobPath());
    }
}
