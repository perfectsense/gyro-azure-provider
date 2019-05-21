package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.AaaaRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithAaaaRecordIPv6AddressOrAttachable;

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
 *     azure::aaaa-record-set aaaa-record-set
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *         name: "aaaarecexample"
 *         ipv6-addresses: ["2001:0db8:85a3:0000:0000:8a2e:0370:7334", "2001:0db8:85a3:0000:0000:8a2e:0370:7335"]
 *     end
 */
@ResourceType("aaaa-record-set")
public class AaaaRecordSetResource extends AzureResource {

    private String dnsZoneId;
    private List<String> ipv6Addresses;
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
     * The ipv6 addresses associated with the record set. (Required)
     */
    @Updatable
    public List<String> getIpv6Addresses() {
        if (ipv6Addresses == null) {
            ipv6Addresses = new ArrayList<>();
        }

        ipv6Addresses = expandIps(ipv6Addresses);
        return ipv6Addresses;
    }

    public void setIpv6Addresses(List<String> ipv6Addresses) {
        this.ipv6Addresses = ipv6Addresses;
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

        AaaaRecordSet aaaaRecordSet = client.dnsZones().getById(getDnsZoneId()).aaaaRecordSets().getByName(getName());

        if (aaaaRecordSet == null) {
            return false;
        }

        getIpv6Addresses().clear();
        setIpv6Addresses(aaaaRecordSet.ipv6Addresses());
        setMetadata(aaaaRecordSet.metadata());
        setName(aaaaRecordSet.name());
        setTimeToLive(Long.toString(aaaaRecordSet.timeToLive()));

        return true;
    }

    @Override
    public void create() {
        if (getIpv6Addresses().isEmpty()) {
            throw new GyroException("At least one ipv6 address must be provided.");
        }

        Azure client = createClient();

        AaaaRecordSetBlank<DnsZone.Update> defineAaaaRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().defineAaaaRecordSet(getName());

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
                client.dnsZones().getById(getDnsZoneId()).update().updateAaaaRecordSet(getName());

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

        client.dnsZones().getById(getDnsZoneId()).update().withoutAaaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "aaaa record set " + getName();
    }

    private List<String> expandIps(List<String> addresses) {
        List<String> expandedIps = new ArrayList<>();

        for (String ip : addresses) {
            expandedIps.add(adjustIp(ip));
        }

        return expandedIps;
    }

    private String adjustIp(String ip) {
        StringBuilder expandedIp = new StringBuilder();
        String [] splitDouble = ip.split("::");

        if (splitDouble.length == 1) {
            expandedIp = expandSegments(expandedIp, splitDouble[0].split(":"));
        } else {

            String[] firstHalf = splitDouble[0].split(":");
            String[] secondHalf = splitDouble[1].split(":");

            // take the first half and format
            expandedIp = expandSegments(expandedIp, firstHalf);

            //add the zeros to the place the double colon was found
            int addZeros = 8 - firstHalf.length - secondHalf.length;
            for (int i = 0; i < addZeros; i++) {
                expandedIp.append("0000");
            }

            // take the second half and format
            expandedIp = expandSegments(expandedIp, secondHalf);
        }

        int count = 0, offset = 4;
        while (count < 7) {
            expandedIp.insert(offset, ":");
            count++;
            offset+=5;
        }

        return expandedIp.toString();
    }



    private StringBuilder expandSegments(StringBuilder builder, String[] segments){
        for (String first : segments) {
            if (first.length() < 4) {
                builder.append(addLeadingZeros(first));
            } else {
                builder.append(first);
            }
        }

        return builder;
    }

    private String addLeadingZeros(String bitBlock) {
        StringBuilder zeros = new StringBuilder();

        int offset = 4 - bitBlock.length();

        for (int i = 0; i < offset; i++) {
            zeros.insert(0, "0");
        }

        zeros.append(bitBlock);

        return zeros.toString();
    }
}
