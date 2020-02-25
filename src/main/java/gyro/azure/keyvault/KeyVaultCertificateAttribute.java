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

import com.microsoft.azure.keyvault.models.CertificateAttributes;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import org.joda.time.DateTime;

public class KeyVaultCertificateAttribute extends Diffable implements Copyable<CertificateAttributes> {

    private Boolean enabled;
    private String expires;
    private String notBefore;
    private String created;
    private String updated;

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
     * A date time value value in UTC specifying when the certificate expires. Format ``YYYY-MM-DDTHH:MM:SS.sssZ``. Example ``2020-04-03T15:54:12.000Z``.
     */
    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    /**
     * A date time value value in UTC specifying the not before time. Format ``YYYY-MM-DDTHH:MM:SS.sssZ``. Example ``2020-04-03T15:54:12.000Z``.
     */
    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    /**
     * The date time value in UTC of when the certificate was created.
     */
    @Output
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * The date time value in UTC of when the certificate was last updated.
     */
    @Output
    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CertificateAttributes toAttributes() {
        return (CertificateAttributes) new CertificateAttributes()
            .withEnabled(getEnabled())
            .withExpires(getExpires() != null ? DateTime.parse(getExpires()) : null)
            .withNotBefore(getNotBefore() != null ? DateTime.parse(getNotBefore()) : null);
    }

    @Override
    public void copyFrom(CertificateAttributes attributes) {
        setEnabled(attributes.enabled());
        setExpires(attributes.expires() != null ? attributes.expires().toString() : null);
        setNotBefore(attributes.notBefore() != null ? attributes.notBefore().toString() : null);
        setCreated(attributes.created() != null ? attributes.created().toString() : null);
        setUpdated(attributes.updated() != null ? attributes.updated().toString() : null);
    }
}
