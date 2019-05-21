package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithTxtRecordTextValueOrAttachable;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.TxtRecordSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an TXT Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::txt-record-set txt-record-set
 *         name: "txtrecexample"
 *         txt-records: ["record1", "record2"]
 *         time-to-live: "3"
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *     end
 */
@ResourceType("txt-record-set")
public class TxtRecordSetResource extends AzureResource {

    private String dnsZoneId;
    private Map<String, String> metadata;
    private String name;
    private List<String> txtRecords;
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
     * The list of txt records. (Required)
     */
    @Updatable
    public List<String> getTxtRecords() {
        if (txtRecords == null) {
            txtRecords = new ArrayList<>();
        }

        return txtRecords;
    }

    public void setTxtRecords(List<String> txtRecords) {
        this.txtRecords = txtRecords;
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

        TxtRecordSet txtRecordSet = client.dnsZones().getById(getDnsZoneId()).txtRecordSets().getByName(getName());

        if (txtRecordSet == null) {
            return false;
        }

        getTxtRecords().clear();
        txtRecordSet.records().forEach(rec -> getTxtRecords().add(rec.value().get(0)));
        setMetadata(txtRecordSet.metadata());
        setName(txtRecordSet.name());
        setTimeToLive(Long.toString(txtRecordSet.timeToLive()));

        return true;
    }

    @Override
    public void create() {
        if (getTxtRecords().isEmpty()) {
            throw new GyroException("At least one record must be provided.");
        }

        Azure client = createClient();

        DnsRecordSet.UpdateDefinitionStages.TxtRecordSetBlank<DnsZone.Update> defineTxtRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().defineTxtRecordSet(getName());

        WithTxtRecordTextValueOrAttachable<DnsZone.Update> createTxtRecordSet = null;
        for (String txtRecord : getTxtRecords()) {
            createTxtRecordSet = defineTxtRecordSet.withText(txtRecord);
        }

        if (getTimeToLive() != null) {
            createTxtRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createTxtRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createTxtRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().updateTxtRecordSet(getName());

        if (getTimeToLive() != null) {
            updateTxtRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        TxtRecordSetResource oldRecord = (TxtRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateTxtRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateTxtRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateTxtRecordSet.withoutMetadata(ele.getKey());
            updateTxtRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<String> addRecords = new ArrayList<>(getTxtRecords());
        addRecords.removeAll(oldRecord.getTxtRecords());

        List<String> removeRecords = new ArrayList<>(oldRecord.getTxtRecords());
        removeRecords.removeAll(getTxtRecords());

        for (String addRecord : addRecords) {
            updateTxtRecordSet.withText(addRecord);
        }

        for (String removeRecord : removeRecords) {
            updateTxtRecordSet.withoutText(removeRecord);
        }

        DnsZone.Update parent = updateTxtRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZoneId()).update().withoutTxtRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "txt record set " + getName();
    }
}
