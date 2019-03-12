package gyro.azure.network;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

import java.util.List;

public abstract class Frontend extends Diffable {

    public List<InboundNatPool> inboundNatPool;
    public List<InboundNatRule> inboundNatRule;

    /**
     * The inbound nat pools associated with the frontend. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<InboundNatPool> getInboundNatPool() {
        return inboundNatPool;
    }

    public void setInboundNatPool(List<InboundNatPool> inboundNatPool) {
        this.inboundNatPool = inboundNatPool;
    }

    /**
     * The inbound nat rules associated with the frontend. Nat rules may not be
     * associated with a frontend if a nat pool is associated. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<InboundNatRule> getInboundNatRule() {
        return inboundNatRule;
    }

    public void setInboundNatRule(List<InboundNatRule> inboundNatRule) {
        this.inboundNatRule = inboundNatRule;
    }
}
