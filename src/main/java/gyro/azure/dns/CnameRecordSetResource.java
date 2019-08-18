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
import com.microsoft.azure.management.dns.CNameRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithCNameRecordSetAttachable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates an CNAME Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cname-record-set
 *         name: "cnamerecexample"
 *         time-to-live: 5
 *         alias: "cnamerecalias"
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *     end
 */
@Type("cname-record-set")
public class CnameRecordSetResource extends AzureResource implements Copyable<CNameRecordSet> {

    private String alias;
    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;
    private String id;

    /**
     * The alias for the Cname Record Set. (Required)
     */
    @Required
    @Updatable
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The DNS Zone where the Cname Record Set resides. (Required)
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The metadata for the Cname Record Set. (Optional)
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
     * The name of the Cname Record Set. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live for the Cname Records Set in the set. (Required)
     */
    @Required
    @Updatable
    public String getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(String timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * The ID of the Cname Record Set.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(CNameRecordSet cnameRecordSet) {
        setAlias(cnameRecordSet.canonicalName());
        setMetadata(cnameRecordSet.metadata());
        setName(cnameRecordSet.name());
        setTimeToLive(Long.toString(cnameRecordSet.timeToLive()));
        setDnsZone(findById(DnsZoneResource.class, cnameRecordSet.parent().id()));
        setId(cnameRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        CNameRecordSet cnameRecordSet = client.dnsZones().getById(getDnsZone().getId()).cNameRecordSets().getByName(getName());

        if (cnameRecordSet == null) {
            return false;
        }

        copyFrom(cnameRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        WithCNameRecordSetAttachable<DnsZone.Update> createCNameRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().
                        defineCNameRecordSet(getName()).withAlias(getAlias());

        if (getTimeToLive() != null) {
            createCNameRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCNameRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createCNameRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.cNameRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateCNameRecordSet updateCNameRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateCNameRecordSet(getName());

        if (getAlias() != null) {
            updateCNameRecordSet.withAlias(getAlias());
        }

        if (getTimeToLive() != null) {
            updateCNameRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        CnameRecordSetResource oldResource = (CnameRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldResource.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateCNameRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateCNameRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateCNameRecordSet.withoutMetadata(ele.getKey());
            updateCNameRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        DnsZone.Update parent = updateCNameRecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.cNameRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutCaaRecordSet(getName()).apply();
    }
}
