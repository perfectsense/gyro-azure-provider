package gyro.azure.network;

import gyro.core.diff.Diffable;
public class InboundNatPool extends Diffable {

    private Integer backendPort;
    private Integer frontendPortRangeStart;
    private Integer frontendPortRangeEnd;
    private String inboundNatPoolName;
    private String protocol;

    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    }

    }

    public Integer getFrontendPortRangeStart() {
        return frontendPortRangeStart;
    }

    public void setFrontendPortRangeStart(Integer frontendPortRangeStart) {
        this.frontendPortRangeStart = frontendPortRangeStart;
    }

    public Integer getFrontendPortRangeEnd() {
        return frontendPortRangeEnd;
    }

    public void setFrontendPortRangeEnd(Integer frontendPortRangeEnd) {
        this.frontendPortRangeEnd = frontendPortRangeEnd;
    }

    public String getInboundNatPoolName() {
        return inboundNatPoolName;
    }

    public void setInboundNatPoolName(String inboundNatPoolName) {
        this.inboundNatPoolName = inboundNatPoolName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String primaryKey() {
        return String.format("%s", getInboundNatPoolName());
    }

    @Override
    public String toDisplayString() {
        return "inbound nat pool " + getInboundNatPoolName();
    }
}
