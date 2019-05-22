package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Frontend extends Diffable {

    public List<InboundNatPool> inboundNatPool;
    public List<InboundNatRule> inboundNatRule;
    private Map<String, InboundNatRule> rules;

    /**
     * The inbound nat pools associated with the frontend. (Optional)
     */
    @Updatable
    public List<InboundNatPool> getInboundNatPool() {
        if (inboundNatPool == null) {
            inboundNatPool = new ArrayList<>();
        }

        return inboundNatPool;
    }

    public void setInboundNatPool(List<InboundNatPool> inboundNatPool) {
        this.inboundNatPool = inboundNatPool;
    }

    /**
     * The inbound nat rules associated with the frontend. Nat rules may not be
     * associated with a frontend if a nat pool is associated. (Optional)
     */
    @Updatable
    public List<InboundNatRule> getInboundNatRule() {
        if (inboundNatRule == null) {
            inboundNatRule = new ArrayList<>();
        }

        return inboundNatRule;
    }

    public void setInboundNatRule(List<InboundNatRule> inboundNatRule) {
        this.inboundNatRule = inboundNatRule;
    }

    @Updatable
    public Map<String, InboundNatRule> rules() {
        if (rules == null) {
            rules = new HashMap<>();
        }

        getInboundNatRule()
                .stream()
                .forEach(rule -> rules.put(rule.getName(), rule));

        return rules;
    }
}
