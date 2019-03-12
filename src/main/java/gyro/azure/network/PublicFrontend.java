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
 *             name: "public-frontend-name"
 *             public-ip-address-name: $(azure::public-ip-address public-ip-address| public-ip-address-name)
 *
 *             inbound-nat-rule
 *                 name: "test-nat-rule"
 *                 frontend-name: "public-frontend-name"
 *                 frontend-port: 80
 *                 protocol: "TCP"
 *             end
 *         end
 */
public class PublicFrontend extends Frontend {

    private String name;
    private String publicIpAddressName;

    public PublicFrontend(){

    }

    public PublicFrontend(LoadBalancerPublicFrontend publicFrontend) {
        setName(publicFrontend.name());
        setPublicIpAddressName(publicFrontend.getPublicIPAddress().name());
        publicFrontend.inboundNatPools().entrySet().stream()
                .forEach(pool -> getInboundNatPool().add(new InboundNatPool(pool.getValue())));
        publicFrontend.inboundNatRules().entrySet().stream()
                .forEach(rule -> getInboundNatRule().add(new InboundNatRule(rule.getValue())));
    }

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
        return "public frontend " + getName();
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

        PublicFrontend publicFrontend = (PublicFrontend) obj;

        return (publicFrontend.getName()).equals(this.getName());
    }
}
