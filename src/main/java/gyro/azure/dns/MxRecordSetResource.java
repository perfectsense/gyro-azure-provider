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
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithMXRecordMailExchangeOrAttachable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

        if (getTimeToLive() != null) {
            createMxRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createMxRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<MxRecord> iter = getMxRecord().listIterator(1);
        while (iter.hasNext()) {
            MxRecord mxRecord = iter.next();
            createMxRecordSet.withMailExchange(mxRecord.getExchange(), mxRecord.getPreference());
        }

        createMxRecordSet.attach().apply();
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
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updateMXRecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updateMXRecordSet.withoutMetadata(del));

        //update keys with changed values
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updateMXRecordSet.withoutMetadata((String) ele.getKey());
            updateMXRecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
        }

        List<MxRecord> addRecords = new ArrayList<>(getMxRecord());

        Map<String, Integer> oldMap =
                oldRecord.getMxRecord().stream()
                        .collect(Collectors.toMap(o->o.getExchange(), o->o.getPreference()));

        addRecords.removeIf(o -> (oldMap.containsKey(o.getExchange())
                && oldMap.get(o.getExchange()) == (o.getPreference())));

        for (MxRecord arecord : addRecords) {
            updateMXRecordSet.withMailExchange(arecord.getExchange(), arecord.getPreference());
        }

        List<MxRecord> removeRecords = new ArrayList<>(oldRecord.getMxRecord());
        Map<String, Integer> currentMap =
                getMxRecord().stream()
                        .collect(Collectors.toMap(o->o.getExchange(), o->o.getPreference()));

        removeRecords.removeIf(o -> (currentMap.containsKey(o.getExchange())
                && currentMap.get(o.getExchange()) == (o.getPreference())));

        for (MxRecord rrecord : removeRecords) {
            updateMXRecordSet.withoutMailExchange(rrecord.getExchange(), rrecord.getPreference());
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
