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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidNumbers;
import gyro.core.validation.ValidStrings;

public class KeyVaultCertificatePolicy extends Diffable implements Copyable<CertificatePolicy> {

    private String certificateType;
    private String contentType;
    private String issuerName;
    private String subject;
    private String keyType;
    private String keyCurveName;
    private Boolean enabled;
    private Boolean transparent;
    private Boolean exportable;
    private Boolean keyReusable;

    private Integer validityInMonths;
    private Integer keySize;
    private Date createdOn;
    private Date updatedOn;
    private KeyVaultCertificateSubjectAlternativeName subjectAlternativeName;
    private List<String> keyUsage;
    private List<String> enhancedKeyUsage;
    private List<KeyVaultCertificateLifetime> lifetimeAction;

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

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

    /**
     * The name of the issuer of the certificate. Valid values are ``Self``, ``Unknown`` or any issuer already present in your azure account as a valid CA.
     *
     * @no-doc ValidStrings
     */
    @Output
    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    /**
     * The x.500 distinguished name.
     */
    @Required
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * The key type.
     */
    @ValidStrings({"RSA", "RSA-HSM", "EC", "EC-HSM"})
    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * The key curve name.
     */
    @ValidStrings({"P-256", "P-384", "P-521", "P-256K"})
    public String getKeyCurveName() {
        return keyCurveName;
    }

    public void setKeyCurveName(String keyCurveName) {
        this.keyCurveName = keyCurveName;
    }

    /**
     * Enable or Disable the certificate for use.
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Enable or Disable transparency of the certificate.
     */
    public Boolean getTransparent() {
        return transparent;
    }

    public void setTransparent(Boolean transparent) {
        this.transparent = transparent;
    }

    /**
     * When set to ``true`` allows the certificates private key to be exportable.
     */
    @Required
    public Boolean getExportable() {
        return exportable;
    }

    public void setExportable(Boolean exportable) {
        this.exportable = exportable;
    }

    /**
     * When set to ``true`` allows the certificate key to be reused or renewed.
     */
    @Required
    public Boolean getKeyReusable() {
        return keyReusable;
    }

    public void setKeyReusable(Boolean keyReusable) {
        this.keyReusable = keyReusable;
    }

    /**
     * Validation of the certificate in months.
     */
    @Required
    @Range(min = 1, max = 12)
    public Integer getValidityInMonths() {
        return validityInMonths;
    }

    public void setValidityInMonths(Integer validityInMonths) {
        this.validityInMonths = validityInMonths;
    }

    /**
     * The key size.
     */
    @ValidNumbers({2048, 3072, 4096})
    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    /**
     * The date time value in UTC of when the certificate was created.
     */
    @Output
    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * The date time value in UTC of when the certificate was last updated.
     */
    @Output
    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Alternate name config for the certificate.
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificateSubjectAlternativeName
     */
    public KeyVaultCertificateSubjectAlternativeName getSubjectAlternativeName() {
        return subjectAlternativeName;
    }

    public void setSubjectAlternativeName(KeyVaultCertificateSubjectAlternativeName subjectAlternativeName) {
        this.subjectAlternativeName = subjectAlternativeName;
    }

    /**
     * A list of key usage flags.
     */
    @ValidStrings({"digitalSignature", "nonRepudiation", "keyEncipherment",
        "dataEncipherment", "keyAgreement", "keyCertSign",
        "cRLSign", "encipherOnly", "decipherOnly"})
    public List<String> getKeyUsage() {
        if (keyUsage == null) {
            keyUsage = new ArrayList<>();
        }

        return keyUsage;
    }

    public void setKeyUsage(List<String> keyUsage) {
        this.keyUsage = keyUsage;
    }

    /**
     * A list of enhanced key usage flags.
     */
    public List<String> getEnhancedKeyUsage() {
        if (enhancedKeyUsage == null) {
            enhancedKeyUsage = new ArrayList<>();
        }

        return enhancedKeyUsage;
    }

    public void setEnhancedKeyUsage(List<String> enhancedKeyUsage) {
        this.enhancedKeyUsage = enhancedKeyUsage;
    }

    /**
     * Lifetime config for the certificate policy.
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

    @Override
    public void copyFrom(CertificatePolicy certificatePolicy) {
        setCertificateType(certificatePolicy.getCertificateType());
        setContentType(certificatePolicy.getContentType().toString());
        setIssuerName(certificatePolicy.getIssuerName());
        setSubject(certificatePolicy.getSubject());
        setKeyType(certificatePolicy.getKeyType().toString());
        setKeyCurveName(certificatePolicy.getKeyCurveName().toString());
        setValidityInMonths(certificatePolicy.getValidityInMonths());
        setKeySize(certificatePolicy.getKeySize());
        setCreatedOn(Date.from(certificatePolicy.getCreatedOn().toInstant()));
        setUpdatedOn(Date.from(certificatePolicy.getUpdatedOn().toInstant()));
        setEnhancedKeyUsage(certificatePolicy.getEnhancedKeyUsage());

        setTransparent(certificatePolicy.isCertificateTransparent());
        setEnabled(certificatePolicy.isEnabled());
        setExportable(certificatePolicy.isExportable());
        setKeyReusable(certificatePolicy.isKeyReusable());

        setKeyUsage(Optional.ofNullable(certificatePolicy.getKeyUsage())
            .map(o -> o.stream().map(ExpandableStringEnum::toString).collect(Collectors.toList()))
            .orElse(null));

        setSubjectAlternativeName(Optional.ofNullable(certificatePolicy.getSubjectAlternativeNames())
            .map(o -> {
                KeyVaultCertificateSubjectAlternativeName subjectAlternativeName
                    = newSubresource(KeyVaultCertificateSubjectAlternativeName.class);
                subjectAlternativeName.copyFrom(o);
                return subjectAlternativeName;
            }).orElse(null));

        setLifetimeAction(Optional.ofNullable(certificatePolicy.getLifetimeActions())
            .map(o -> o.stream().map(oo -> {
                KeyVaultCertificateLifetime lifetime = newSubresource(KeyVaultCertificateLifetime.class);
                lifetime.copyFrom(oo);
                return lifetime;
            }).collect(Collectors.toList())).orElse(null));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CertificatePolicy toCertificatePolicy() {
        CertificatePolicy policy = CertificatePolicy.getDefault();
        policy.setLifetimeActions(getLifetimeAction()
            .stream().map(KeyVaultCertificateLifetime::toLifetimeAction)
            .toArray(LifetimeAction[]::new));

        policy.setCertificateType(getCertificateType());
        policy.setCertificateTransparent(getTransparent());
        policy.setContentType(CertificateContentType.fromString(getContentType()));
        policy.setKeyCurveName(CertificateKeyCurveName.fromString(getKeyCurveName()));
        policy.setKeySize(getKeySize());
        policy.setValidityInMonths(getValidityInMonths());
        policy.setEnabled(getEnabled());
        policy.setEnhancedKeyUsage(getEnhancedKeyUsage());
        policy.setExportable(getExportable());
        policy.setKeyReusable(getKeyReusable());
        policy.setSubjectAlternativeNames(getSubjectAlternativeName().toSubjectAlternativeNames());
        policy.setKeyUsage(getKeyUsage().stream()
            .map(CertificateKeyUsage::fromString)
            .toArray(CertificateKeyUsage[]::new));
        policy.setKeyType(CertificateKeyType.fromString(getKeyType()));
        policy.setSubject(getSubject());

        return policy;
    }
}
