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

import com.azure.core.credential.TokenCredential;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.communication.CommunicationManager;
import gyro.core.resource.Resource;

public abstract class AzureResource extends Resource {

    protected String getRegion() {
        return credentials(AzureCredentials.class).getRegion();
    }

    public static <T> T createClient(Class<T> clientClass, AzureCredentials credentials) {
        return credentials.createClient(clientClass);
    }

    protected <T> T createClient(Class<T> clientClass) {
        return AzureResource.createClient(clientClass, credentials(AzureCredentials.class));
    }

    protected AzureResourceManager createClient() {
        return AzureResource.createClient(AzureResourceManager.class, credentials(AzureCredentials.class));
    }

    protected CommunicationManager createCommunicationClient() {
        return AzureResource.createClient(CommunicationManager.class, credentials(AzureCredentials.class));
    }

    public static TokenCredential getTokenCredential(AzureCredentials credentials) {
        return credentials.getTokenCredential();
    }

    protected TokenCredential getTokenCredential() {
        return credentials(AzureCredentials.class).getTokenCredential();
    }
}
