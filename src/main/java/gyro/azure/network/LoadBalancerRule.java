package gyro.azure.network;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.TransportProtocol;

/**
 * Creates a load balancer rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         load-balancer-rule
 *             load-balancer-rule-name: "test-rule-sat"
 *             backend-port: 80
 *             floating-ip: false
 *             frontend-name: "test-frontend"
 *             frontend-port: 443
 *             idle-timeout-in-minutes: 8
 *             protocol: "TCP"
 *             backend-pool-name: "backendpoolname"
 *             health-check-probe-name: "healthcheck-http"
 *         end
 */
public class LoadBalancerRule extends Diffable {

    private String backendPoolName;
    private Integer backendPort;
    private Boolean floatingIp;
    private String frontendName;
    private Integer frontendPort;
    private Integer idleTimeoutInMinutes;
    private String loadBalancerRuleName;
    private String healthCheckProbeName;
    private String protocol;

    public LoadBalancerRule() {}

    public LoadBalancerRule(LoadBalancingRule rule) {
        setBackendPoolName(rule.backend().name());
        setBackendPort(rule.backendPort());
        setFloatingIp(rule.floatingIPEnabled());
        setFrontendName(rule.frontend().name());
        setFrontendPort(rule.frontendPort());
        setIdleTimeoutInMinutes(rule.idleTimeoutInMinutes());
        setLoadBalancerRuleName(rule.name());
        setHealthCheckProbeName(rule.probe().name());
        setProtocol(rule.protocol() == TransportProtocol.TCP ? "TCP" : "UDP");
    }

    /**
     * The backend pool associated with the load balancer rule. (Required)
     */
    public String getBackendPoolName() {
        return backendPoolName;
    }

    public void setBackendPoolName(String backendPoolName) {
        this.backendPoolName = backendPoolName;
    }

    /**
     * The backend port that receives network traffic. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    /**
     * Determines whether floating ip support is enabled. Defaults to false (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getFloatingIp() {
        if (floatingIp == null) {
            floatingIp = false;
        }

        return floatingIp;
    }

    public void setFloatingIp(Boolean floatingIp) {
        this.floatingIp = floatingIp;
    }

    /**
     * The name of the frontend associated with the load balancer rule (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getFrontendName() {
        return frontendName;
    }

    public void setFrontendName(String frontendName) {
        this.frontendName = frontendName;
    }

    /**
     * The frontend port that receives network traffic. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getFrontendPort() {
        return frontendPort;
    }

    public void setFrontendPort(Integer frontendPort) {
        this.frontendPort = frontendPort;
    }

    /**
     * The number of minutes before an unresponsive connection is closed. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getIdleTimeoutInMinutes() {
        return idleTimeoutInMinutes;
    }

    public void setIdleTimeoutInMinutes(Integer idleTimeoutInMinutes) {
        this.idleTimeoutInMinutes = idleTimeoutInMinutes;
    }

    /**
     * The name of the load balancer rule. (Required)
     */
    public String getLoadBalancerRuleName() {
        return loadBalancerRuleName;
    }

    public void setLoadBalancerRuleName(String loadBalancerRuleName) {
        this.loadBalancerRuleName = loadBalancerRuleName;
    }

    /**
     * The health check probe associated with the load balancer rule. (Required)
     */
    public String getHealthCheckProbeName() {
        return healthCheckProbeName;
    }

    public void setHealthCheckProbeName(String healthCheckProbeName) {
        this.healthCheckProbeName = healthCheckProbeName;
    }

    /**
     * The protocol used by the load balancer rule. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
