package gyro.azure.network;

import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;

/**
 * Creates a private frontend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         private-frontend
 *             name: "private-frontend"
 *             network-id: $(azure::network load-balancer-network-example | network-id)
 *             subnet-name: "subnet2"
 *
 *             inbound-nat-pool
 *                 name: "test-nat-pool"
 *                 frontend-name: "test-frontend"
 *                 backend-port: 80
 *                 protocol: "TCP"
 *                 frontend-port-range-start: 80
 *                 frontend-port-range-end: 89
 *             end
 *         end
 */
public class PrivateFrontend extends Frontend {

    private String name;
    private String privateIpAddress;
    private String subnetName;
    private String networkId;

    public PrivateFrontend() {

    }

    public PrivateFrontend(LoadBalancerPrivateFrontend privateFrontend) {
        setName(privateFrontend.name());
        setPrivateIpAddress(privateFrontend.privateIPAddress());
        setSubnetName(privateFrontend.subnetName());
        setNetworkId(privateFrontend.networkId());
        privateFrontend.inboundNatPools().entrySet().stream()
                .forEach(pool -> getInboundNatPool().add(new InboundNatPool(pool.getValue())));
        privateFrontend.inboundNatRules().entrySet().stream()
                .forEach(rule -> getInboundNatRule().add(new InboundNatRule(rule.getValue())));
    }

    /**
     * The name of the private frontend. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the private ip address associated with the private frontend. (Optional)
     */
    @Updatable
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The id of the network where the subnet is found. (Required)
     */
    @Updatable
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * The name of the subnet that is associated with the private frontend. (Required)
     */
    @Updatable
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "private frontend " + getName();
    }

}
