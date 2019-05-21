package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
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
 *         time-to-live: "5"
 *         alias: "cnamerecalias"
 *         dns-zone-id: $(azure::dns-zone dns-zone-example-zones | id)
 *     end
 */
@Type("cname-record-set")
public class CnameRecordSetResource extends AzureResource {

    private String alias;
    private String dnsZoneId;
    private Map<String, String> metadata;
    private String name;
    private String timeToLive;

    /**
     * The alias for the record. (Required)
     */
    @Updatable
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

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

        CNameRecordSet cnameRecordSet = client.dnsZones().getById(getDnsZoneId()).cNameRecordSets().getByName(getName());

        if (cnameRecordSet == null) {
            return false;
        }

        setAlias(cnameRecordSet.canonicalName());
        setMetadata(cnameRecordSet.metadata());
        setName(cnameRecordSet.name());
        setTimeToLive(Long.toString(cnameRecordSet.timeToLive()));

        return true;
    }

    @Override
    public void create() {
        if (getAlias() == null) {
            throw new GyroException("An alias must be provided.");
        }

        Azure client = createClient();

        WithCNameRecordSetAttachable<DnsZone.Update> createCNameRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().
                        defineCNameRecordSet(getName()).withAlias(getAlias());

        if (getTimeToLive() != null) {
            createCNameRecordSet.withTimeToLive(Long.parseLong(getTimeToLive()));
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCNameRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createCNameRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateCNameRecordSet updateCNameRecordSet =
                client.dnsZones().getById(getDnsZoneId()).update().updateCNameRecordSet(getName());

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
        parent.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZoneId()).update().withoutCaaRecordSet(getName()).apply();
    }

    @Override
    public String toDisplayString() {
        return "cname record set " + getName();
    }
}
