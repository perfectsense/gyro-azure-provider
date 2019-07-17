package gyro.azure.dns;

import gyro.core.resource.Diffable;

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
public class CaaRecord extends Diffable {

    private Integer flags;
    private String tag;
    private String value;

    public CaaRecord() {}

    public CaaRecord(com.microsoft.azure.management.dns.CaaRecord caaRecord) {
        setFlags(caaRecord.flags());
        setTag(caaRecord.tag());
        setValue(caaRecord.value());
    }

    /**
     * The flags for the record. Valid values are integers between 0 and 255. (Required)
     */
    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    /**
     * The tag for the record. (Required)
     */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * The value for the record. (Required)
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return String.format("%d/%s/%s", getFlags(), getTag(), getValue());
    }

}
