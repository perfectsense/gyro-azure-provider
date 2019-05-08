package gyro.azure.network;

import gyro.core.resource.Diffable;

import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;

/**
 * Creates a nic nat rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         nic-nat-rule
 *             load-balancer-name: $(azure::load-balancer load-balancer-example | name)
 *             nat-rule-name: "test-nat-rule"
 *         end
 */
public class NicNatRule extends Diffable {

    private String loadBalancerName;
    private String natRuleName;

    public NicNatRule() {}

    public NicNatRule(LoadBalancerInboundNatRule rule) {
        setLoadBalancerName(rule.parent().name());
        setNatRuleName(rule.name());
    }

    /**
     * The name of the load balancer to associate the configuration to. (Required)
     */
    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    /**
     * The name of the nat rule to associate the configuration to. (Required)
     */
    public String getNatRuleName() {
        return natRuleName;
    }

    public void setNatRuleName(String natRuleName) {
        this.natRuleName = natRuleName;
    }

    public String primaryKey() {
        return String.format("%s/%s", getLoadBalancerName(), getNatRuleName());
    }

    public String toDisplayString() {
        return "attachment of nic ip configuration to nat rule "
                + getNatRuleName()
                + " associated with load balancer "
                + getLoadBalancerName();
    }
}
