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
 *     mx-record-set
 *         name: "mxrecexample"
 *         time-to-live: 4
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
public class MxRecord extends Diffable implements Copyable<com.microsoft.azure.management.dns.MxRecord> {

    private String exchange;
    private Integer preference;

    /**
     * The mail exchange server's host name. (Required)
     */
    @Required
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    /**
     * The priority for the mail exchange host. The lower the value, the higher the priority. (Required)
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
    public void copyFrom(com.microsoft.azure.management.dns.MxRecord mxRecord) {
        setExchange(mxRecord.exchange());
        setPreference(mxRecord.preference());
    }

    @Override
    public String primaryKey() {
        return getExchange();
    }

}
