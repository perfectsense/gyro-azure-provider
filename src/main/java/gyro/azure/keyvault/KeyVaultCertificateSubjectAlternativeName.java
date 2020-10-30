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

import com.microsoft.azure.keyvault.models.SubjectAlternativeNames;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class KeyVaultCertificateSubjectAlternativeName extends Diffable implements Copyable<SubjectAlternativeNames> {

    private List<String> emails;
    private List<String> dnsNames;
    private List<String> upns;

    /**
     * A list of emails as part of the certificate.
     */
    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    /**
     * A list of DNS names to be associated with the certificate.
     */
    @Required
    public List<String> getDnsNames() {
        return dnsNames;
    }

    public void setDnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
    }

    /**
     * A list of UPNS values.
     */
    public List<String> getUpns() {
        return upns;
    }

    public void setUpns(List<String> upns) {
        this.upns = upns;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    SubjectAlternativeNames toSubjectAlternativeNames() {
        return new SubjectAlternativeNames()
            .withDnsNames(getDnsNames())
            .withEmails(getEmails())
            .withUpns(getUpns());
    }

    @Override
    public void copyFrom(SubjectAlternativeNames subjectAlternativeNames) {
        setDnsNames(subjectAlternativeNames.dnsNames());
        setEmails(subjectAlternativeNames.emails());
        setUpns(subjectAlternativeNames.upns());
    }
}
