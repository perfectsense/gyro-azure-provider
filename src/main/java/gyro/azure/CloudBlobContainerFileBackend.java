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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.FileBackend;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.auth.Credentials;
import gyro.core.auth.CredentialsSettings;

@Type("cloud-blob-container")
public class CloudBlobContainerFileBackend extends FileBackend {

    private String storageAccount;
    private String cloudBlobContainer;
    private String resourceGroup;
    private String prefix;
    private String credentials;

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

    public String getCredentials() {
        if (ObjectUtils.isBlank(credentials)) {
            setCredentials("default");
        }

        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public Stream<String> list() throws Exception {
        return StreamSupport.stream(container().listBlobs(getPrefix(), true).spliterator(), false)
            .map(ListBlobItem::getUri)
            .map(URI::getPath)
            .filter(f -> f.endsWith(".gyro"))
            .map(this::removeContainerAndPrefix);
    }

    @Override
    public InputStream openInput(String file) throws Exception {
        return container().getBlockBlobReference(prefixed(file)).openInputStream();
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {
        return new ByteArrayOutputStream() {

            public void close() {
                try {
                    container()
                        .getBlockBlobReference(prefixed(file))
                        .uploadFromByteArray(toByteArray(), 0, toByteArray().length);
                } catch (StorageException | URISyntaxException | IOException e) {
                    throw new GyroException(e.getMessage());
                }
            }
        };
    }

    @Override
    public void delete(String file) throws Exception {
        // Delete the file only if it exists.
        try {
            container().getBlockBlobReference(prefixed(file)).delete();
        } catch (StorageException e) {
            if (e.getHttpStatusCode() != 404) {
                throw e;
            }
        }
    }

    private Azure client() {
        Credentials credentials = getRootScope().getSettings(CredentialsSettings.class)
            .getCredentialsByName()
            .get("azure::" + getCredentials());

        return AzureResource.createClient((AzureCredentials) credentials);
    }

    private CloudBlobContainer container() {
        StorageAccountResource storage = getRootScope().findResourceById(
            StorageAccountResource.class,
            getStorageAccount());

        StorageAccount storageAccount = client().storageAccounts()
            .getByResourceGroup(getResourceGroup(), getStorageAccount());
        storage.copyFrom(storageAccount);

        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storage.getConnection());
            CloudBlobClient blobClient = account.createCloudBlobClient();

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
}
