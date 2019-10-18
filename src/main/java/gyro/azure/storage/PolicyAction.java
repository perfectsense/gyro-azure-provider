package gyro.azure.storage;

import com.microsoft.azure.management.storage.ManagementPolicyAction;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class PolicyAction extends Diffable implements Copyable<ManagementPolicyAction> {
    private PolicyBaseBlob baseBlob;
    private PolicySnapshot snapshot;

    /**
     * Policy Action for the base blob. (Required)
     */
    @Required
    @Updatable
    public PolicyBaseBlob getBaseBlob() {
        return baseBlob;
    }

    public void setBaseBlob(PolicyBaseBlob baseBlob) {
        this.baseBlob = baseBlob;
    }

    /**
     * Policy Action for snapshot.
     */
    @Updatable
    public PolicySnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(PolicySnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String primaryKey() {
        return "policy-action";
    }

    @Override
    public void copyFrom(ManagementPolicyAction policyAction) {
        PolicyBaseBlob policyBaseBlob = null;
        if (policyAction.baseBlob() != null) {
            policyBaseBlob = newSubresource(PolicyBaseBlob.class);
            policyBaseBlob.copyFrom(policyAction.baseBlob());
        }
        setBaseBlob(policyBaseBlob);

        PolicySnapshot policySnapshot = null;
        if (policyAction.snapshot() != null) {
            policySnapshot = newSubresource(PolicySnapshot.class);
            policySnapshot.copyFrom(policyAction.snapshot());
        }
        setSnapshot(policySnapshot);
    }

    ManagementPolicyAction toManagementPolicyAction() {
        ManagementPolicyAction policyAction = new ManagementPolicyAction();

        if (getBaseBlob() != null) {
            policyAction = policyAction.withBaseBlob(getBaseBlob().toManagementPolicyBaseBlob());
        }

        if (getSnapshot() != null) {
            policyAction = policyAction.withSnapshot(getSnapshot().toManagementPolicySnapShot());
        }

        return policyAction;
    }
}
