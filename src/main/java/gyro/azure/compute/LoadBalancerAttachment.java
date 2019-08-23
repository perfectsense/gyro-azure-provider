package gyro.azure.compute;

import gyro.azure.network.LoadBalancerResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

import java.util.HashSet;
import java.util.Set;

public class LoadBalancerAttachment extends Diffable {
    private LoadBalancerResource loadBalancer;
    private Set<String> backends;
    private Set<String> inboundNatPools;

    /**
     * The Load Balancer to be attached as internal/public-internet type to a Scale Set. (Required)
     */
    @Required
    @Updatable
    public LoadBalancerResource getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerResource loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * The corresponding Load Balancer Backends. (Required)
     */
    @Required
    @Updatable
    public Set<String> getBackends() {
        if (backends == null) {
            backends = new HashSet<>();
        }

        return backends;
    }

    public void setBackends(Set<String> backends) {
        this.backends = backends;
    }

    /**
     * The corresponding Load Balancer Inbound Nat Pools.
     */
    @Updatable
    public Set<String> getInboundNatPools() {
        if (inboundNatPools == null) {
            inboundNatPools = new HashSet<>();
        }

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
