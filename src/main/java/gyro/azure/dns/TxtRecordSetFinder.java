package gyro.azure.dns;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.TxtRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("txt-record-set")
public class TxtRecordSetFinder extends AzureFinder<TxtRecordSet, TxtRecordSetResource> {
    private String dnsZoneId;
    private String name;

    /**
     * The ID of the DNS Zone.
     */
    public String getDnsZoneId() {
        return dnsZoneId;
    }

    public void setDnsZoneId(String dnsZoneId) {
        this.dnsZoneId = dnsZoneId;
    }

    /**
     * The Name of the Txt Record Set.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<TxtRecordSet> findAllAzure(Azure client) {
        return client.dnsZones().list().stream().map(o -> o.txtRecordSets().list()).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    protected List<TxtRecordSet> findAzure(Azure client, Map<String, String> filters) {
        if (ObjectUtils.isBlank(filters.get("dns-zone-id"))) {
            throw new GyroException("'dns-zone-id' is required.");
        }

        DnsZone dnsZone = client.dnsZones().getById(filters.get("dns-zone-id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            if (filters.containsKey("name")) {
                return Collections.singletonList(dnsZone.txtRecordSets().getByName(filters.get("name")));
            } else {
                return dnsZone.txtRecordSets().list();
            }
        }
    }
}
