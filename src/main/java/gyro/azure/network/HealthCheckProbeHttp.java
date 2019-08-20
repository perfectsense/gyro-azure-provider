package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerHttpProbe;
import gyro.core.validation.Required;

/**
 * Creates a http health check probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    health-check-probe-http
 *        name: "healthcheck"
 *        interval: 8
 *        request-path: "/"
 *        port: 80
 *        probes: 3
 *    end
 */
public class HealthCheckProbeHttp extends AbstractHealthCheckProbe implements Copyable<LoadBalancerHttpProbe> {
    private String requestPath;

    /**
     * The HTTP request path by the probe to call to check the health status. (Required)
     */
    @Required
    @Updatable
    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    @Override
    public void copyFrom(LoadBalancerHttpProbe httpProbe) {
        setName(httpProbe.name());
        setInterval(httpProbe.intervalInSeconds());
        setRequestPath(httpProbe.requestPath());
        setPort(httpProbe.port());
        setProbes(httpProbe.numberOfProbes());
    }
}
