package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public abstract class AbstractHealthCheckProbe extends Diffable {
    private String name;
    private Integer interval;
    private Integer port;
    private Integer probes;

    /**
     * The name of the health probe. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The amount of time between probes. (Required)
     */
    @Updatable
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * The destination port used for a probe. (Required)
     */
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Health probe failures required by an unhealthy target to be considered unhealthy. (Required)
     */
    @Updatable
    public Integer getProbes() {
        return probes;
    }

    public void setProbes(Integer probes) {
        this.probes = probes;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }
}
