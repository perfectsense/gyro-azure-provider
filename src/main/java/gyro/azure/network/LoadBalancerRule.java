package gyro.azure.network;

import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;

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
 *             name: "test-rule"
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
    private String name;
    private String healthCheckProbeName;
    private String protocol;

    public LoadBalancerRule() {}

    public LoadBalancerRule(LoadBalancingRule rule) {
        setBackendPoolName(rule.backend() != null ? rule.backend().name() : null);
        setBackendPort(rule.backendPort());
        setFloatingIp(rule.floatingIPEnabled());
        setFrontendName(rule.frontend() != null ? rule.frontend().name() : null);
        setFrontendPort(rule.frontendPort());
        setIdleTimeoutInMinutes(rule.idleTimeoutInMinutes());
        setName(rule.name());
        setHealthCheckProbeName(rule.probe() != null ? rule.probe().name() : null);
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The health check probe associated with the load balancer rule. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "load balancer rule " + getName();
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

        LoadBalancerRule rule = (LoadBalancerRule) obj;

        return (rule.getName()).equals(this.getName());
    }
}
