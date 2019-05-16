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
import com.microsoft.azure.management.dns.MXRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.MXRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithMXRecordMailExchangeOrAttachable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an MX Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     mx-record-set
 *         name: "mxrecexample"
 *         time-to-live: "4"
 *         dns-zone: $(azure::dns-zone dns-zone-resource-example)
 *
 *         mx-record
 *             exchange: "mail.cont.com"
 *             preference: 1
 *         end
 *
 *         mx-record
 *             exchange: "mail.conto.com"
 *             preference: 2
 *         end
 *     end
 */
public class MxRecordSetResource extends AzureResource {

    private DnsZoneResource dnsZone;
    private List<MxRecord> mxRecord;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    public MxRecordSetResource() {}

    public MxRecordSetResource(MXRecordSet mxRecordSet) {
        mxRecordSet.records().forEach(record -> getMxRecord().add(new MxRecord(record)));
        setMetadata(mxRecordSet.metadata());
        setName(mxRecordSet.name());
        setTimeToLive(Long.toString(mxRecordSet.timeToLive()));
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
     * The list of mx records. (Required)
     */
    @ResourceUpdatable
    public List<MxRecord> getMxRecord() {
        if (mxRecord == null) {
            mxRecord = new ArrayList<>();
        }

        return mxRecord;
    }

    public void setMxRecord(List<MxRecord> mxRecord) {
        this.mxRecord = mxRecord;
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
        if (getMxRecord() == null || getMxRecord().size() == 0) {
            throw new GyroException("At least one mx record must be provided.");
        }

        Azure client = createClient();

        MXRecordSetBlank<DnsZone.Update> defineMXRecordSet =
                getDnsZone().getDnsZone(client).defineMXRecordSet(getName());

        WithMXRecordMailExchangeOrAttachable<DnsZone.Update> createMXRecordSet = null;
        for (MxRecord mxRecord : getMxRecord()) {
            createMXRecordSet = defineMXRecordSet.withMailExchange(mxRecord.getExchange(), mxRecord.getPreference());
        }

        if (getTimeToLive() != null) {
            createMXRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createMXRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        createMXRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateMXRecordSet updateMXRecordSet =
                getDnsZone().getDnsZone(client).updateMXRecordSet(getName());

        if (getTimeToLive() != null) {
            updateMXRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        MxRecordSetResource oldRecord = (MxRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateMXRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateMXRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateMXRecordSet.withoutMetadata(ele.getKey());
            updateMXRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<MxRecord> addRecords = new ArrayList<>(getMxRecord());

        Map<String, Integer> oldMap =
                oldRecord.getMxRecord().stream()
                        .collect(Collectors.toMap(MxRecord::getExchange, MxRecord::getPreference));

        addRecords.removeIf(o -> (oldMap.containsKey(o.getExchange())
                && oldMap.get(o.getExchange()).equals(o.getPreference())));

        for (MxRecord addRecord : addRecords) {
            updateMXRecordSet.withMailExchange(addRecord.getExchange(), addRecord.getPreference());
        }

        List<MxRecord> removeRecords = new ArrayList<>(oldRecord.getMxRecord());
        Map<String, Integer> currentMap =
                getMxRecord().stream()
                        .collect(Collectors.toMap(MxRecord::getExchange, MxRecord::getPreference));

        removeRecords.removeIf(o -> (currentMap.containsKey(o.getExchange())
                && currentMap.get(o.getExchange()).equals(o.getPreference())));

        for (MxRecord removeRecord : removeRecords) {
            updateMXRecordSet.withoutMailExchange(removeRecord.getExchange(), removeRecord.getPreference());
        }

        updateMXRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutMXRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "mx record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
