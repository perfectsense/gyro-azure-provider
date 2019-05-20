package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.AaaaRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithAaaaRecordIPv6AddressOrAttachable;
import inet.ipaddr.IPAddressString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an AAAA Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aaaa-record-set
 *         name: "aaaarecexample"
 *         ipv6-addresses: ["2001:0db8:85a3:0000:0000:8a2e:0370:7334", "2001:0db8:85a3:0000:0000:8a2e:0370:7335"]
 *     end
 */
public class AaaaRecordSetResource extends AzureResource {
    private List<String> ipv6Addresses;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    public AaaaRecordSetResource() {}

    public AaaaRecordSetResource (AaaaRecordSet aaaaRecordSet) {
        setIpv6Addresses(aaaaRecordSet.ipv6Addresses());
        setMetadata(aaaaRecordSet.metadata());
        setName(aaaaRecordSet.name());
        setTimeToLive(Long.toString(aaaaRecordSet.timeToLive()));
    }

    /**
     * The ipv6 addresses associated with the record set. (Required)
     */
    @ResourceUpdatable
    public List<String> getIpv6Addresses() {
        if (ipv6Addresses == null) {
            ipv6Addresses = new ArrayList<>();
        }

        ipv6Addresses = addLeadingZeros(ipv6Addresses);
        return ipv6Addresses;
    }

    public void setIpv6Addresses(List<String> ipv6Addresses) {
        this.ipv6Addresses = ipv6Addresses;
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
        if (getIpv6Addresses().isEmpty()) {
            throw new GyroException("At least one ipv4 address must be provided.");
        }

        Azure client = createClient();

        AaaaRecordSetBlank<DnsZone.Update> defineAaaaRecordSet =
                ((DnsZoneResource) parentResource()).getDnsZone(client).defineAaaaRecordSet(getName());

        WithAaaaRecordIPv6AddressOrAttachable<DnsZone.Update> createAaaaRecordSet = null;
        for (String ip : getIpv6Addresses()) {
            createAaaaRecordSet = defineAaaaRecordSet.withIPv6Address(ip);
        }

        if (getTimeToLive() != null) {
            createAaaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createAaaaRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createAaaaRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateAaaaRecordSet updateAaaaRecordSet =
                ((DnsZoneResource) parentResource()).getDnsZone(client).updateAaaaRecordSet(getName());

        if (getTimeToLive() != null) {
            updateAaaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        AaaaRecordSetResource oldResource = (AaaaRecordSetResource) current;

        List<String> addIps = new ArrayList<>(getIpv6Addresses());
        addIps.removeAll(oldResource.getIpv6Addresses());

        List<String> removeIps = new ArrayList<>(oldResource.getIpv6Addresses());
        removeIps.removeAll(getIpv6Addresses());

        for (String ip : addIps) {
            updateAaaaRecordSet.withIPv6Address(ip);
        }

        for (String ip : removeIps) {
            updateAaaaRecordSet.withoutIPv6Address(ip);
        }

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldResource.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateAaaaRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateAaaaRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateAaaaRecordSet.withoutMetadata(ele.getKey());
            updateAaaaRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        DnsZone.Update parent = updateAaaaRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        ((DnsZoneResource) parentResource()).getDnsZone(client).withoutAaaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "aaaa record set " + getName();
    }

    @Override
    public String primaryKey() {
        return name;
    }

    private List<String> addLeadingZeros(List<String> addresses) {
        List<String> results = new ArrayList<>();

        for (String ip : addresses) {
            IPAddressString addressString = new IPAddressString(ip);
            results.add(addressString.getAddress().toFullString());
        }

        return results;
    }
}
