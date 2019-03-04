package gyro.azure.network;

import gyro.core.diff.Diffable;


public class HealthCheckProbe extends Diffable {

    private String healthProbeName;
    private Integer interval;
    private String path;
    private Integer port;
    private Integer probes;
    private String protocol;

    /**
     * The name of the health probe. (Required)
     */
    public String getHealthProbeName() {
        return healthProbeName;
    }

    public void setHealthProbeName(String healthProbeName) {
        this.healthProbeName = healthProbeName;
    }

    /**
     * The amount of time between probes. (Required)
     */
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * Only needed for HTTP. (Optional)
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The destination port used for a probe. (Required)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Health probe failures required by an unhealthy target to be considered unhealthy (Required)
     */
    public Integer getProbes() {
        return probes;
    }

    public void setProbes(Integer probes) {
        this.probes = probes;
    }

    /**
     * The protocol used for the probe (Required)
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String primaryKey() {
        return String.format("%s", getHealthProbeName());
    }

    @Override
    public String toDisplayString() {
        return "health check probe " + getHealthProbeName();
    }
}
