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

package gyro.azure;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.CopyStatus;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.psddev.dari.util.StringUtils;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.FileBackend;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.Type;

@Type("cloud-blob-container")
public class CloudBlobContainerFileBackend extends FileBackend {

    private String storageAccount;
    private String cloudBlobContainer;
    private String resourceGroup;
    private String prefix;

    public String getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(String storageAccount) {
        this.storageAccount = storageAccount;
    }

    public String getCloudBlobContainer() {
        return cloudBlobContainer;
    }

    public void setCloudBlobContainer(String cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    @Override
    public Stream<String> list() throws Exception {
        if (this.equals(GyroCore.getStateBackend(getName()))) {
            return StreamSupport.stream(container().listBlobs(getPrefix(), true).spliterator(), false)
                .map(ListBlobItem::getUri)
                .map(URI::getPath)
                .filter(f -> f.endsWith(".gyro"))
                .map(this::removeContainerAndPrefix);
        }

        return Stream.empty();
    }

    @Override
    public InputStream openInput(String file) throws Exception {
        return getBlockBlobReference(file).openInputStream();
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {
        return getBlockBlobReference(file).openOutputStream();
    }

    @Override
    public void delete(String file) throws Exception {
        getBlockBlobReference(file).deleteIfExists();
    }

    @Override
    public boolean exists(String file) throws Exception {
        return getBlockBlobReference(file).exists();
    }

    @Override
    public void copy(String source, String destination) throws Exception {
        CloudBlockBlob target = getBlockBlobReference(destination);
        target.startCopy(getBlockBlobReference(source));

        long wait = 0L;

        while (true) {
            target.downloadAttributes();
            CopyStatus copyStatus = target.getCopyState().getStatus();

            if (copyStatus != CopyStatus.PENDING) {
                if (copyStatus != CopyStatus.SUCCESS) {
                    throw new GyroException(
                        String.format("Copying %s to %s failed: %s", source, destination, copyStatus));
                }
                break;
            }
            wait += 1000L;
            Thread.sleep(wait);
        }
    }

    private CloudBlobContainer container() {
        String account = getStorageAccount();
        StorageAccount storageAccount = Optional.ofNullable(getCredentials("azure"))
            .filter(AzureCredentials.class::isInstance)
            .map(AzureCredentials.class::cast)
            .map(AzureResource::createClient)
            .map(Azure::storageAccounts)
            .map(e -> e.getByResourceGroup(getResourceGroup(), account))
            .orElseThrow(() -> new GyroException("No storage account available!"));
        StorageAccountResource storage = getRootScope().findResourceById(StorageAccountResource.class, account);
        // storage.copyFrom(storageAccount); // TODO handle this

        try {
            CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(storage.getConnection());
            CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();

            return blobClient.getContainerReference(getCloudBlobContainer());
        } catch (StorageException | URISyntaxException | InvalidKeyException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private String prefixed(String file) {
        return getPrefix() != null ? getPrefix() + '/' + file : file;
    }

    private String removeContainerAndPrefix(String file) {
        String cloudBlobContainer = getCloudBlobContainer();

        if (StringUtils.isBlank(cloudBlobContainer)) {
            throw new IllegalStateException("container can't be null.");
        }

        file = StringUtils.removeStart(file, "/" + cloudBlobContainer + "/");

        if (getPrefix() != null && file.startsWith(getPrefix() + "/")) {
            return file.substring(getPrefix().length() + 1);
        }

        return file;
    }

    private CloudBlockBlob getBlockBlobReference(String file) throws Exception {
        return container().getBlockBlobReference(prefixed(file));
    }
}
