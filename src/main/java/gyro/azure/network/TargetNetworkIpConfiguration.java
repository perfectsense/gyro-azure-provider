package gyro.azure.network;

import gyro.core.diff.Diffable;

/**
 * Creates a target network configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *          target-network-ip-configuration
 *              ip-configuration-name: "primary"
 *              network-interface-id: $(azure::network-interface network-interface-example-lb | network-interface-id)
 *          end
 */
public class TargetNetworkIpConfiguration extends Diffable {

    private String ipConfigurationName;
    private String networkInterfaceId;

    public TargetNetworkIpConfiguration(){

    }

    public TargetNetworkIpConfiguration(String ipConfigurationName, String networkInterfaceId){
        setIpConfigurationName(ipConfigurationName);
        setNetworkInterfaceId(networkInterfaceId);
    }

    /**
     * The name of the ip configuration. (Required)
     */
    public String getIpConfigurationName() {
        return ipConfigurationName;
    }

    public void setIpConfigurationName(String ipConfigurationName) {
        this.ipConfigurationName = ipConfigurationName;
    }

    /**
     * The id of the corresponding network interface. (Required)
     */
    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    public String primaryKey() {
        return String.format("%s/%s", getIpConfigurationName(), getNetworkInterfaceId());
    }

    @Override
    public String toDisplayString() {
        return "target network ip configuration " + getIpConfigurationName() + " from network interface" + getNetworkInterfaceId();
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

        TargetNetworkIpConfiguration config = (TargetNetworkIpConfiguration) obj;

        return (config.getIpConfigurationName()).equals(this.getIpConfigurationName())
                && (config.getNetworkInterfaceId()).equals(this.getNetworkInterfaceId());
    }
}
