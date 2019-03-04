package gyro.azure.network;

import gyro.core.diff.Diffable;

public class InboundNatRule extends Diffable {

    private String inboundNatRuleName;
    private String port;
    private String protocol;

    public String getInboundNatRuleName() {
        return inboundNatRuleName;
    }

    public void setInboundNatRuleName(String inboundNatRuleName) {
        this.inboundNatRuleName = inboundNatRuleName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String primaryKey() {
        return String.format("%s", getInboundNatRuleName());
    }

    @Override
    public String toDisplayString() {
        return "inbound nat rule " + getInboundNatRuleName();
    }
}
