package gyro.azure.storage;

import com.microsoft.azure.management.storage.ManagementPolicyDefinition;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class PolicyDefinition extends Diffable implements Copyable<ManagementPolicyDefinition> {
    private PolicyAction action;
    private PolicyFilter filter;

    /**
     * The action details for the policy rule. (Required)
     *
     * @subresource gyro.azure.storage.PolicyAction
     */
    @Required
    @Updatable
    public PolicyAction getAction() {
        return action;
    }

    public void setAction(PolicyAction action) {
        this.action = action;
    }

    /**
     * The filter details for the policy rule.
     *
     * @subresource gyro.azure.storage.PolicyFilter
     */
    @Updatable
    public PolicyFilter getFilter() {
        if (filter == null) {
            filter = newSubresource(PolicyFilter.class);
        }

        return filter;
    }

    public void setFilter(PolicyFilter filter) {
        this.filter = filter;
    }

    @Override
    public String primaryKey() {
        return "policy-definition";
    }

    @Override
    public void copyFrom(ManagementPolicyDefinition policyDefinition) {
        PolicyAction policyAction = null;
        if (policyDefinition.actions() != null) {
            policyAction = newSubresource(PolicyAction.class);
            policyAction.copyFrom(policyDefinition.actions());
        }
        setAction(policyAction);

        PolicyFilter policyFilter = null;
        if (policyDefinition.filters() != null) {
            policyFilter = newSubresource(PolicyFilter.class);
            policyFilter.copyFrom(policyDefinition.filters());
        }
        setFilter(policyFilter);
    }

    ManagementPolicyDefinition toManagementPolicyDefinition() {
        ManagementPolicyDefinition policyDefinition = new ManagementPolicyDefinition();
        if (getFilter() != null) {
            policyDefinition = policyDefinition.withFilters(getFilter().toManagementPolicyFilter());
        }

        if (getAction() != null) {
            policyDefinition = policyDefinition.withActions(getAction().toManagementPolicyAction());
        }

        return policyDefinition;
    }
}
