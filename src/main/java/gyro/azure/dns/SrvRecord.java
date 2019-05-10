package gyro.azure.dns;

import gyro.core.resource.Diffable;

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String primaryKey() {
        return String.format("%d/%d/%s/%d",
                getPort(), getPriority(), getTarget(), getWeight());
    }

    public String toDisplayString() {
        return "srv record ";
    }
}
