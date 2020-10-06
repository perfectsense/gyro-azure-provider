/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.azure.keyvault;

import com.microsoft.azure.keyvault.models.SecretProperties;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class KeyVaultCertificateSecretProperties extends Diffable implements Copyable<SecretProperties> {

    private String contentType;

    /**
     * The type of certificate to generate.
     */
    @Required
    @ValidStrings({"application/x-pem-file", "application/x-pkcs12"})
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    SecretProperties toSecretProperties() {
        return new SecretProperties().withContentType(getContentType());
    }

    @Override
    public void copyFrom(SecretProperties secretProperties) {
        setContentType(secretProperties.contentType());
    }
}
