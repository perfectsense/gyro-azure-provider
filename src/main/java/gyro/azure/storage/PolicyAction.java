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

import com.microsoft.azure.management.storage.ManagementPolicyAction;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class PolicyAction extends Diffable implements Copyable<ManagementPolicyAction> {
    private PolicyBaseBlob baseBlob;
    private PolicySnapshot snapshot;

    /**
     * The policy action for the base blob. (Required)
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
     * The policy action for snapshot.
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
