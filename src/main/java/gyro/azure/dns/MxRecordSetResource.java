package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.MXRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.MXRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithMXRecordMailExchangeOrAttachable;
import gyro.core.scope.State;

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
 *     azure::mx-record-set mx-record-set
 *         name: "mxrecexample"
 *         time-to-live: "4"
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *
 *         mx-record
 *            exchange: "mail.cont.com"
 *            preference: 1
 *         end
 *
 *         mx-record
 *             exchange: "mail.conto.com"
 *             preference: 2
 *         end
 *     end
 */
@Type("mx-record-set")
public class MxRecordSetResource extends AzureResource {

    private String dnsZoneId;
    private List<MxRecord> mxRecord;
    private Map<String, String> metadata;
    private String name;
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
     * The list of mx records. (Required)
     */
    @Updatable
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

        MXRecordSet mxRecordSet = client.dnsZones().getById(getDnsZoneId()).mxRecordSets().getByName(getName());

        if (mxRecordSet == null) {
            return false;
        }

        getMxRecord().clear();
        mxRecordSet.records().forEach(record -> getMxRecord().add(new MxRecord(record)));
        setMetadata(mxRecordSet.metadata());
        setName(mxRecordSet.name());
        setTimeToLive(Long.toString(mxRecordSet.timeToLive()));

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (getMxRecord() == null || getMxRecord().size() == 0) {
            throw new GyroException("At least one mx record must be provided.");
        }

        Azure client = createClient();

        MXRecordSetBlank<DnsZone.Update> defineMXRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().defineMXRecordSet(getName());

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

        DnsZone.Update attach = createMXRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateMXRecordSet updateMXRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().updateMXRecordSet(getName());

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

        DnsZone.Update parent = updateMXRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZoneId()).update().withoutMXRecordSet(getName()).apply();
    }

}
