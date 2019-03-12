package gyro.azure.network;

import gyro.core.diff.Diffable;

import com.microsoft.azure.management.network.LoadBalancerBackend;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a backend pool.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         backend-pool
 *             backend-pool-name: "backendpoolname"
 *             virtual-machine-ids: [$(azure::virtual-machine virtual-machine-example-lb | virtual-machine-id)]
 *         end
 */
public class BackendPool extends Diffable {

    private String backendPoolName;
    private List<String> virtualMachineIds;

    public BackendPool(){

    }

    public BackendPool(LoadBalancerBackend backend) {
        setBackendPoolName(backendPoolName);
        setVirtualMachineIds(new ArrayList<>(backend.getVirtualMachineIds()));
    }

    /**
     * The name of the backend pool. (Required)
     */
    public String getBackendPoolName() {
        return backendPoolName;
    }

    public void setBackendPoolName(String backendPoolName) {
        this.backendPoolName = backendPoolName;
    }

    /**
     * The virtual machine ids associated with backend pool. (Required)
     */
    public List<String> getVirtualMachineIds() {
        return virtualMachineIds;
    }

    public void setVirtualMachineIds(List<String> virtualMachineIds) {
        this.virtualMachineIds = virtualMachineIds;
    }

    public String primaryKey() {
        return String.format("%s", getBackendPoolName());
    }

    @Override
    public String toDisplayString() {
        return "backend pool " + getBackendPoolName();
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

        BackendPool pool = (BackendPool) obj;

        return (pool.getName()).equals(this.getName());
    }
}
