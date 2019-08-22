package gyro.azure.dns;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

/**
 * Creates an SRV Record.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     srv-record
 *         port: 80
 *         priority: 1
 *         target: "testtarget.com"
 *         weight: 100
 *     end
 */
public class SrvRecord extends Diffable implements Copyable<com.microsoft.azure.management.dns.SrvRecord> {

    private Integer port;
    private Integer priority;
    private String target;
    private Integer weight;

    /**
     * The port on which the service is bounded. (Required)
     */
    @Required
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The priority of the target host. The lower the value, the higher the priority. (Required)
     */
    @Required
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * The canonical name of the target host. (Required)
     */
    @Required
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * The preference of the records with the same priority. The higher the value, the higher the preference. (Required)
     */
    @Required
    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public void copyFrom(com.microsoft.azure.management.dns.SrvRecord srvRecord) {
        setPort(srvRecord.port());
        setPriority(srvRecord.priority());
        setTarget(srvRecord.target());
        setWeight(srvRecord.weight());
    }

    @Override
    public String primaryKey() {
        return String.format("%d/%d/%s/%d",
                getPort(), getPriority(), getTarget(), getWeight());
    }

}
