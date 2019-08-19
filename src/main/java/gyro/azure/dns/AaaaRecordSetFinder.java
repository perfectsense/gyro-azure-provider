package gyro.azure.dns;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("aaaa-record-set")
public class AaaaRecordSetFinder extends AzureFinder<AaaaRecordSet, AaaaRecordSetResource> {
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
     * The Name of the Aaaa Record Set.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<AaaaRecordSet> findAllAzure(Azure client) {
        return client.dnsZones().list().stream().map(o -> o.aaaaRecordSets().list()).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    protected List<AaaaRecordSet> findAzure(Azure client, Map<String, String> filters) {
        DnsZone dnsZone = client.dnsZones().getById(filters.get("dns-zone-id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            if (filters.containsKey("name")) {
                return Collections.singletonList(dnsZone.aaaaRecordSets().getByName(filters.get("name")));
            } else {
                return dnsZone.aaaaRecordSets().list();
            }
        }
    }
}
