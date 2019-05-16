package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.CNameRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithCNameRecordSetAttachable;

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
 *     cname-record-set
 *         name: "cname-rec-example"
 *         time-to-live: "5"
 *         alias: "cname-rec-alias"
 *         dns-zone: $(azure::dns-zone dns-zone-resource-example)
 *     end
 */
public class CnameRecordSetResource extends AzureResource {

    private String alias;
    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    public CnameRecordSetResource() {}

    public CnameRecordSetResource(CNameRecordSet cNameRecordSet) {
        setAlias(cNameRecordSet.canonicalName());
        setMetadata(cNameRecordSet.metadata());
        setName(cNameRecordSet.name());
        setTimeToLive(Long.toString(cNameRecordSet.timeToLive()));
    }

    /**
     * The alias for the record. (Required)
     */
    @ResourceUpdatable
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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
        if (getAlias() == null) {
            throw new GyroException("An alias must be provided.");
        }

        Azure client = createClient();

        WithCNameRecordSetAttachable<DnsZone.Update> createCNameRecordSet =
                getDnsZone().getDnsZone(client).defineCNameRecordSet(getName()).withAlias(getAlias());

        if (getTimeToLive() != null) {
            createCNameRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCNameRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        createCNameRecordSet.attach().apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        DnsRecordSet.UpdateCNameRecordSet updateCNameRecordSet =
                modify().updateCNameRecordSet(getName());

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

        updateCNameRecordSet.parent().apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        getDnsZone().getDnsZone(client).withoutCaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "cname record set " + getName();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
