package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Frontend extends Diffable {
    private Set<InboundNatPool> inboundNatPool;
    private Set<InboundNatRule> inboundNatRule;
    private Map<String, InboundNatRule> rules;

    /**
     * The inbound nat pools associated with the frontend. (Optional)
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
     * The inbound nat rules associated with the frontend. Nat rules may not be associated with a frontend if a nat pool is associated. (Optional)
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

    @Updatable
    public Map<String, InboundNatRule> rules() {
        if (rules == null) {
            rules = new HashMap<>();
        }

        getInboundNatRule().forEach(rule -> rules.put(rule.getName(), rule));

        return rules;
    }
}
