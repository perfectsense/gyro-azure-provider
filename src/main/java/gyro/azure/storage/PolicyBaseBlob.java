/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.storage;

import java.util.ArrayList;
import java.util.List;

import com.azure.resourcemanager.storage.models.DateAfterModification;
import com.azure.resourcemanager.storage.models.ManagementPolicyBaseBlob;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;

public class PolicyBaseBlob extends Diffable implements Copyable<ManagementPolicyBaseBlob> {

    private Double deleteDays;
    private Double tierToArchiveDays;
    private Double tierToCoolDays;

    /**
     * Days after which a blob object will be deleted. At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required.
     */
    @Updatable
    public Double getDeleteDays() {
        return deleteDays;
    }

    public void setDeleteDays(Double deleteDays) {
        this.deleteDays = deleteDays;
    }

    /**
     * Days after which a blob object will be moved to archive. At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required.
     */
    @Updatable
    public Double getTierToArchiveDays() {
        return tierToArchiveDays;
    }

    public void setTierToArchiveDays(Double tierToArchiveDays) {
        this.tierToArchiveDays = tierToArchiveDays;
    }

    /**
     * Days after which a blob object will be moved to cool storage. At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required.
     */
    @Updatable
    public Double getTierToCoolDays() {
        return tierToCoolDays;
    }

    public void setTierToCoolDays(Double tierToCoolDays) {
        this.tierToCoolDays = tierToCoolDays;
    }

    @Override
    public String primaryKey() {
        return "policy-base-blob";
    }

    @Override
    public void copyFrom(ManagementPolicyBaseBlob policyBaseBlob) {
        setDeleteDays(policyBaseBlob.delete() != null
            ? (double) policyBaseBlob.delete().daysAfterModificationGreaterThan()
            : null);
        setTierToArchiveDays(policyBaseBlob.tierToArchive() != null ? (double) policyBaseBlob.tierToArchive()
            .daysAfterModificationGreaterThan() : null);
        setTierToCoolDays(policyBaseBlob.tierToCool() != null ? (double) policyBaseBlob.tierToCool()
            .daysAfterModificationGreaterThan() : null);
    }

    ManagementPolicyBaseBlob toManagementPolicyBaseBlob() {
        ManagementPolicyBaseBlob blob = new ManagementPolicyBaseBlob();
        if (getDeleteDays() != null) {
            blob.withDelete(new DateAfterModification().withDaysAfterModificationGreaterThan(Float.parseFloat(
                getDeleteDays().toString())));
        }

        if (getTierToArchiveDays() != null) {
            blob.withTierToArchive(new DateAfterModification().withDaysAfterModificationGreaterThan(Float.parseFloat(
                getTierToArchiveDays().toString())));
        }

        if (getTierToCoolDays() != null) {
            blob.withTierToCool(new DateAfterModification().withDaysAfterModificationGreaterThan(Float.parseFloat(
                getTierToCoolDays().toString())));
        }

        return blob;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getDeleteDays() == null && getTierToCoolDays() == null && getTierToArchiveDays() == null) {
            errors.add(new ValidationError(
                this,
                null,
                "At least one of 'delete-days' or 'tier-to-cool-days' or 'tier-to-archive-days' is required."));
        }

        return errors;
    }
}
