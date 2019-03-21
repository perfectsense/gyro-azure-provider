package gyro.azure.network;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.TransportProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 *             target-network-ip-configuration
 *                 ip-configuration-name: "primary"
 *                 network-interface-id: $(azure::network-interface network-interface-example-lb | network-interface-id)
 *             end
 *         end
 */
public class InboundNatRule extends Diffable {

    private Integer backendPort;
    private Boolean floatingIp;
    private String frontendName;
    private Integer frontendPort;
    private String name;
    private String protocol;
    private TargetNetworkIpConfiguration targetNetworkIpConfiguration;

    public InboundNatRule() {}

    public InboundNatRule(LoadBalancerInboundNatRule natRule) {
        setBackendPort(natRule.backendPort());
        setFloatingIp(natRule.floatingIPEnabled());
        setFrontendName(natRule.frontend() != null ? natRule.frontend().name() : null);
        setFrontendPort(natRule.frontendPort());
        setName(natRule.name());
        setProtocol(natRule.protocol() == TransportProtocol.TCP ? "TCP" : "UDP");
        setTargetNetworkIpConfiguration(new TargetNetworkIpConfiguration(natRule.backendNicIPConfigurationName(), natRule.backendNetworkInterfaceId()));
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
     * The frontend associated with the inbound nat rule. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public Integer getFrontendPort() {
        return frontendPort;
    }

    public void setFrontendPort(Integer frontendPort) {
        this.frontendPort = frontendPort;
    }

    /**
     * The protocol used by the nat rule. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The target network ip configuration associated with the inbound nat rule. (Optional)
     */
    public TargetNetworkIpConfiguration getTargetNetworkIpConfiguration() {
        return targetNetworkIpConfiguration;
    }

    public void setTargetNetworkIpConfiguration(TargetNetworkIpConfiguration targetNetworkIpConfiguration) {
        this.targetNetworkIpConfiguration = targetNetworkIpConfiguration;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "inbound nat rule " + getName();
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

        InboundNatRule rule = (InboundNatRule) obj;

        return (rule.getName()).equals(this.getName());
    }
}
