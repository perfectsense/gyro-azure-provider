/*
 * Copyright 2024, Brightspot, Inc.
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

import com.azure.resourcemanager.communication.models.DomainPropertiesVerificationStates;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;

public class VerificationStates extends Diffable implements Copyable<DomainPropertiesVerificationStates> {

    private DomainVerificationStatusRecord dkim;
    private DomainVerificationStatusRecord spf;
    private DomainVerificationStatusRecord dkim2;
    private DomainVerificationStatusRecord dmarc;
    private DomainVerificationStatusRecord domain;

    /**
     * The dkim verification state record.
     */
    @Output
    public DomainVerificationStatusRecord getDkim() {
        return dkim;
    }

    public void setDkim(DomainVerificationStatusRecord dkim) {
        this.dkim = dkim;
    }

    /**
     * The spf verification state record.
     */
    @Output
    public DomainVerificationStatusRecord getSpf() {
        return spf;
    }

    public void setSpf(DomainVerificationStatusRecord spf) {
        this.spf = spf;
    }

    /**
     * The dkim2 verification state record.
     */
    @Output
    public DomainVerificationStatusRecord getDkim2() {
        return dkim2;
    }

    public void setDkim2(DomainVerificationStatusRecord dkim2) {
        this.dkim2 = dkim2;
    }

    /**
     * The dmarc verification state record.
     */
    @Output
    public DomainVerificationStatusRecord getDmarc() {
        return dmarc;
    }

    public void setDmarc(DomainVerificationStatusRecord dmarc) {
        this.dmarc = dmarc;
    }

    /**
     * The domain verification state record.
     */
    @Output
    public DomainVerificationStatusRecord getDomain() {
        return domain;
    }

    public void setDomain(DomainVerificationStatusRecord domain) {
        this.domain = domain;
    }

    @Override
    public void copyFrom(DomainPropertiesVerificationStates model) {
        setDkim(null);
        if (model.dkim() != null) {
            DomainVerificationStatusRecord dkimRecord = new DomainVerificationStatusRecord();
            dkimRecord.copyFrom(model.dkim());
            setDkim(dkimRecord);
        }

        setSpf(null);
        if (model.spf() != null) {
            DomainVerificationStatusRecord spfRecord = new DomainVerificationStatusRecord();
            spfRecord.copyFrom(model.spf());
            setSpf(spfRecord);
        }

        setDkim2(null);
        if (model.dkim2() != null) {
            DomainVerificationStatusRecord dkim2Record = new DomainVerificationStatusRecord();
            dkim2Record.copyFrom(model.dkim2());
            setDkim2(dkim2Record);
        }

        setDmarc(null);
        if (model.dmarc() != null) {
            DomainVerificationStatusRecord dmarcRecord = new DomainVerificationStatusRecord();
            dmarcRecord.copyFrom(model.dmarc());
            setDmarc(dmarcRecord);
        }

        setDomain(null);
        if (model.domain() != null) {
            DomainVerificationStatusRecord domainRecord = new DomainVerificationStatusRecord();
            domainRecord.copyFrom(model.domain());
            setDomain(domainRecord);
        }

    }

    @Override
    public String primaryKey() {
        return "";
    }
}
