package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.ARecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithARecordIPv4AddressOrAttachable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an A Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     a-record-set
 *         name: "arecexample"
 *         time-to-live: "3"
 *         ipv4-addresses: ["10.0.0.1"]
 *     end
 */
public class ARecordSetResource extends AzureResource {

    private List<String> ipv4Addresses;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    public ARecordSetResource() {}

    public ARecordSetResource(ARecordSet aRecordSet) {
        setIpv4Addresses(aRecordSet.ipv4Addresses());
        setMetadata(aRecordSet.metadata());
        setName(aRecordSet.name());
        setTimeToLive(Long.toString(aRecordSet.timeToLive()));
    }

    /**
     * The ipv4 addresses associated with the record set. (Required)
     */
    @ResourceUpdatable
    public List<String> getIpv4Addresses() {
        if (ipv4Addresses == null) {
            ipv4Addresses = new ArrayList<>();
        }
        
        return ipv4Addresses;
    }

    public void setIpv4Addresses(List<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
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
    public String getName() { return name; }

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
        if (getIpv4Addresses().isEmpty()) {
            throw new GyroException("At least one ipv4 address must be provided.");
        }

        Azure client = createClient();

        ARecordSetBlank<DnsZone.Update> defineARecordSetBlank =
                ((DnsZoneResource) parentResource()).getDnsZone(client).defineARecordSet(getName());

        WithARecordIPv4AddressOrAttachable<DnsZone.Update> createARecordSet = null;
        for (String ip : getIpv4Addresses()) {
            createARecordSet = defineARecordSetBlank.withIPv4Address(ip);
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createARecordSet.withMetadata(e.getKey(), e.getValue());
        }

        if (getTimeToLive() != null) {
            createARecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        DnsZone.Update attach = createARecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateARecordSet updateARecordSet =
                ((DnsZoneResource) parentResource()).getDnsZone(client).updateARecordSet(getName());

        if (getTimeToLive() != null) {
            updateARecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        ARecordSetResource oldResource = (ARecordSetResource) current;

        List<String> addIps = new ArrayList<>(getIpv4Addresses());
        addIps.removeAll(oldResource.getIpv4Addresses());

        List<String> removeIps = new ArrayList<>(oldResource.getIpv4Addresses());
        removeIps.removeAll(getIpv4Addresses());

        for (String ip : addIps) {
            updateARecordSet.withIPv4Address(ip);
        }

        for (String ip : removeIps) {
            updateARecordSet.withoutIPv4Address(ip);
        }

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldResource.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateARecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateARecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateARecordSet.withoutMetadata(ele.getKey());
            updateARecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        DnsZone.Update parent = updateARecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        ((DnsZoneResource) parentResource()).getDnsZone(client).withoutARecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "a record set " + getName();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
