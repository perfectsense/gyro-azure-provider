package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;

/**
 * Creates a public frontend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         public-frontend
 *             name: "public-frontend-name"
 *             public-ip-address: $(azure::public-ip-address public-ip-address)
 *
 *             inbound-nat-rule
 *                 name: "test-nat-rule"
 *                 frontend-name: "public-frontend-name"
 *                 frontend-port: 80
 *                 protocol: "TCP"
 *             end
 *         end
 */
public class PublicFrontend extends Frontend implements Copyable<LoadBalancerPublicFrontend> {

    private String name;
    private PublicIpAddressResource publicIpAddress;

    /**
     * The name of the public frontend. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Public IP Address associated with the frontend. (Required)
     */
    @Updatable
    public PublicIpAddressResource getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(PublicIpAddressResource publicIpAddressName) {
        this.publicIpAddress = publicIpAddressName;
    }

    @Override
    public void copyFrom(LoadBalancerPublicFrontend publicFrontend) {
        setName(publicFrontend.name());
        setPublicIpAddress(findById(PublicIpAddressResource.class, publicFrontend.getPublicIPAddress().id()));
        publicFrontend.inboundNatPools().forEach((key, value) -> getInboundNatPool().add(new InboundNatPool(value)));
        publicFrontend.inboundNatRules().forEach((key, value) -> getInboundNatRule().add(new InboundNatRule(value)));
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }
}
