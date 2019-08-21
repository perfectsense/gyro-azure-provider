package gyro.azure.dns;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
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
public class CaaRecord extends Diffable implements Copyable<com.microsoft.azure.management.dns.CaaRecord> {

    private Integer flags;
    private String tag;
    private String value;

    /**
     * The flags for the record. Valid values are integers between 0 and 255. (Required)
     */
    @Required
    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    /**
     * The tag for the record. (Required)
     */
    @Required
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * The value for the record. (Required)
     */
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(com.microsoft.azure.management.dns.CaaRecord caaRecord) {
        setFlags(caaRecord.flags());
        setTag(caaRecord.tag());
        setValue(caaRecord.value());
    }

    @Override
    public String primaryKey() {
        return String.format("%d/%s/%s", getFlags(), getTag(), getValue());
    }

}
