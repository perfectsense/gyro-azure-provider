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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.microsoft.azure.keyvault.models.CertificatePolicy;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;

public class KeyVaultCertificatePolicy extends Diffable implements Copyable<CertificatePolicy> {

    private KeyVaultCertificateIssuerParameter issuerParameter;
    private KeyVaultCertificateKeyProperties keyProperties;
    private List<KeyVaultCertificateLifetime> lifetimeAction;
    private KeyVaultCertificateSecretProperties secretProperties;
    private KeyVaultCertificateX509Properties x509Properties;
    private KeyVaultCertificateAttribute attribute;

    /**
     * Issuer parameter config for the certificate policy. (Required)
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateIssuerParameter
     */
    @Required
    public KeyVaultCertificateIssuerParameter getIssuerParameter() {
        return issuerParameter;
    }

    public void setIssuerParameter(KeyVaultCertificateIssuerParameter issuerParameter) {
        this.issuerParameter = issuerParameter;
    }

    /**
     * The key properties for the certificate policy. (Required)
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateKeyProperties
     */
    @Required
    public KeyVaultCertificateKeyProperties getKeyProperties() {
        return keyProperties;
    }

    public void setKeyProperties(KeyVaultCertificateKeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    /**
     * Lifetime config for the certificate policy. (Required)
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateLifetime
     */
    @Required
    @CollectionMax(1)
    public List<KeyVaultCertificateLifetime> getLifetimeAction() {
        if (lifetimeAction == null) {
            lifetimeAction = new ArrayList<>();
        }

        return lifetimeAction;
    }

    public void setLifetimeAction(List<KeyVaultCertificateLifetime> lifetimeAction) {
        this.lifetimeAction = lifetimeAction;
    }

    /**
     * Secrets config for the certificate policy. (Required)
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateSecretProperties
     */
    @Required
    public KeyVaultCertificateSecretProperties getSecretProperties() {
        return secretProperties;
    }

    public void setSecretProperties(KeyVaultCertificateSecretProperties secretProperties) {
        this.secretProperties = secretProperties;
    }

    /**
     * X509 properties for the certificate policy. (Required)
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateX509Properties
     */
    @Required
    public KeyVaultCertificateX509Properties getX509Properties() {
        return x509Properties;
    }

    public void setX509Properties(KeyVaultCertificateX509Properties x509Properties) {
        this.x509Properties = x509Properties;
    }

    /**
     * Additional attributes for the certificate policy.
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateAttribute
     */
    public KeyVaultCertificateAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(KeyVaultCertificateAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public void copyFrom(CertificatePolicy certificatePolicy) {
        setIssuerParameter(Optional.ofNullable(certificatePolicy.issuerParameters())
            .map(o -> {
                KeyVaultCertificateIssuerParameter issuerParameter = newSubresource(KeyVaultCertificateIssuerParameter.class);
                issuerParameter.copyFrom(o);
                return issuerParameter;
            }).orElse(null));
        setKeyProperties(Optional.ofNullable(certificatePolicy.keyProperties())
            .map(o -> {
                KeyVaultCertificateKeyProperties keyProperties = newSubresource(KeyVaultCertificateKeyProperties.class);
                keyProperties.copyFrom(o);
                return keyProperties;
            }).orElse(null));
        setLifetimeAction(Optional.ofNullable(certificatePolicy.lifetimeActions())
            .map(o -> o.stream().map(oo -> {
                KeyVaultCertificateLifetime lifetime = newSubresource(KeyVaultCertificateLifetime.class);
                lifetime.copyFrom(oo);
                return lifetime;
            }).collect(Collectors.toList())).orElse(null));certificatePolicy.lifetimeActions();
        setX509Properties(Optional.ofNullable(certificatePolicy.x509CertificateProperties())
            .map(o -> {
                KeyVaultCertificateX509Properties x509Properties = newSubresource(KeyVaultCertificateX509Properties.class);
                x509Properties.copyFrom(o);
                return x509Properties;
            }).orElse(null));
        setSecretProperties(Optional.ofNullable(certificatePolicy.secretProperties())
            .map( o -> {
                KeyVaultCertificateSecretProperties secretProperties = newSubresource(
                    KeyVaultCertificateSecretProperties.class);
                secretProperties.copyFrom(o);
                return secretProperties;
            }).orElse(null));
        setAttribute(Optional.ofNullable(certificatePolicy.attributes()).map( o -> {
            KeyVaultCertificateAttribute certificateAttribute = newSubresource(KeyVaultCertificateAttribute.class);
            certificateAttribute.copyFrom(o);
            return certificateAttribute;
        }).orElse(null));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CertificatePolicy toCertificatePolicy() {
        CertificatePolicy policy = new CertificatePolicy();
        policy = policy.withIssuerParameters(getIssuerParameter().toIssuerParameters());
        policy = policy.withKeyProperties(getKeyProperties().toKeyProperties());
        policy = policy.withLifetimeActions(getLifetimeAction()
            .stream().map(KeyVaultCertificateLifetime::toLifetimeAction)
            .collect(Collectors.toList()));
        policy = policy.withSecretProperties(getSecretProperties().toSecretProperties());
        policy = policy.withX509CertificateProperties(getX509Properties().toX509CertificateProperties());
        policy = policy.withAttributes(getAttribute() != null ? getAttribute().toAttributes() : null);
        return policy;
    }
}
