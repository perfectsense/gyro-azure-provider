package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.CaaRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithCaaRecordEntryOrAttachable;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.CaaRecordSetBlank;
import gyro.core.scope.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an CAA Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::caa-record-set caa-record-set
 *         name: "caaexample"
 *         time-to-live: "3"
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *
 *         caa-record
 *             flags: 1
 *             tag: "tag1"
 *             value: "val1"
 *         end
 *
 *         caa-record
 *             flags: 2
 *             tag: "tag2"
 *             value: "val2"
 *         end
 *     end
 */
@Type("caa-record-set")
public class CaaRecordSetResource extends AzureResource {

    private List<CaaRecord> caaRecord;
    private String dnsZoneId;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    /**
     * The Caa records associated with the record. (Required)
     */
    @Updatable
    public List<CaaRecord> getCaaRecord() {
        if (caaRecord == null) {
            caaRecord = new ArrayList<>();
        }

        return caaRecord;
    }

    public void setCaaRecord(List<CaaRecord> caaRecord) {
        this.caaRecord = caaRecord;
    }

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

        CaaRecordSet caaRecordSet = client.dnsZones().getById(getDnsZoneId()).caaRecordSets().getByName(getName());

        getCaaRecord().clear();
        caaRecordSet.records().forEach(record -> getCaaRecord().add(new CaaRecord(record)));
        setMetadata(caaRecordSet.metadata());
        setName(caaRecordSet.name());
        setTimeToLive(Long.toString(caaRecordSet.timeToLive()));

        return true;
    }

    @Override
    public void create(State state) {
        if (getCaaRecord().isEmpty()) {
            throw new GyroException("At least one caa record must be provided.");
        }

        Azure client = createClient();

        CaaRecordSetBlank<DnsZone.Update> defineCaaRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().defineCaaRecordSet(getName());

        WithCaaRecordEntryOrAttachable<DnsZone.Update> createCaaRecordSet = null;
        for (CaaRecord caaRecord : getCaaRecord()) {
            createCaaRecordSet = defineCaaRecordSet.withRecord(caaRecord.getFlags(), caaRecord.getTag(), caaRecord.getValue());
        }

        if (getTimeToLive() != null) {
            createCaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCaaRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createCaaRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateCaaRecordSet updateCaaRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().updateCaaRecordSet(getName());

        if (getTimeToLive() != null) {
            updateCaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        CaaRecordSetResource oldRecord = (CaaRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateCaaRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateCaaRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateCaaRecordSet.withoutMetadata(ele.getKey());
            updateCaaRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<CaaRecord> addRecords = comparator(getCaaRecord(), oldRecord.getCaaRecord());

        for (CaaRecord addRecord : addRecords) {
            updateCaaRecordSet.withRecord(addRecord.getFlags(), addRecord.getTag(), addRecord.getValue());
        }

        List<CaaRecord> removeRecords = comparator(oldRecord.getCaaRecord(), getCaaRecord());

        for (CaaRecord removeRecord : removeRecords) {
            updateCaaRecordSet.withoutRecord(removeRecord.getFlags(), removeRecord.getTag(), removeRecord.getValue());
        }

        DnsZone.Update parent = updateCaaRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete(State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZoneId()).update().withoutCaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "caa record set " + getName();
    }

    private List<CaaRecord> comparator(List<CaaRecord> original, List<CaaRecord> compareTo) {
        List<CaaRecord> differences = new ArrayList<>(original);

        for (CaaRecord record : original) {
            for (CaaRecord comp : compareTo) {
                if (record.primaryKey().equals(comp.primaryKey())) {
                    differences.remove(record);
                }
            }
        }

        return differences;
    }
}
