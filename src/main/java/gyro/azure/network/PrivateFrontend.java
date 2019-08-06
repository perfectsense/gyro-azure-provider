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
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetworkId(NetworkResource network) {
        this.network = network;
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

    @Override
    public void copyFrom(LoadBalancerPrivateFrontend privateFrontend) {
        setName(privateFrontend.name());
        setPrivateIpAddress(privateFrontend.privateIPAddress());
        setSubnetName(privateFrontend.subnetName());
        setNetworkId(findById(NetworkResource.class, privateFrontend.networkId()));
        privateFrontend.inboundNatRules().forEach((key, value) -> getInboundNatRule().add(new InboundNatRule(value)));
        setInboundNatPool(privateFrontend.inboundNatPools().values().stream().map(o -> {
            InboundNatPool inboundNatPool = newSubresource(InboundNatPool.class);
            inboundNatPool.copyFrom(o);
            return inboundNatPool;
        }).collect(Collectors.toSet()));
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }
}
