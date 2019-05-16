package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;

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
import java.util.ListIterator;
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
 *     txt-record-set
 *         name: "txtexample"
 *         txt-records: ["record1", "record2"]
 *         time-to-live: "3"
 *     end
 */
public class TxtRecordSetResource extends AzureResource {

    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private List<String> txtRecords;
    private String timeToLive;

    public TxtRecordSetResource() {}

    public TxtRecordSetResource(TxtRecordSet txtRecordSet) {
        txtRecordSet.records().forEach(rec -> getTxtRecords().add(rec.value().get(0)));
        setMetadata(txtRecordSet.metadata());
        setName(txtRecordSet.name());
        setTimeToLive(Long.toString(txtRecordSet.timeToLive()));
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
     * The list of txt records. (Required)
     */
    @ResourceUpdatable
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
        if (getTxtRecords().isEmpty()) {
            throw new GyroException("At least one record must be provided.");
        }

        String firstRecord = getTxtRecords().get(0);
        Azure client = createClient();

        DnsRecordSet.UpdateDefinitionStages.TxtRecordSetBlank<DnsZone.Update> defineTxtRecordSet =
                getDnsZone().getDnsZone(client).defineTxtRecordSet(getName());


        if (getTimeToLive() != null) {
            createTxtRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createTxtRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<String> iter = getTxtRecords().listIterator(1);
        while (iter.hasNext()) {
            String txtRecord = iter.next();
            createTxtRecordSet.withText(txtRecord);
        }

        createTxtRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet =
                getDnsZone().getDnsZone(client).updateTxtRecordSet(getName());

        if (getTimeToLive() != null) {
            updateTxtRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        TxtRecordSetResource oldRecord = (TxtRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updateTxtRecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updateTxtRecordSet.withoutMetadata(del));

        //update keys with changed values
        //delete
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updateTxtRecordSet.withoutMetadata((String) ele.getKey());
            updateTxtRecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
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

        updateTxtRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutTxtRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "txt record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
