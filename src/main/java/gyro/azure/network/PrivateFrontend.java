package gyro.azure.network;

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
 *             private-frontend-name: "private-frontend"
 *             network-id: $(azure::network load-balancer-network-example | network-id)
 *             subnet-name: "subnet2"
 *
 *             inbound-nat-pool
 *                 inbound-nat-pool-name: "test-nat-pool"
 *                 frontend-name: "test-frontend"
 *                 backend-port: 80
 *                 protocol: "TCP"
 *                 frontend-port-range-start: 80
 *                 frontend-port-range-end: 89
 *             end
 *         end
 */
public class PrivateFrontend extends Frontend {

    private String privateFrontendName;
    private String privateIpAddress;
    private String subnetName;
    private String networkId;

    public PrivateFrontend() {

    }

    public PrivateFrontend(LoadBalancerPrivateFrontend privateFrontend) {
        setPrivateFrontendName(privateFrontend.name());
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
    public String getPrivateFrontendName() {
        return privateFrontendName;
    }

    public void setPrivateFrontendName(String privateFrontendName) {
        this.privateFrontendName = privateFrontendName;
    }

    /**
     * The name of the private ip address associated with the private frontend. (Optional)
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The id of the network where the subnet is found. (Required)
     */
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * The name of the subnet that is associated with the private frontend. (Required)
     */
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getPrivateFrontendName(), getNetworkId(), getSubnetName());
    }

    @Override
    public String toDisplayString() {
        return "private frontend " + getPrivateFrontendName();
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

        PrivateFrontend privateFrontend = (PrivateFrontend) obj;

        return (privateFrontend.getName()).equals(this.getName());
    }
}
