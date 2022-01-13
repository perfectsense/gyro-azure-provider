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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Properties;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import gyro.core.GyroException;
import gyro.core.auth.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import retrofit2.Retrofit;

public class AzureCredentials extends Credentials {

    private String region;
    private String credentialFilePath;
    private String logLevel;

    public String getRegion() {
        return region != null ? region.toUpperCase() : null;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCredentialFilePath() {
        return credentialFilePath;
    }

    public void setCredentialFilePath(String credentialFilePath) {
        this.credentialFilePath = credentialFilePath;
    }

    public String getLogLevel() {
        return logLevel != null ? logLevel.toUpperCase() : null;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public Azure createClient() {
        AzureEnvironment environment = AzureEnvironment.AZURE;
        Properties properties;

        try (InputStream input = openInput(getCredentialFilePath())) {
            properties = new Properties();

            properties.load(input);

        } catch (IOException error) {
            throw new GyroException(error.getMessage());
        }
        String tenant = ObjectUtils.to(String.class, properties.get("tenant"));

        AzureTokenCredentials credentials = new ApplicationTokenCredentials(
            ObjectUtils.to(String.class, properties.get("client")),
            tenant,
            ObjectUtils.to(String.class, properties.get("key")),
            environment);

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder().protocols(Collections.singletonList(Protocol.HTTP_1_1));
        RestClient restClient = new RestClient.Builder(httpBuilder, new Retrofit.Builder())
            .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
            .withCredentials(credentials)
            .withSerializerAdapter(new AzureJacksonAdapter())
            .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
            .withInterceptor(new ProviderRegistrationInterceptor(credentials))
            .withInterceptor(new ResourceManagerThrottlingInterceptor())
            .withLogLevel(LogLevel.valueOf(getLogLevel()))
            .build();

        try {
            Azure.Authenticated authenticate = Azure.authenticate(restClient, tenant);
            String subscription = ObjectUtils.to(String.class, properties.get("subscription"));

            return StringUtils.isBlank(subscription)
                ? authenticate.withDefaultSubscription()
                : authenticate.withSubscription(subscription);

        } catch (IOException error) {
            throw new GyroException(error.getMessage(), error);
        }
    }

    public AzureResourceManager createResourceManagerClient() {
        Properties properties;

        try (InputStream input = openInput(getCredentialFilePath())) {
            properties = new Properties();

            properties.load(input);

        } catch (IOException error) {
            throw new GyroException(error.getMessage());
        }

        String tenant = ObjectUtils.to(String.class, properties.get("tenant"));

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(ObjectUtils.to(String.class, properties.get("client")))
            .clientSecret(ObjectUtils.to(String.class, properties.get("key")))
            .tenantId(tenant)
            .build();

        String subscription = ObjectUtils.to(String.class, properties.get("subscription"));

        AzureProfile azureProfile = new AzureProfile(tenant, subscription, com.azure.core.management.AzureEnvironment.AZURE);

        try {
            AzureResourceManager.Authenticated authenticated = AzureResourceManager
                .configure()
                .withHttpClient(new OkHttpAsyncHttpClientBuilder().build())
                .authenticate(credential, azureProfile);


            return StringUtils.isBlank(subscription)
                ? authenticated.withDefaultSubscription()
                : authenticated.withSubscription(subscription);

        } catch (Exception error) {
            throw new GyroException(error.getMessage(), error);
        }
    }

}
