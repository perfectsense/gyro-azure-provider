package gyro.azure.network;

import gyro.core.diff.Diffable;

public class Frontend extends Diffable {

    private Boolean staticPublicIp;
    private String frontendName;
    private String staticPublicIpAddress;
    private String networkName;
    private Boolean publicFrontEnd;
    private String subnet;

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

    public String primaryKey() {
        return String.format("%s/%s", getFrontendName(), getStaticPublicIpAddress());
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (getPublicFrontEnd() == true) {
            sb.append("public " );
        } else {
            sb.append("private ");
        }
        sb.append("frontend " + getFrontendName());
        return sb.toString();
    }
}
