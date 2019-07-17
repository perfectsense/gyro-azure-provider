package gyro.azure.network;

import gyro.core.resource.Updatable;

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
 *             name: "healthcheck"
 *             interval: 8
 *             request-path: "/"
 *             port: 80
 *             probes: 3
 *         end
 */
public class HealthCheckProbeHttp extends HealthCheckProbeTcp {

    private String requestPath;

    public HealthCheckProbeHttp() {}

    public HealthCheckProbeHttp(LoadBalancerHttpProbe httpProbe) {
        setName(httpProbe.name());
        setInterval(httpProbe.intervalInSeconds());
        setRequestPath(httpProbe.requestPath());
        setPort(httpProbe.port());
        setProbes(httpProbe.numberOfProbes());
    }

    /**
     * The HTTP request path by the probe to call to check the health status. (Required)
     */
    @Updatable
    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

}
