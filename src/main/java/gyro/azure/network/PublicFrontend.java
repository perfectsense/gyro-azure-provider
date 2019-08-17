package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;

import java.util.stream.Collectors;

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
     * The name of the Public Frontend. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Public IP Address associated with the Public Frontend. (Required)
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
        setInboundNatPool(publicFrontend.inboundNatPools().values().stream().map(o -> {
            InboundNatPool inboundNatPool = newSubresource(InboundNatPool.class);
            inboundNatPool.copyFrom(o);
            return inboundNatPool;
        }).collect(Collectors.toSet()));
        setInboundNatRule(publicFrontend.inboundNatRules().values().stream().map(o -> {
            InboundNatRule inboundNatRule = newSubresource(InboundNatRule.class);
            inboundNatRule.copyFrom(o);
            return inboundNatRule;
        }).collect(Collectors.toSet()));
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }
}