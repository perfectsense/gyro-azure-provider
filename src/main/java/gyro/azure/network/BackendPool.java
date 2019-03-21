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
 *             name: "backendpoolname"
 *             target-network-ip-configuration
 *                 ip-configuration-name: "primary"
 *                 network-id: $(azure::network-interface network-interface-example-lb | network-interface-id)
 *             end
 *             virtual-machine-ids: [$(azure::virtual-machine virtual-machine-example-lb | virtual-machine-id)]
 *         end
 */
public class BackendPool extends Diffable {

    private String name;
    private List<TargetNetworkIpConfiguration> targetNetworkIpConfiguration;
    private List<String> virtualMachineIds;

    public BackendPool(){

    }

    public BackendPool(LoadBalancerBackend backend, List<TargetNetworkIpConfiguration> configurations) {
        setName(backend.name());
        configurations.forEach((config) -> getTargetNetworkIpConfiguration().add(new TargetNetworkIpConfiguration(config.getIpConfigurationName(), config.getNetworkInterfaceId())));
        setVirtualMachineIds(new ArrayList<>(backend.getVirtualMachineIds()));
    }

    /**
     * The name of the backend pool. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The target network ip configurations associated with the backend pool. (Required)
     */
    public List<TargetNetworkIpConfiguration> getTargetNetworkIpConfiguration() {
        if (targetNetworkIpConfiguration == null) {
            targetNetworkIpConfiguration = new ArrayList<>();
        }
        return targetNetworkIpConfiguration;
    }

    public void setTargetNetworkIpConfiguration(List<TargetNetworkIpConfiguration> targetNetworkIpConfiguration) {
        this.targetNetworkIpConfiguration = targetNetworkIpConfiguration;
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
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "backend pool " + getName();
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

        BackendPool pool = (BackendPool) obj;

        return (pool.getName()).equals(this.getName());
    }
}
