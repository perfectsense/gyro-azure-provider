package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.ARecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithARecordIPv4AddressOrAttachable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 *     azure::a-record-set a-record-set
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *         name: "arecexample"
 *         ttl: 3
 *         ipv4-addresses: ["10.0.0.1"]
 *     end
 */
@Type("a-record-set")
public class ARecordSetResource extends AzureResource implements Copyable<ARecordSet> {

    private DnsZoneResource dnsZone;
    private Set<String> ipv4Addresses;
    private Map<String, String> metadata;
    private String name;
    private Long ttl;
    private String id;

    /**
     * The DNS Zone where the A Record Set resides. (Required)
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The ipv4 addresses associated with the A Record Set set. (Required)
     */
    @Required
    @Updatable
    public Set<String> getIpv4Addresses() {
        if (ipv4Addresses == null) {
            ipv4Addresses = new HashSet<>();
        }

        return ipv4Addresses;
    }

    public void setIpv4Addresses(Set<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
    }

    /**
     * The metadata for the A Record Set. (Optional)
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
     * The name of the A Record Set. (Required)
     */
    @Required
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live in Seconds for the A Record Set in the set. (Required)
     */
    @Required
    @Updatable
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * The ID of the A Record Set.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(ARecordSet aRecordSet) {
        getIpv4Addresses().clear();
        setIpv4Addresses(new HashSet<>(aRecordSet.ipv4Addresses()));
        setMetadata(aRecordSet.metadata());
        setName(aRecordSet.name());
        setTtl(aRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, aRecordSet.parent().id()));
        setId(aRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ARecordSet aRecordSet = client.dnsZones().getById(getDnsZone().getId()).aRecordSets().getByName(getName());

        if (aRecordSet == null) {
            return false;
        }

        copyFrom(aRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        ARecordSetBlank<DnsZone.Update> defineARecordSetBlank =
                client.dnsZones().getById(getDnsZone().getId()).update().defineARecordSet(getName());

        WithARecordIPv4AddressOrAttachable<DnsZone.Update> createARecordSet = null;
        for (String ip : getIpv4Addresses()) {
            createARecordSet = defineARecordSetBlank.withIPv4Address(ip);
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createARecordSet.withMetadata(e.getKey(), e.getValue());
        }

        if (getTtl() != null) {
            createARecordSet.withTimeToLive(getTtl());
        }

        DnsZone.Update attach = createARecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.aRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateARecordSet updateARecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateARecordSet(getName());

        if (getTtl() != null) {
            updateARecordSet.withTimeToLive(getTtl());
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
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.aRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutARecordSet(getName()).apply();
    }
}
