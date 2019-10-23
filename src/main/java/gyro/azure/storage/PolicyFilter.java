package gyro.azure.storage;

import com.microsoft.azure.management.storage.ManagementPolicyFilter;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
