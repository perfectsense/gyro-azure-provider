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

import com.microsoft.azure.keyvault.models.IssuerParameters;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class KeyVaultCertificateIssuerParameter extends Diffable implements Copyable<IssuerParameters> {

    private String name;
    private String type;

    /**
     * The name of the issuer of the certificate. Valid values are ``Self``, ``Unknown`` or any issuer already present in your azure account as a valid CA. (Required)
     *
     * @no-doc ValidStrings
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Type of certificate being issued.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    IssuerParameters toIssuerParameters() {
        return new IssuerParameters()
            .withCertificateType(getType())
            .withName(getName());
    }

    @Override
    public void copyFrom(IssuerParameters issuerParameters) {
        setName(issuerParameters.name());
        setType(issuerParameters.certificateType());
    }
}
