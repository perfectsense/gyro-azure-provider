package gyro.azure.network;

import gyro.core.diff.Diffable;

public class LoadBalancerRule extends Diffable {

    private BackendPool backendPool;
    private Integer backendPort;
    private FrontendIpConfiguration frontendIpConfiguration;
    private Integer frontendPort;
    private Integer idleTimeoutInMinutes;
    private String loadBalancerRuleName;
    private HealthCheckProbe healthCheckProbe;
    private String protocol;

    public BackendPool getBackendPool() {
        return backendPool;
    }

    public void setBackendPool(BackendPool backendPool) {
        this.backendPool = backendPool;
    }

    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    public FrontendIpConfiguration getFrontendIpConfiguration() {
        return frontendIpConfiguration;
    }

    public void setFrontendIpConfiguration(FrontendIpConfiguration frontendIpConfiguration) {
        this.frontendIpConfiguration = frontendIpConfiguration;
    }

    public Integer getFrontendPort() {
        return frontendPort;
    }

    public void setFrontendPort(Integer frontendPort) {
        this.frontendPort = frontendPort;
    }

    public Integer getIdleTimeoutInMinutes() {
        return idleTimeoutInMinutes;
    }

    public void setIdleTimeoutInMinutes(Integer idleTimeoutInMinutes) {
        this.idleTimeoutInMinutes = idleTimeoutInMinutes;
    }

    public String getLoadBalancerRuleName() {
        return loadBalancerRuleName;
    }

    public void setLoadBalancerRuleName(String loadBalancerRuleName) {
        this.loadBalancerRuleName = loadBalancerRuleName;
    }

    public HealthCheckProbe getHealthCheckProbe() {
        return healthCheckProbe;
    }

    public void setHealthCheckProbe(HealthCheckProbe healthCheckProbe) {
        this.healthCheckProbe = healthCheckProbe;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String primaryKey() {
        return String.format("%s", getLoadBalancerRuleName());
    }

    @Override
    public String toDisplayString() {
        return "load balancer rule" + getLoadBalancerRuleName();
    }
}
