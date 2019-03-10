package gyro.azure.network;

import gyro.core.diff.ResourceDiffProperty;

import com.microsoft.azure.management.network.LoadBalancerHttpProbe;

/**
 * Creates a http health check probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         health-check-probe-http
 *             health-check-probe-name: "healthcheck"
 *             interval: 8
 *             request-path: "/"
 *             port: 80
 *             probes: 3
 *             protocol: "TCP"
 *         end
 */
public class HealthCheckProbeHttp extends HealthCheckProbeTcp {

    private String requestPath;

    public HealthCheckProbeHttp() {}

    public HealthCheckProbeHttp(LoadBalancerHttpProbe httpProbe) {
        setHealthCheckProbeName(httpProbe.name());
        setInterval(httpProbe.intervalInSeconds());
        setRequestPath(httpProbe.requestPath());
        setPort(httpProbe.port());
        setProbes(httpProbe.numberOfProbes());
        setProtocol("HTTP");
    }

    /**
     * The HTTP request path by the probe to call to check the health status. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String primaryKey() {
        return String.format("%s", getHealthCheckProbeName());
    }

    @Override
    public String toDisplayString() {
        return "health check probe http" + getHealthCheckProbeName();
    }
}
