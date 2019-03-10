package gyro.azure.network;

import gyro.core.diff.Diffable;


    private Boolean staticPublicIp;
    private String frontendName;
    private String staticPublicIpAddress;
    private String networkName;
    private Boolean publicFrontEnd;
    private String subnet;
public abstract class Frontend extends Diffable {

    public Boolean getStaticPublicIp() {
        return staticPublicIp;
    }

    public void setStaticPublicIp(Boolean staticPublicIp) {
        this.staticPublicIp = staticPublicIp;
    }

    public String getFrontendName() {
        return frontendName;
    }

    public void setFrontendName(String frontendName) {
        this.frontendName = frontendName;
    }

    public String getStaticPublicIpAddress() {
        return staticPublicIpAddress;
    }

    public void setStaticPublicIpAddress(String staticPublicIpAddress) {
        this.staticPublicIpAddress = staticPublicIpAddress;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public Boolean getPublicFrontEnd() {
        return publicFrontEnd;
    }

    public void setPublicFrontEnd(Boolean publicFrontEnd) {
        this.publicFrontEnd = publicFrontEnd;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    }

    }
}
