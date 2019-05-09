package gyro.azure.dns;

import gyro.core.resource.Diffable;

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

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String primaryKey() {
        return String.format("%d/%s/%s", getFlags(), getTag(), getValue());
    }

    public String toDisplayString() {
        return "caa record with flag " + getFlags() + ", tag " + getTag() + ", and value " + getValue();
    }
}
