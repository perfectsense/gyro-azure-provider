package gyro.azure.network;

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
 *             public-frontend-name: "public-frontend-name"
 *             public-ip-address-name: $(azure::public-ip-address public-ip-address| public-ip-address-name)
 *
 *             inbound-nat-rule
 *                 inbound-nat-rule-name: "test-nat-rule"
 *                 frontend-name: "public-frontend-name"
 *                 frontend-port: 80
 *                 protocol: "TCP"
 *             end
 *         end
 */
public class PublicFrontend extends Frontend {

    private String publicFrontendName;
    private String publicIpAddressName;

    public PublicFrontend(){

    }

    public PublicFrontend(LoadBalancerPublicFrontend publicFrontend) {
        setPublicFrontendName(publicFrontend.name());
        setPublicIpAddressName(publicFrontend.getPublicIPAddress().name());
        publicFrontend.inboundNatPools().entrySet().stream()
                .forEach(pool -> getInboundNatPool().add(new InboundNatPool(pool.getValue())));
        publicFrontend.inboundNatRules().entrySet().stream()
                .forEach(rule -> getInboundNatRule().add(new InboundNatRule(rule.getValue())));
    }

    /**
     * The name of the public frontend. (Required)
     */
    public String getPublicFrontendName() {
        return publicFrontendName;
    }

    public void setPublicFrontendName(String publicFrontendName) {
        this.publicFrontendName = publicFrontendName;
    }

    /**
     * The name of the public ip address associated with the frontend. (Required)
     */
    public String getPublicIpAddressName() {
        return publicIpAddressName;
    }

    public void setPublicIpAddressName(String publicIpAddressName) {
        this.publicIpAddressName = publicIpAddressName;
    }

    public String primaryKey() {
        return String.format("%s/%s", getPublicFrontendName(), getPublicIpAddressName());
    }

    @Override
    public String toDisplayString() {
        return "public frontend " + getPublicFrontendName();
    }
}
