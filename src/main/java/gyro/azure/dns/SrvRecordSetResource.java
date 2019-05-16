package gyro.azure.dns;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithSrvRecordEntryOrAttachable;
import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Creates an SRV Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     srv-record-set
 *         name: "srvrecexample"
 *         time-to-live: "4"
 *
 *         srv-record
 *             port: 80
 *             priority: 1
 *             target: "testtarget.com"
 *             weight: 100
 *         end
 *     end
 */
public class SrvRecordSetResource extends AzureResource {

    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private List<SrvRecord> srvRecord;
    private String timeToLive;

    public SrvRecordSetResource() {}

    public SrvRecordSetResource(SrvRecordSet srvRecordSet) {
        srvRecordSet.records().forEach(record -> getSrvRecord().add(new SrvRecord(record)));
        setMetadata(srvRecordSet.metadata());
        setName(srvRecordSet.name());
        setTimeToLive(Long.toString(srvRecordSet.timeToLive()));
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
     * The list of srv records. (Required)
     */
    @ResourceUpdatable
    public List<SrvRecord> getSrvRecord() {
        if (srvRecord == null) {
            srvRecord = new ArrayList<>();
        }

        return srvRecord;
    }

    public void setSrvRecord(List<SrvRecord> srvRecord) {
        this.srvRecord = srvRecord;
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

    @Override
    public void create() {
        Azure client = createClient();

        SrvRecordSetBlank<DnsZone.Update> defineSrvRecordSet =
                getDnsZone().getDnsZone(client).defineSrvRecordSet(getName());

        if (getTimeToLive() != null) {
            createSrvRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createSrvRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<SrvRecord> iter = getSrvRecord().listIterator(1);
        while (iter.hasNext()) {
            SrvRecord srvRecord = iter.next();
            createSrvRecordSet
                    .withRecord(srvRecord.getTarget(), srvRecord.getPort(), srvRecord.getPriority(), srvRecord.getWeight());
        }

        createSrvRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateSrvRecordSet updateSrvRecordSet =
                getDnsZone().getDnsZone(client).updateSrvRecordSet(getName());

        if (getTimeToLive() != null) {
            updateSrvRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        SrvRecordSetResource oldRecord = (SrvRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updateSrvRecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updateSrvRecordSet.withoutMetadata(del));

        //update keys with changed values
        //delete
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updateSrvRecordSet.withoutMetadata((String) ele.getKey());
            updateSrvRecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
        }

        List<SrvRecord> addRecords = comparator(getSrvRecord(), oldRecord.getSrvRecord());


        for (SrvRecord arecord : addRecords) {
            updateSrvRecordSet.withRecord(arecord.getTarget(), arecord.getPort(), arecord.getPriority(), arecord.getWeight());
        }

        List<SrvRecord> removeRecords = comparator(oldRecord.getSrvRecord(), getSrvRecord());

        for (SrvRecord rrecord : removeRecords) {
            updateSrvRecordSet.withoutRecord(rrecord.getTarget(), rrecord.getPort(), rrecord.getPriority(), rrecord.getWeight());
        }

        updateSrvRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutSrvRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "srv record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    private List<SrvRecord> comparator(List<SrvRecord> original, List<SrvRecord> compareTo) {
        List<SrvRecord> differences = new ArrayList<>(original);

        for (SrvRecord record : original) {
            for (SrvRecord comp : compareTo) {
                if (record.getTarget().equals(comp.getTarget())
                        && record.getPort().equals(comp.getPort())
                        && record.getPriority().equals(comp.getPriority())
                        && record.getWeight().equals(comp.getWeight())) {
                    differences.remove(record);
                }
            }
        }

        return differences;
    }
}
