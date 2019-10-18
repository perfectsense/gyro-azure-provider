package gyro.azure.storage;

import com.microsoft.azure.management.storage.DateAfterModification;
import com.microsoft.azure.management.storage.ManagementPolicyBaseBlob;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

public class PolicyBaseBlob extends Diffable implements Copyable<ManagementPolicyBaseBlob> {
    private Integer deleteDays;
    private Integer tierToArchiveDays;
    private Integer tierToCoolDays;

    /**
     * Days after which blob object to be deleted. At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required.
     */
    @Updatable
    public Integer getDeleteDays() {
        return deleteDays;
    }

    public void setDeleteDays(Integer deleteDays) {
        this.deleteDays = deleteDays;
    }

    /**
     * Days after which blob object moved to archive. At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required.
     */
    @Updatable
    public Integer getTierToArchiveDays() {
        return tierToArchiveDays;
    }

    public void setTierToArchiveDays(Integer tierToArchiveDays) {
        this.tierToArchiveDays = tierToArchiveDays;
    }

    /**
     * Days after which blob object moved to cool storage. At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required.
     */
    @Updatable
    public Integer getTierToCoolDays() {
        return tierToCoolDays;
    }

    public void setTierToCoolDays(Integer tierToCoolDays) {
        this.tierToCoolDays = tierToCoolDays;
    }

    @Override
    public String primaryKey() {
        return "policy-base-blob";
    }

    @Override
    public void copyFrom(ManagementPolicyBaseBlob policyBaseBlob) {
        setDeleteDays(policyBaseBlob.delete() != null ? policyBaseBlob.delete().daysAfterModificationGreaterThan() : null);
        setTierToArchiveDays(policyBaseBlob.tierToArchive() != null ? policyBaseBlob.tierToArchive().daysAfterModificationGreaterThan() : null);
        setTierToCoolDays(policyBaseBlob.tierToCool() != null ? policyBaseBlob.tierToCool().daysAfterModificationGreaterThan() : null);
    }

    ManagementPolicyBaseBlob toManagementPolicyBaseBlob() {
        ManagementPolicyBaseBlob blob = new ManagementPolicyBaseBlob();
        if (getDeleteDays() != null) {
            blob.withDelete(new DateAfterModification().withDaysAfterModificationGreaterThan(getDeleteDays()));
        }

        if (getTierToArchiveDays() != null) {
            blob.withTierToArchive(new DateAfterModification().withDaysAfterModificationGreaterThan(getTierToArchiveDays()));
        }

        if (getTierToCoolDays() != null) {
            blob.withTierToCool(new DateAfterModification().withDaysAfterModificationGreaterThan(getTierToCoolDays()));
        }

        return blob;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getDeleteDays() == null && getTierToCoolDays() == null && getTierToArchiveDays() == null) {
            errors.add(new ValidationError(this, null, "At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required."));
        }

        return errors;
    }
}
