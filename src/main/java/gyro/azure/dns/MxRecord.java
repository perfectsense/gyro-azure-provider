package gyro.azure.dns;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

/**
 * Creates an MX Record.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     mx-record-set
 *         name: "mxrecexample"
 *         time-to-live: "4"
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
public class MxRecord extends Diffable {

    private String exchange;
    private Integer preference;

    public MxRecord() {}

    public MxRecord(com.microsoft.azure.management.dns.MxRecord mxRecord) {
        setExchange(mxRecord.exchange());
        setPreference(mxRecord.preference());
    }

    /**
     * The mail exchange server's host name. (Required)
     */
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    /**
     * The priority for the mail exchange host. The lower the value, the higher the priority. (Required)
     */
    @Updatable
    public Integer getPreference() {
        return preference;
    }

    public void setPreference(Integer preference) {
        this.preference = preference;
    }

    @Override
    public String primaryKey() {
        return exchange;
    }

    @Override
    public String toDisplayString() {
        return "mx record with exchange " + getExchange() + " and preference " + getPreference();
    }
}
