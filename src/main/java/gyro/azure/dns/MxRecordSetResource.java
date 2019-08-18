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
import com.microsoft.azure.management.dns.MXRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.MXRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithMXRecordMailExchangeOrAttachable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 *         time-to-live: 4
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
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
public class MxRecordSetResource extends AzureResource implements Copyable<MXRecordSet> {

    private DnsZoneResource dnsZone;
    private Set<MxRecord> mxRecord;
    private Map<String, String> metadata;
    private String name;
    private Long timeToLive;
    private String id;

    /**
     * The DNS Zone where the MX Record Set resides. (Required)
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The list of mx MX Records Set. (Required)
     */
    @Required
    @Updatable
    public Set<MxRecord> getMxRecord() {
        if (mxRecord == null) {
            mxRecord = new HashSet<>();
        }

        return mxRecord;
    }

    public void setMxRecord(Set<MxRecord> mxRecord) {
        this.mxRecord = mxRecord;
    }

    /**
     * The metadata for the MX Record Set. (Optional)
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
     * The name of the MX Record Set. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live for the MX Record Set in the set. (Required)
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
     * The ID of the MX Record Set.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(MXRecordSet mxRecordSet) {
        setMxRecord(mxRecordSet.records().stream().map(o -> {
            MxRecord mxRecord = newSubresource(MxRecord.class);
            mxRecord.copyFrom(o);
            return mxRecord;
        }).collect(Collectors.toSet()));
        setMetadata(mxRecordSet.metadata());
        setName(mxRecordSet.name());
        setTimeToLive(mxRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, mxRecordSet.parent().id()));
        setId(mxRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        MXRecordSet mxRecordSet = client.dnsZones().getById(getDnsZone().getId()).mxRecordSets().getByName(getName());

        if (mxRecordSet == null) {
            return false;
        }

        copyFrom(mxRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        MXRecordSetBlank<DnsZone.Update> defineMXRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().defineMXRecordSet(getName());

        WithMXRecordMailExchangeOrAttachable<DnsZone.Update> createMXRecordSet = null;
        for (MxRecord mxRecord : getMxRecord()) {
            createMXRecordSet = defineMXRecordSet.withMailExchange(mxRecord.getExchange(), mxRecord.getPreference());
        }

        if (getTimeToLive() != null) {
            createMXRecordSet.withTimeToLive(getTimeToLive());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createMXRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createMXRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.mxRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateMXRecordSet updateMXRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateMXRecordSet(getName());

        if (getTimeToLive() != null) {
            updateMXRecordSet.withTimeToLive(getTimeToLive());
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
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.mxRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutMXRecordSet(getName()).apply();
    }
}
