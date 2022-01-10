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
import java.util.Properties;

import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import gyro.core.GyroException;
import gyro.core.auth.Credentials;

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

    public AzureResourceManager createClient() {
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
                .authenticate(credential, azureProfile);


            return StringUtils.isBlank(subscription)
                ? authenticated.withDefaultSubscription()
                : authenticated.withSubscription(subscription);

        } catch (Exception error) {
            throw new GyroException(error.getMessage(), error);
        }
    }

}
