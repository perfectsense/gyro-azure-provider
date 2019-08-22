package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;

import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import gyro.core.validation.Required;

/**
 * Creates a nic nat rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    nic-nat-rule
 *        load-balancer: $(azure::load-balancer load-balancer-example)
 *        inbound-nat-rule-name: "test-nat-rule"
 *    end
 */
public class NicNatRule extends Diffable implements Copyable<LoadBalancerInboundNatRule> {
    private LoadBalancerResource loadBalancer;
    private String inboundNatRuleName;

    /**
     * The Load Balancer to associate the IP Configuration to. (Required)
     */
    @Required
    public LoadBalancerResource getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerResource loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * The name of the Inbound Nat Rule present on the Load Balancer to associate with the IP configuration. (Required)
     */
    @Required
    public String getInboundNatRuleName() {
        return inboundNatRuleName;
    }

    public void setInboundNatRuleName(String inboundNatRuleName) {
        this.inboundNatRuleName = inboundNatRuleName;
    }

    @Override
    public void copyFrom(LoadBalancerInboundNatRule rule) {
        setLoadBalancer(findById(LoadBalancerResource.class, rule.parent().id()));
        setInboundNatRuleName(rule.name());
    }

    public String primaryKey() {
        return String.format("%s %s", getLoadBalancer().getName(), getInboundNatRuleName());
    }
}
