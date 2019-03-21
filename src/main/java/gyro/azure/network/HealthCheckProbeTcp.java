package gyro.azure.network;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

import com.microsoft.azure.management.network.LoadBalancerTcpProbe;

/**
 * Creates a tcp health check probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         health-check-probe-tcp
 *             name: "healthcheck-tcp-test-sat"
 *             interval: 5
 *             port: 80
 *             probes: 2
 *         end
 */
public class HealthCheckProbeTcp extends Diffable {

    private String name;
    private Integer interval;
    private Integer port;
    private Integer probes;

    public HealthCheckProbeTcp() {

    }

    public HealthCheckProbeTcp(LoadBalancerTcpProbe tcpProbe) {
        setName(tcpProbe.name());
        setInterval(tcpProbe.intervalInSeconds());
        setPort(tcpProbe.port());
        setProbes(tcpProbe.numberOfProbes());
    }

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
    @ResourceDiffProperty(updatable = true)
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * The destination port used for a probe. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Health probe failures required by an unhealthy target to be considered unhealthy (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getProbes() {
        return probes;
    }

    public void setProbes(Integer probes) {
        this.probes = probes;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "health check probe tcp " + getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        HealthCheckProbeTcp probe = (HealthCheckProbeTcp) obj;

        return (probe.getName()).equals(this.getName());
    }
}