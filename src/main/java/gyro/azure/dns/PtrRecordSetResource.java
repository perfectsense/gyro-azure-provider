package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.PtrRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithPtrRecordTargetDomainNameOrAttachable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an PTR Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     ptr-record-set
 *         name: "ptrexample"
 *         target-domain-names: ["domain1.com", "domain2.com"]
 *         time-to-live: "3"
 *     end
 */
public class PtrRecordSetResource extends AzureResource {

    private Map<String, String> metadata;
    private String name;
    private List<String> targetDomainNames;
    private String timeToLive;

    public PtrRecordSetResource() {}

    public PtrRecordSetResource(PtrRecordSet nsRecordSet) {
        setMetadata(nsRecordSet.metadata());
        setName(nsRecordSet.name());
        setTargetDomainNames(nsRecordSet.targetDomainNames());
        setTimeToLive(Long.toString(nsRecordSet.timeToLive()));
    }

    /**
     * The metadata for the record. (Optional)
     */
    @ResourceUpdatable
    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * The name of the record. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The domain names associated with the record. (Required)
     */
    @ResourceUpdatable
    public List<String> getTargetDomainNames() {
        if (targetDomainNames == null) {
            targetDomainNames = new ArrayList<>();
        }

        return targetDomainNames;
    }

    public void setTargetDomainNames(List<String> targetDomainNames) {
        this.targetDomainNames = targetDomainNames;
    }

    /**
     * The Time To Live for the records in the set. (Required)
     */
    @ResourceUpdatable
    public String getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(String timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    public void create() {
        if (getTargetDomainNames() == null || getTargetDomainNames().size() == 0) {
            throw new GyroException("At least one target domain name must be provided.");
        }

        Azure client = createClient();

        PtrRecordSetBlank<DnsZone.Update> definePtrRecordSet =
                ((DnsZoneResource) parentResource()).getDnsZone(client).definePtrRecordSet(getName());

        WithPtrRecordTargetDomainNameOrAttachable<DnsZone.Update> createPtrRecordSet = null;
        for (String targetDomainName : getTargetDomainNames()) {
            createPtrRecordSet = definePtrRecordSet.withTargetDomainName(targetDomainName);
        }

        if (getTimeToLive() != null) {
            createPtrRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createPtrRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        createPtrRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet =
                ((DnsZoneResource) parentResource()).getDnsZone(client).updatePtrRecordSet(getName());

        if (getTimeToLive() != null) {
            updatePtrRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        PtrRecordSetResource oldRecord = (PtrRecordSetResource) current;

        List<String> addNames = new ArrayList<>(getTargetDomainNames());
        addNames.removeAll(oldRecord.getTargetDomainNames());

        List<String> removeNames = new ArrayList<>(oldRecord.getTargetDomainNames());
        removeNames.removeAll(getTargetDomainNames());

        for (String addDomain : addNames) {
            updatePtrRecordSet.withTargetDomainName(addDomain);
        }

        for (String remDomain : removeNames) {
            updatePtrRecordSet.withoutTargetDomainName(remDomain);
        }

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updatePtrRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updatePtrRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updatePtrRecordSet.withoutMetadata(ele.getKey());
            updatePtrRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        updatePtrRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        ((DnsZoneResource) parentResource()).getDnsZone(client).withoutPtrRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "ptr record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
