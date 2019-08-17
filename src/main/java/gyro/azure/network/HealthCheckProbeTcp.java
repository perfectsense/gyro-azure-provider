package gyro.azure.network;

import gyro.azure.Copyable;

import com.microsoft.azure.management.network.LoadBalancerTcpProbe;

/**
 * Creates a tcp health check probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    health-check-probe-tcp
 *        name: "healthcheck-tcp-test-sat"
 *        interval: 5
 *        port: 80
 *        probes: 2
 *    end
 */
public class HealthCheckProbeTcp extends AbstractHealthCheckProbe implements Copyable<LoadBalancerTcpProbe> {

    @Override
    public void copyFrom(LoadBalancerTcpProbe tcpProbe) {
        setName(tcpProbe.name());
        setInterval(tcpProbe.intervalInSeconds());
        setPort(tcpProbe.port());
        setProbes(tcpProbe.numberOfProbes());
    }
}