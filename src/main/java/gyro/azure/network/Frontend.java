package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.HashSet;
import java.util.Set;

public abstract class Frontend extends Diffable {
    private Set<InboundNatPool> inboundNatPool;
    private Set<InboundNatRule> inboundNatRule;

    /**
     * The Inbound Nat Pools Associated with the Frontend. (Optional)
     */
    @Updatable
    public Set<InboundNatPool> getInboundNatPool() {
        if (inboundNatPool == null) {
            inboundNatPool = new HashSet<>();
        }

        return inboundNatPool;
    }

    public void setInboundNatPool(Set<InboundNatPool> inboundNatPool) {
        this.inboundNatPool = inboundNatPool;
    }

    /**
     * The Inbound Nat Rules associated with the Frontend. Nat rules may not be associated with a frontend if a Nat Pool is associated. (Optional)
     */
    @Updatable
    public Set<InboundNatRule> getInboundNatRule() {
        if (inboundNatRule == null) {
            inboundNatRule = new HashSet<>();
        }

        return inboundNatRule;
    }

    public void setInboundNatRule(Set<InboundNatRule> inboundNatRule) {
        this.inboundNatRule = inboundNatRule;
    }
}