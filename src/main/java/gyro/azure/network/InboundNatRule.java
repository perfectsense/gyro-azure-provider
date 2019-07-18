package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.TransportProtocol;

/**
 * Creates a nat rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         inbound-nat-rule
 *             name: "test-nat-rule"
 *             frontend-name: "test-frontend"
 *             frontend-port: 80
 *             protocol: "TCP"
 *         end
 */
public class InboundNatRule extends Diffable {

    private Integer backendPort;
    private Boolean floatingIp;
    private String frontendName;
    private Integer frontendPort;
    private String name;
    private String protocol;

    public InboundNatRule() {}

    public InboundNatRule(LoadBalancerInboundNatRule natRule) {
        setBackendPort(natRule.backendPort());
        setFloatingIp(natRule.floatingIPEnabled());
        setFrontendName(natRule.frontend() != null ? natRule.frontend().name() : null);
        setFrontendPort(natRule.frontendPort());
        setName(natRule.name());
        setProtocol(natRule.protocol() == TransportProtocol.TCP ? "TCP" : "UDP");
    }

    /**
     * The backend port that receives network traffic. (Required)
     */
    @Updatable
    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    /**
     * Determines whether floating ip support is enabled. Defaults to false (Required)
     */
    @Updatable
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
     * The frontend associated with the inbound nat rule. (Required)
     */
    @Updatable
    public String getFrontendName() {
        return frontendName;
    }

    public void setFrontendName(String frontendName) {
        this.frontendName = frontendName;
    }

    /**
     * The name of the inbound nat rule. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The frontend port that receives network traffic. (Required)
     */
    @Updatable
    public Integer getFrontendPort() {
        return frontendPort;
    }

    public void setFrontendPort(Integer frontendPort) {
        this.frontendPort = frontendPort;
    }

    /**
     * The protocol used by the nat rule. (Required)
     */
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

}
