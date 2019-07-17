package gyro.azure.dns;

import gyro.core.resource.Diffable;

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
public class SrvRecord extends Diffable {

    private Integer port;
    private Integer priority;
    private String target;
    private Integer weight;

    public SrvRecord() {}

    public SrvRecord(com.microsoft.azure.management.dns.SrvRecord srvRecord) {
        setPort(srvRecord.port());
        setPriority(srvRecord.priority());
        setTarget(srvRecord.target());
        setWeight(srvRecord.weight());
    }

    /**
     * The port on which the service is bounded. (Required)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The priority of the target host. The lower the value, the higher the priority. (Required)
     */
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * The canonical name of the target host. (Required)
     */
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * The preference of the records with the same priority. The higher the value, the higher the preference. (Required)
     */
    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String primaryKey() {
        return String.format("%d/%d/%s/%d",
                getPort(), getPriority(), getTarget(), getWeight());
    }

}
