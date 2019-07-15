package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
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
 *     azure::ptr-record-set
 *         name: "ptrrecexample"
 *         target-domain-names: ["domain1.com", "domain2.com"]
 *         time-to-live: "3"
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *     end
 */
@Type("ptr-record-set")
public class PtrRecordSetResource extends AzureResource {

    private String dnsZoneId;
    private Map<String, String> metadata;
    private String name;
    private List<String> targetDomainNames;
    private String timeToLive;

    /**
     * The dns zone where the record resides. (Required)
     */
    public String getDnsZoneId() {
        return dnsZoneId;
    }

    public void setDnsZoneId(String dnsZoneId) {
        this.dnsZoneId = dnsZoneId;
    }

    /**
     * The metadata for the record. (Optional)
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
    @Updatable
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
    @Updatable
    public String getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(String timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        PtrRecordSet ptrRecordSet = client.dnsZones().getById(getDnsZoneId()).ptrRecordSets().getByName(getName());

        if (ptrRecordSet == null) {
            return false;
        }

        setMetadata(ptrRecordSet.metadata());
        setName(ptrRecordSet.name());
        setTargetDomainNames(ptrRecordSet.targetDomainNames());
        setTimeToLive(Long.toString(ptrRecordSet.timeToLive()));

        return true;
    }

    public void create(State state) {
        if (getTargetDomainNames() == null || getTargetDomainNames().size() == 0) {
            throw new GyroException("At least one target domain name must be provided.");
        }

        Azure client = createClient();

        PtrRecordSetBlank<DnsZone.Update> definePtrRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().definePtrRecordSet(getName());

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

        DnsZone.Update attach = createPtrRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdatePtrRecordSet updatePtrRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().updatePtrRecordSet(getName());

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

        DnsZone.Update parent = updatePtrRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete(State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZoneId()).update().withoutPtrRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "ptr record set " + getName();
    }
}
