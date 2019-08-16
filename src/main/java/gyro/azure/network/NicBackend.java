package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;

import com.microsoft.azure.management.network.LoadBalancerBackend;
import gyro.core.validation.Required;

/**
 * Creates a nic backend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    nic-backend
 *        load-balancer: $(azure::load-balancer load-balancer-example)
 *        backend-name: "backend-pool-one"
 *    end
 */
public class NicBackend extends Diffable implements Copyable<LoadBalancerBackend> {

    private String backendName;
    private LoadBalancerResource loadBalancer;

    /**
     * The name of the backend pool present on the Load Balancer to associate with the IP configuration. (Required)
     */
    @Required
    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

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

    @Override
    public void copyFrom(LoadBalancerBackend backend) {
        setBackendName(backend.name());
        setLoadBalancer(findById(LoadBalancerResource.class, backend.parent().id()));
    }

    public String primaryKey() {
        return String.format("%s %s", getLoadBalancer().getName(), getBackendName());
    }
}