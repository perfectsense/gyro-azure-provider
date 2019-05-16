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
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithPtrRecordTargetDomainNameOrAttachable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
 *         dns-zone: $(azure::dns-zone dns-zone-resource-example)
 *     end
 */
public class PtrRecordSetResource extends AzureResource {

    private DnsZoneResource dnsZone;
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
     * The dns zone where the record set resides. (Required)
     */
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
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
                getDnsZone().getDnsZone(client).definePtrRecordSet(getName());


        if (getTimeToLive() != null) {
            createPtrRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createPtrRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<String> iter = getTargetDomainNames().listIterator(1);
        while (iter.hasNext()) {
            String domainName = iter.next();
            createPtrRecordSet.withTargetDomainName(domainName);
        }

        createPtrRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet =
                getDnsZone().getDnsZone(client).updatePtrRecordSet(getName());

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
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updatePtrRecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updatePtrRecordSet.withoutMetadata(del));

        //update keys with changed values
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updatePtrRecordSet.withoutMetadata((String) ele.getKey());
            updatePtrRecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
        }

        updatePtrRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutPtrRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "ptr record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
