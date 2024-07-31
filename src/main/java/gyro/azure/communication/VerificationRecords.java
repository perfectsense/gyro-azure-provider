/*
 * Copyright 2024, Perfect Sense, Inc.
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

package gyro.azure.communication;

import com.azure.resourcemanager.communication.models.DomainPropertiesVerificationRecords;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;

public class VerificationRecords extends Diffable implements Copyable<DomainPropertiesVerificationRecords> {

    private DomainDnsRecord dkim;
    private DomainDnsRecord spf;
    private DomainDnsRecord dkim2;
    private DomainDnsRecord dmarc;
    private DomainDnsRecord domain;

    /**
     * The dkim verification record.
     */
    @Output
    public DomainDnsRecord getDkim() {
        return dkim;
    }

    public void setDkim(DomainDnsRecord dkim) {
        this.dkim = dkim;
    }

    /**
     * The spf verification record.
     */
    @Output
    public DomainDnsRecord getSpf() {
        return spf;
    }

    public void setSpf(DomainDnsRecord spf) {
        this.spf = spf;
    }

    /**
     * The dkim2 verification record.
     */
    @Output
    public DomainDnsRecord getDkim2() {
        return dkim2;
    }

    public void setDkim2(DomainDnsRecord dkim2) {
        this.dkim2 = dkim2;
    }

    /**
     * The dmarc verification record.
     */
    @Output
    public DomainDnsRecord getDmarc() {
        return dmarc;
    }

    public void setDmarc(DomainDnsRecord dmarc) {
        this.dmarc = dmarc;
    }

    /**
     * The domain verification record.
     */
    @Output
    public DomainDnsRecord getDomain() {
        return domain;
    }

    public void setDomain(DomainDnsRecord domain) {
        this.domain = domain;
    }

    @Override
    public void copyFrom(DomainPropertiesVerificationRecords model) {
        setDkim(null);
        if (model.dkim() != null) {
            DomainDnsRecord dkimRecord = new DomainDnsRecord();
            dkimRecord.copyFrom(model.dkim());
            setDkim(dkimRecord);
        }

        setSpf(null);
        if (model.spf() != null) {
            DomainDnsRecord spfRecord = new DomainDnsRecord();
            spfRecord.copyFrom(model.spf());
            setSpf(spfRecord);
        }

        setDkim2(null);
        if (model.dkim2() != null) {
            DomainDnsRecord dkim2Record = new DomainDnsRecord();
            dkim2Record.copyFrom(model.dkim2());
            setDkim2(dkim2Record);
        }

        setDmarc(null);
        if (model.dmarc() != null) {
            DomainDnsRecord dmarcRecord = new DomainDnsRecord();
            dmarcRecord.copyFrom(model.dmarc());
            setDmarc(dmarcRecord);
        }

        setDomain(null);
        if (model.domain() != null) {
            DomainDnsRecord domainRecord = new DomainDnsRecord();
            domainRecord.copyFrom(model.domain());
            setDomain(domainRecord);
        }

    }

    @Override
    public String primaryKey() {
        return "";
    }
}
