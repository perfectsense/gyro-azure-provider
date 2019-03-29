package gyro.azure.network;

import gyro.core.diff.Diffable;

import com.microsoft.azure.management.network.LoadBalancerBackend;

/**
 * Creates a nic backend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         nic-backend
 *             load-balancer-name: "load-balancer-example"
 *             backend-pool-name: "backend-pool-one"
 *         end
 */
public class NicBackend extends Diffable {

    private String backendPoolName;
    private String loadBalancerName;

    public NicBackend() {}

    public NicBackend(LoadBalancerBackend backend) {
        setBackendPoolName(backend.name());
        setLoadBalancerName(backend.parent().name());
    }

    /**
     * The name of the backend pool to associate the configuration to. (Required)
     */
    public String getBackendPoolName() {
        return backendPoolName;
    }

    public void setBackendPoolName(String backendPoolName) {
        this.backendPoolName = backendPoolName;
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

    public String primaryKey() {
        return String.format("%s/%s", getLoadBalancerName(), getBackendPoolName());
    }

    public String toDisplayString() {
        return "attachment of nic ip configuration to backend pool "
                + getBackendPoolName()
                + " associated with load balancer "
                + getLoadBalancerName();
    }
}
