package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;

import java.util.stream.Collectors;

/**
 * Creates a private frontend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    private-frontend
 *        name: "private-frontend"
 *        network: $(azure::network load-balancer-network-example)
 *        subnet-name: "subnet2"
 *
 *        inbound-nat-pool
 *            name: "test-nat-pool"
 *            frontend-name: "test-frontend"
 *            backend-port: 80
 *            protocol: "TCP"
 *            frontend-port-range-start: 80
 *            frontend-port-range-end: 89
 *        end
 *    end
 */
public class PrivateFrontend extends Frontend implements Copyable<LoadBalancerPrivateFrontend> {
    private String name;
    private String privateIpAddress;
    private String subnetName;
    private NetworkResource network;

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
     * The Private IP Address associated with the private frontend. (Optional)
     */
    @Updatable
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The network where the subnet is found. (Required)
     */
    @Updatable
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The name of the subnet that is associated with the Private Frontend. (Required)
     */
    @Updatable
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    @Override
    public void copyFrom(LoadBalancerPrivateFrontend privateFrontend) {
        setName(privateFrontend.name());
        setPrivateIpAddress(privateFrontend.privateIPAddress());
        setSubnetName(privateFrontend.subnetName());
        setNetwork(findById(NetworkResource.class, privateFrontend.networkId()));
        setInboundNatPool(privateFrontend.inboundNatPools().values().stream().map(o -> {
            InboundNatPool inboundNatPool = newSubresource(InboundNatPool.class);
            inboundNatPool.copyFrom(o);
            return inboundNatPool;
        }).collect(Collectors.toSet()));
        setInboundNatRule(privateFrontend.inboundNatRules().values().stream().map(o -> {
            InboundNatRule inboundNatRule = newSubresource(InboundNatRule.class);
            inboundNatRule.copyFrom(o);
            return inboundNatRule;
        }).collect(Collectors.toSet()));
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }
}
