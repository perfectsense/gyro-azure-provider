package gyro.azure.network;

import gyro.core.diff.Diffable;

import java.util.ArrayList;
import java.util.List;

public class FrontendIpConfiguration extends Diffable {

    private String frontendIpConfigurationName;
    private List<InboundNatPool> inboundNatPool;
    private List<InboundNatRule> inboundNatRule;

    public String getFrontendIpConfigurationName() {
        return frontendIpConfigurationName;
    }

    public void setFrontendIpConfigurationName(String frontendIpConfigurationName) {
        this.frontendIpConfigurationName = frontendIpConfigurationName;
    }

    public List<InboundNatPool> getInboundNatPool() {
        if (inboundNatPool == null) {
            inboundNatPool = new ArrayList<>();
        }

        return inboundNatPool;
    }

    public void setInboundNatPool(List<InboundNatPool> inboundNatPool) {
        this.inboundNatPool = inboundNatPool;
    }

    public List<InboundNatRule> getInboundNatRule() {
        if (inboundNatRule == null) {
            inboundNatRule = new ArrayList<>();
        }

        return inboundNatRule;
    }

    public void setInboundNatRule(List<InboundNatRule> inboundNatRule) {
        this.inboundNatRule = inboundNatRule;
    }

    public String primaryKey() {
        return String.format("%s", getFrontendIpConfigurationName());
    }

    @Override
    public String toDisplayString() {
        return "frontend ip configuration " + getFrontendIpConfigurationName();
    }
}

