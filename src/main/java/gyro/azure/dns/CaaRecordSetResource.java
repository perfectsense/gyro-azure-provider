package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.CaaRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithCaaRecordEntryOrAttachable;
import gyro.core.resource.ResourceUpdatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class CaaRecordSetResource extends AzureResource {

    private List<CaaRecord> caaRecord;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    public CaaRecordSetResource() {}

    public CaaRecordSetResource(CaaRecordSet caaRecordSet) {
        caaRecordSet.records().forEach(record -> getCaaRecord().add(new CaaRecord(record)));
        setMetadata(caaRecordSet.metadata());
        setName(caaRecordSet.name());
        setTimeToLive(Long.toString(caaRecordSet.timeToLive()));
    }

    @ResourceUpdatable
    public List<CaaRecord> getCaaRecord() {
        if (caaRecord == null) {
            caaRecord = new ArrayList<>();
        }

        return caaRecord;
    }

    public void setCaaRecord(List<CaaRecord> caaRecord) {
        this.caaRecord = caaRecord;
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
        if (getCaaRecord() == null || getCaaRecord().size() == 0) {
            throw new GyroException("At least one caa record must be provided.");
        }

        CaaRecord firstRecord = getCaaRecord().get(0);
        WithCaaRecordEntryOrAttachable<DnsZone.Update> createCaaRecordSet =
                modify().defineCaaRecordSet(getName())
                        .withRecord(firstRecord.getFlags(), firstRecord.getTag(), firstRecord.getValue());

        if (getTimeToLive() != null) {
            createCaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCaaRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<CaaRecord> iter = getCaaRecord().listIterator(1);
        while (iter.hasNext()) {
            CaaRecord record = iter.next();
            createCaaRecordSet.withRecord(record.getFlags(), record.getTag(), record.getValue());
        }

        createCaaRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        DnsRecordSet.UpdateCaaRecordSet updateCaaRecordSet = modify().updateCaaRecordSet(getName());

        if (getTimeToLive() != null) {
            updateCaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        CaaRecordSetResource oldRecord = (CaaRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updateCaaRecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updateCaaRecordSet.withoutMetadata(del));

        //update keys with changed values
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updateCaaRecordSet.withoutMetadata((String) ele.getKey());
            updateCaaRecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
        }

        List<CaaRecord> addRecords = comparator(getCaaRecord(), oldRecord.getCaaRecord());

        for (CaaRecord arecord : addRecords) {
            updateCaaRecordSet.withRecord(arecord.getFlags(), arecord.getTag(), arecord.getValue());
        }

        List<CaaRecord> removeRecords = comparator(oldRecord.getCaaRecord(), getCaaRecord());

        for (CaaRecord rrecord : removeRecords) {
            updateCaaRecordSet.withoutRecord(rrecord.getFlags(), rrecord.getTag(), rrecord.getValue());
        }

        updateCaaRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        modify().withoutCaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "caa record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    private DnsZone.Update modify() {
        Azure client = createClient();

        DnsZoneResource parent = (DnsZoneResource) parent();

        DnsZone dnsZone = parent.getDnsZone(client);

        return dnsZone.update();
    }

    private List<CaaRecord> comparator(List<CaaRecord> original, List<CaaRecord> compareTo) {
        List<CaaRecord> differences = new ArrayList<>(original);

        for (CaaRecord record : original) {
            for (CaaRecord comp : compareTo) {
                if (record.getFlags().equals(comp.getFlags())
                        && record.getTag().equals(comp.getTag())
                        && record.getValue().equals(comp.getValue())) {
                    differences.remove(record);
                }
            }
        }

        return differences;
    }
}
