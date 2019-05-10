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

public class TxtRecordSetResource extends AzureResource {

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
    public List<String> getTxtRecords() {
        if (txtRecords == null) {
            txtRecords = new ArrayList<>();
        }

        return txtRecords;
    }

    public void setTxtRecords(List<String> txtRecords) {
        this.txtRecords = txtRecords;
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
        if (getTxtRecords() == null || getTxtRecords().size() == 0) {
            throw new GyroException("At least one record must be provided.");
        }
        
        String firstRecord = getTxtRecords().get(0);

        WithTxtRecordTextValueOrAttachable<DnsZone.Update> createTxtRecordSet =
                modify().defineTxtRecordSet(getName()).withText(firstRecord);

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
        DnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet = modify().updateTxtRecordSet(getName());


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

        for (String arecord : addRecords) {
            updateTxtRecordSet.withText(arecord);
        }

        for (String rrecord : removeRecords) {
            updateTxtRecordSet.withoutText(rrecord);
        }

        updateTxtRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        modify().withoutTxtRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "txt record set " + getName(); }

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
}
