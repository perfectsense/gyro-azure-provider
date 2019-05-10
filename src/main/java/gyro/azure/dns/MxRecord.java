package gyro.azure.dns;

import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

public class MxRecord extends Diffable {

    private String exchange;
    private Integer preference;

    public MxRecord() {}

    public MxRecord(com.microsoft.azure.management.dns.MxRecord mxRecord) {
        setExchange(mxRecord.exchange());
        setPreference(mxRecord.preference());
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @ResourceUpdatable
    public Integer getPreference() {
        return preference;
    }

    public void setPreference(Integer preference) {
        this.preference = preference;
    }

    public String primaryKey() {
        return String.format("%s", getExchange());
    }

    public String toDisplayString() {
        return "mx record with exchange " + getExchange() + " and preference " + getPreference();
    }
}
