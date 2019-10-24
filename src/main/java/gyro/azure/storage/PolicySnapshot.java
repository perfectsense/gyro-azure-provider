package gyro.azure.storage;

import com.microsoft.azure.management.storage.DateAfterCreation;
import com.microsoft.azure.management.storage.ManagementPolicySnapShot;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class PolicySnapshot extends Diffable implements Copyable<ManagementPolicySnapShot> {
    private Integer deleteDays;

    /**
     * Days after which snapshot of the blob object is deleted. (Required)
     */
    @Required
    @Updatable
    public Integer getDeleteDays() {
        return deleteDays;
    }

    public void setDeleteDays(Integer deleteDays) {
        this.deleteDays = deleteDays;
    }

    @Override
    public String primaryKey() {
        return "policy-snapshot";
    }

    @Override
    public void copyFrom(ManagementPolicySnapShot policySnapShot) {
        setDeleteDays(policySnapShot.delete() != null ? policySnapShot.delete().daysAfterCreationGreaterThan() : null);
    }

    ManagementPolicySnapShot toManagementPolicySnapShot() {
        ManagementPolicySnapShot snapShot = new ManagementPolicySnapShot();
        if (getDeleteDays() != null) {
            snapShot.withDelete(new DateAfterCreation().withDaysAfterCreationGreaterThan(getDeleteDays()));
        }

        return snapShot;
    }
}
