package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.PtrRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithPtrRecordTargetDomainNameOrAttachable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 *     azure::ptr-record-set
 *         name: "ptrrecexample"
 *         target-domain-names: ["domain1.com", "domain2.com"]
 *         time-to-live: 3
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *     end
 */
@Type("ptr-record-set")
public class PtrRecordSetResource extends AzureResource implements Copyable<PtrRecordSet> {

    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private Set<String> targetDomainNames;
    private Long timeToLive;
    private String id;

    /**
     * The DNS Zone where the Ptr Record Set resides. (Required)
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The metadata for the Ptr Record Set. (Optional)
     */
    @Updatable
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
     * The name of the Ptr Record Set. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The domain names associated with the Ptr Record Set. (Required)
     */
    @Required
    @Updatable
    public Set<String> getTargetDomainNames() {
        if (targetDomainNames == null) {
            targetDomainNames = new HashSet<>();
        }

        return targetDomainNames;
    }

    public void setTargetDomainNames(Set<String> targetDomainNames) {
        this.targetDomainNames = targetDomainNames;
    }

    /**
     * The Time To Live for the Ptr Record Set in the set. (Required)
     */
    @Required
    @Updatable
    public Long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * The ID of the Ptr Record Set.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(PtrRecordSet ptrRecordSet) {
        setMetadata(ptrRecordSet.metadata());
        setName(ptrRecordSet.name());
        setTargetDomainNames(new HashSet<>(ptrRecordSet.targetDomainNames()));
        setTimeToLive(ptrRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, ptrRecordSet.parent().id()));
        setId(ptrRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        PtrRecordSet ptrRecordSet = client.dnsZones().getById(getDnsZone().getId()).ptrRecordSets().getByName(getName());

        if (ptrRecordSet == null) {
            return false;
        }

        copyFrom(ptrRecordSet);

        return true;
    }

    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        PtrRecordSetBlank<DnsZone.Update> definePtrRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().definePtrRecordSet(getName());

        WithPtrRecordTargetDomainNameOrAttachable<DnsZone.Update> createPtrRecordSet = null;
        for (String targetDomainName : getTargetDomainNames()) {
            createPtrRecordSet = definePtrRecordSet.withTargetDomainName(targetDomainName);
        }

        if (getTimeToLive() != null) {
            createPtrRecordSet.withTimeToLive(getTimeToLive());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createPtrRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createPtrRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.ptrRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updatePtrRecordSet(getName());

        if (getTimeToLive() != null) {
            updatePtrRecordSet.withTimeToLive(getTimeToLive());
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

        DnsZone.Update parent = updatePtrRecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.ptrRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutPtrRecordSet(getName()).apply();
    }
}
