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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.microsoft.azure.keyvault.models.KeyUsageType;
import com.microsoft.azure.keyvault.models.X509CertificateProperties;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;

public class KeyVaultCertificateX509Properties extends Diffable implements Copyable<X509CertificateProperties> {

    private List<String> keyUsage;
    private String subject;
    private KeyVaultCertificateSubjectAlternativeName subjectAlternativeName;
    private Integer validityInMonths;
    private List<String> ekus;

    /**
     * A list of key usage flags.
     */
    public List<String> getKeyUsage() {
        return keyUsage;
    }

    public void setKeyUsage(List<String> keyUsage) {
        this.keyUsage = keyUsage;
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
     * Validation of the certificate in months. Value should be between 1 to 12.
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
     * A list of x.660 OID.
     */
    public List<String> getEkus() {
        return ekus;
    }

    public void setEkus(List<String> ekus) {
        this.ekus = ekus;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    X509CertificateProperties toX509CertificateProperties() {
        return new X509CertificateProperties()
            .withEkus(getEkus())
            .withKeyUsage(getKeyUsage().stream().map(KeyUsageType::new).collect(Collectors.toList()))
            .withSubject(getSubject())
            .withSubjectAlternativeNames(getSubjectAlternativeName() != null ? getSubjectAlternativeName().toSubjectAlternativeNames() : null)
            .withValidityInMonths(getValidityInMonths());
    }

    @Override
    public void copyFrom(X509CertificateProperties x509CertificateProperties) {
        setEkus(x509CertificateProperties.ekus());
        setSubject(x509CertificateProperties.subject());
        setValidityInMonths(x509CertificateProperties.validityInMonths());
        setKeyUsage(Optional.ofNullable(x509CertificateProperties.keyUsage())
            .map(o -> o.stream().map(KeyUsageType::toString).collect(Collectors.toList())).orElse(null));
        setSubjectAlternativeName(Optional.ofNullable(x509CertificateProperties.subjectAlternativeNames())
            .map(o -> {
                KeyVaultCertificateSubjectAlternativeName subjectAlternativeName = newSubresource(
                    KeyVaultCertificateSubjectAlternativeName.class);
                subjectAlternativeName.copyFrom(o);
                return subjectAlternativeName;
            }).orElse(null));
    }
}
