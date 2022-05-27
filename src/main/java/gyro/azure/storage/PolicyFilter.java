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
import java.util.HashSet;
import java.util.Set;

import com.azure.resourcemanager.storage.models.ManagementPolicyFilter;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class PolicyFilter extends Diffable implements Copyable<ManagementPolicyFilter> {

    private Set<String> blobTypes;
    private Set<String> prefixMatches;

    /**
     * Allowed blob types for the filter. Currently only supported value is ``blockBlob``. Defaults to ``blockBlob``.
     */
    public Set<String> getBlobTypes() {
        if (blobTypes == null) {
            blobTypes = new HashSet<>();
            blobTypes.add("blockBlob");
        }

        return blobTypes;
    }

    public void setBlobTypes(Set<String> blobTypes) {
        this.blobTypes = blobTypes;
    }

    /**
     * A set of prefixes for the blob objects to be filtered on.
     */
    @Updatable
    public Set<String> getPrefixMatches() {
        if (prefixMatches == null) {
            prefixMatches = new HashSet<>();
        }

        return prefixMatches;
    }

    public void setPrefixMatches(Set<String> prefixMatches) {
        this.prefixMatches = prefixMatches;
    }

    @Override
    public String primaryKey() {
        return "policy-filter";
    }

    @Override
    public void copyFrom(ManagementPolicyFilter policyFilter) {
        setBlobTypes(policyFilter.blobTypes() != null ? new HashSet<>(policyFilter.blobTypes()) : null);
        setPrefixMatches(policyFilter.prefixMatch() != null ? new HashSet<>(policyFilter.prefixMatch()) : null);
    }

    ManagementPolicyFilter toManagementPolicyFilter() {
        ManagementPolicyFilter policyFilter = new ManagementPolicyFilter();

        policyFilter = policyFilter.withBlobTypes(new ArrayList<>(getBlobTypes()))
            .withPrefixMatch(new ArrayList<>(getPrefixMatches()));

        return policyFilter;
    }
}
