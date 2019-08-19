package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public abstract class AbstractHealthCheckProbe extends Diffable {
    private String name;
    private Integer interval;
    private Integer port;
    private Integer probes;

    /**
     * The name of the Health Probe. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The amount of time before a Health Probe signals unhealthy. (Required)
     */
    @Required
    @Updatable
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * The destination port used for the Health Probe. (Required)
     */
    @Required
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The number of Health probe failures required by an unhealthy target to be considered unhealthy. (Required)
     */
    @Required
    @Updatable
    public Integer getProbes() {
        return probes;
    }

    public void setProbes(Integer probes) {
        this.probes = probes;
    }

    public String primaryKey() {
        return getName();
    }
}
