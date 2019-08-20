package gyro.azure.compute;

import gyro.azure.network.LoadBalancerResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

import java.util.Set;

public class LoadBalancerAttachment extends Diffable {
    private LoadBalancerResource loadBalancer;
    private Set<String> backends;
    private Set<String> inboundNatPools;

    /**
     * The Load Balancer to be attached as internal/public-internet type to a Scale Set.
     */
    @Required
    public LoadBalancerResource getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerResource loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * The Corresponding Load Balancer Backends.
     */
    @Required
    public Set<String> getBackends() {
        return backends;
    }

    public void setBackends(Set<String> backends) {
        this.backends = backends;
    }

    /**
     * The Corresponding Load Balancer Inbound Nat Pools.
     */
    @Required
    public Set<String> getInboundNatPools() {
        return inboundNatPools;
    }

    public void setInboundNatPools(Set<String> inboundNatPools) {
        this.inboundNatPools = inboundNatPools;
    }

    @Override
    public String primaryKey() {
        return "loadbalancer";
    }
}
