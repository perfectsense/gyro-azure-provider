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

import com.azure.resourcemanager.communication.models.DnsRecord;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;

public class DomainDnsRecord extends Diffable implements Copyable<DnsRecord> {
    private String name;
    private Integer ttl;
    private String type;
    private String value;

    /**
     * The name of the dns record.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ttl of the dns record.
     */
    @Output
    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    /**
     * The type of the dns record.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The value of the dns record.
     */
    @Output
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(DnsRecord model) {
        setName(model.name());
        setTtl(model.ttl());
        setType(model.type());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
