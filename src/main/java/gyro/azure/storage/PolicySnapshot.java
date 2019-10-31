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
