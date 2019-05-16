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
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithARecordIPv4AddressOrAttachable;
import com.microsoft.azure.management.dns.DnsZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
 *         dns-zone: $(azure::dns-zone dns-zone-resource-example)
 *         time-to-live: "3"
 *         ipv4-addresses: ["10.0.0.1"]
 *     end
 */
public class ARecordSetResource extends AzureResource {

    private DnsZoneResource dnsZone;
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
     * The dns zone where the record set resides. (Required)
     */
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
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

    private String parentId() {
        DnsZoneResource parent = (DnsZoneResource) parent();

        return parent.getId();
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        if (getIpv4Addresses() == null || getIpv4Addresses().size() == 0) {
            throw new GyroException("At least one ipv4 address must be provided.");
        }

        Azure client = createClient();

        if (getTimeToLive() != null) {
            createARecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        ARecordSetBlank<DnsZone.Update> defineARecordSetBlank =
                getDnsZone().getDnsZone(client).defineARecordSet(getName());

        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createARecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<String> iter = getIpv4Addresses().listIterator(1);
        while (iter.hasNext()) {
            createARecordSet.withIPv4Address(iter.next());
        }

        createARecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateARecordSet updateARecordSet = getDnsZone().getDnsZone(client).updateARecordSet(getName());

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
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updateARecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updateARecordSet.withoutMetadata(del));

        //update changed keys
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updateARecordSet.withoutMetadata((String) ele.getKey());
            updateARecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
        }

        updateARecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutARecordSet(getName()).apply();
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