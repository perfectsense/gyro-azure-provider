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
import gyro.core.validation.Range;
import gyro.core.validation.Required;

/**
 * Creates an CAA Record.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     caa-record
 *         flags: 1
 *         tag: "tag1"
 *         value: "val1"
 *     end
 */
public class CaaRecord extends Diffable implements Copyable<com.azure.resourcemanager.dns.models.CaaRecord> {

    private Integer flags;
    private String tag;
    private String value;

    /**
     * The flags for the record.
     */
    @Required
    @Range(min = 0, max = 255)
    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    /**
     * The tag for the record.
     */
    @Required
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * The value for the record.
     */
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(com.azure.resourcemanager.dns.models.CaaRecord caaRecord) {
        setFlags(caaRecord.flags());
        setTag(caaRecord.tag());
        setValue(caaRecord.value());
    }

    @Override
    public String primaryKey() {
        return String.format("%d/%s/%s", getFlags(), getTag(), getValue());
    }

}
