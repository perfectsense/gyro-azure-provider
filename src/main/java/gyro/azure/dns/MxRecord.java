/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.azure.dns;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

/**
 * Creates an MX Record.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::mx-record-set mx-record-set
 *         name: "mxrecexample"
 *         ttl: 4
 *
 *         mx-record
 *             exchange: "mail.cont.com"
 *             preference: 1
 *         end
 *
 *         mx-record
 *             exchange: "mail.conto.com"
 *             preference: 2
 *         end
 *     end
 */
public class MxRecord extends Diffable implements Copyable<com.azure.resourcemanager.dns.models.MxRecord> {

    private String exchange;
    private Integer preference;

    /**
     * The mail exchange server's host name.
     */
    @Required
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    /**
     * The priority for the mail exchange host. The lower the value, the higher the priority.
     */
    @Required
    @Updatable
    public Integer getPreference() {
        return preference;
    }

    public void setPreference(Integer preference) {
        this.preference = preference;
    }

    @Override
    public void copyFrom(com.azure.resourcemanager.dns.models.MxRecord mxRecord) {
        setExchange(mxRecord.exchange());
        setPreference(mxRecord.preference());
    }

    @Override
    public String primaryKey() {
        return getExchange();
    }

}
