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
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithAaaaRecordIPv6AddressOrAttachable;
import inet.ipaddr.IPAddressString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
        if (getIpv6Addresses() == null || getIpv6Addresses().size() == 0) {
            throw new GyroException("At least one ipv4 address must be provided.");
        }

        Azure client = createClient();

        AaaaRecordSetBlank<DnsZone.Update> defineAaaaRecordSet =
                getDnsZone().getDnsZone(client).defineAaaaRecordSet(getName());


        if (getTimeToLive() != null) {
            createAaaaRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createAaaaRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        ListIterator<String> iter = getIpv6Addresses().listIterator(1);
        while (iter.hasNext()) {
            createAaaaRecordSet.withIPv6Address(iter.next());
        }

        createAaaaRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateAaaaRecordSet updateAaaaRecordSet =
                getDnsZone().getDnsZone(client).updateAaaaRecordSet(getName());

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
        diff.entriesOnlyOnRight().entrySet().forEach(ele -> updateAaaaRecordSet.withMetadata(ele.getKey(), ele.getValue()));
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(del -> updateAaaaRecordSet.withoutMetadata(del));

        //update changed keys
        //delete
        for (Map.Entry ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = (MapDifference.ValueDifference<String>) ele.getValue();
            updateAaaaRecordSet.withoutMetadata((String) ele.getKey());
            updateAaaaRecordSet.withMetadata((String) ele.getKey(), disc.rightValue());
        }

        updateAaaaRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutAaaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() { return "aaaa record set " + getName(); }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    private List<String> addLeadingZeros(List<String> addresses) {
        List<String> results = new ArrayList<>();

        for (String ip : addresses) {
            IPAddressString addrString = new IPAddressString(ip);
            results.add(addrString.getAddress().toFullString());
        }

        return results;
    }
}
