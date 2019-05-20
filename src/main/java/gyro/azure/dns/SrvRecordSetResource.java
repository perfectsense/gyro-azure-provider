package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithSrvRecordEntryOrAttachable;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.SrvRecordSetBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 *     azure::srv-record-set
 *         name: "srvrecexample"
 *         time-to-live: "4"
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *
 *         srv-record
 *             port: 80
 *             priority: 1
 *             target: "hi.com"
 *             weight: 100
 *         end
 *     end
 */
@ResourceType("srv-record-set")
public class SrvRecordSetResource extends AzureResource {

    private String dnsZoneId;
    private Map<String, String> metadata;
    private String name;
    private List<SrvRecord> srvRecord;
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
        Azure client = createClient();

        SrvRecordSet srvRecordSet = client.dnsZones().getById(getDnsZoneId()).srvRecordSets().getByName(getName());

        if (srvRecordSet == null) {
            return false;
        }

        getSrvRecord().clear();
        srvRecordSet.records().forEach(record -> getSrvRecord().add(new SrvRecord(record)));
        setMetadata(srvRecordSet.metadata());
        setName(srvRecordSet.name());
        setTimeToLive(Long.toString(srvRecordSet.timeToLive()));

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        SrvRecordSetBlank<DnsZone.Update> defineSrvRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().defineSrvRecordSet(getName());

        WithSrvRecordEntryOrAttachable<DnsZone.Update> createSrvRecordSet = null;
        for (SrvRecord srvRecord : getSrvRecord()) {
            createSrvRecordSet = defineSrvRecordSet
                    .withRecord(srvRecord.getTarget(), srvRecord.getPort(), srvRecord.getPriority(), srvRecord.getWeight());
        }

        if (getTimeToLive() != null) {
            createSrvRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createSrvRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createSrvRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateSrvRecordSet updateSrvRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().updateSrvRecordSet(getName());

        if (getTimeToLive() != null) {
            updateSrvRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        SrvRecordSetResource oldRecord = (SrvRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateSrvRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateSrvRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateSrvRecordSet.withoutMetadata(ele.getKey());
            updateSrvRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<SrvRecord> addRecords = comparator(getSrvRecord(), oldRecord.getSrvRecord());

        for (SrvRecord addRecord : addRecords) {
            updateSrvRecordSet.withRecord(addRecord.getTarget(), addRecord.getPort(), addRecord.getPriority(), addRecord.getWeight());
        }

        List<SrvRecord> removeRecords = comparator(oldRecord.getSrvRecord(), getSrvRecord());

        for (SrvRecord removeRecord : removeRecords) {
            updateSrvRecordSet.withoutRecord(removeRecord.getTarget(), removeRecord.getPort(), removeRecord.getPriority(), removeRecord.getWeight());
        }

        DnsZone.Update parent = updateSrvRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZoneId()).update().withoutSrvRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "srv record set " + getName();
    }

    private List<SrvRecord> comparator(List<SrvRecord> original, List<SrvRecord> compareTo) {
        List<SrvRecord> differences = new ArrayList<>(original);

        for (SrvRecord record : original) {
            for (SrvRecord comp : compareTo) {
                if (record.primaryKey().equals(comp.primaryKey())) {
                    differences.remove(record);
                }
            }
        }

        return differences;
    }
}
