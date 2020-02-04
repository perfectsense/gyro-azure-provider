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

public class VaultCertificatePolicy extends Diffable implements Copyable<CertificatePolicy> {

    private VaultCertificateIssuerParameter issuerParameter;
    private VaultCertificateKeyProperties keyProperties;
    private List<VaultCertificateLifetime> lifetimeAction;
    private VaultCertificateSecretProperties secretProperties;
    private VaultCertificateX509Properties x509Properties;
    private VaultCertificateAttribute attribute;

    /**
     * Issuer parameter config for the certificate policy. (Required)
     */
    @Required
    public VaultCertificateIssuerParameter getIssuerParameter() {
        return issuerParameter;
    }

    public void setIssuerParameter(VaultCertificateIssuerParameter issuerParameter) {
        this.issuerParameter = issuerParameter;
    }

    /**
     * The key properties for the certificate policy. (Required)
     */
    @Required
    public VaultCertificateKeyProperties getKeyProperties() {
        return keyProperties;
    }

    public void setKeyProperties(VaultCertificateKeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    /**
     * Lifetime config for the certificate policy. (Required)
     */
    @Required
    @CollectionMax(1)
    public List<VaultCertificateLifetime> getLifetimeAction() {
        if (lifetimeAction == null) {
            lifetimeAction = new ArrayList<>();
        }

        return lifetimeAction;
    }

    public void setLifetimeAction(List<VaultCertificateLifetime> lifetimeAction) {
        this.lifetimeAction = lifetimeAction;
    }

    /**
     * Secrets config for the certificate policy. (Required)
     */
    @Required
    public VaultCertificateSecretProperties getSecretProperties() {
        return secretProperties;
    }

    public void setSecretProperties(VaultCertificateSecretProperties secretProperties) {
        this.secretProperties = secretProperties;
    }

    /**
     * X509 properties for the certificate policy. (Required)
     */
    @Required
    public VaultCertificateX509Properties getX509Properties() {
        return x509Properties;
    }

    public void setX509Properties(VaultCertificateX509Properties x509Properties) {
        this.x509Properties = x509Properties;
    }

    /**
     * Additional attributes for the certificate policy.
     */
    public VaultCertificateAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(VaultCertificateAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public void copyFrom(CertificatePolicy certificatePolicy) {
        setIssuerParameter(Optional.ofNullable(certificatePolicy.issuerParameters())
            .map(o -> {
                VaultCertificateIssuerParameter issuerParameter = newSubresource(VaultCertificateIssuerParameter.class);
                issuerParameter.copyFrom(o);
                return issuerParameter;
            }).orElse(null));
        setKeyProperties(Optional.ofNullable(certificatePolicy.keyProperties())
            .map(o -> {
                VaultCertificateKeyProperties keyProperties = newSubresource(VaultCertificateKeyProperties.class);
                keyProperties.copyFrom(o);
                return keyProperties;
            }).orElse(null));
        setLifetimeAction(Optional.ofNullable(certificatePolicy.lifetimeActions())
            .map(o -> o.stream().map(oo -> {
                VaultCertificateLifetime lifetime = newSubresource(VaultCertificateLifetime.class);
                lifetime.copyFrom(oo);
                return lifetime;
            }).collect(Collectors.toList())).orElse(null));certificatePolicy.lifetimeActions();
        setX509Properties(Optional.ofNullable(certificatePolicy.x509CertificateProperties())
            .map(o -> {
                VaultCertificateX509Properties x509Properties = newSubresource(VaultCertificateX509Properties.class);
                x509Properties.copyFrom(o);
                return x509Properties;
            }).orElse(null));
        setSecretProperties(Optional.ofNullable(certificatePolicy.secretProperties())
            .map( o -> {
                VaultCertificateSecretProperties secretProperties = newSubresource(VaultCertificateSecretProperties.class);
                secretProperties.copyFrom(o);
                return secretProperties;
            }).orElse(null));
        setAttribute(Optional.ofNullable(certificatePolicy.attributes()).map( o -> {
            VaultCertificateAttribute certificateAttribute = newSubresource(VaultCertificateAttribute.class);
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
            .stream().map(VaultCertificateLifetime::toLifetimeAction)
            .collect(Collectors.toList()));
        policy = policy.withSecretProperties(getSecretProperties().toSecretProperties());
        policy = policy.withX509CertificateProperties(getX509Properties().toX509CertificateProperties());
        policy = policy.withAttributes(getAttribute() != null ? getAttribute().toAttributes() : null);
        return policy;
    }
}
