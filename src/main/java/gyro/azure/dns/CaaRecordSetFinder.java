package gyro.azure.dns;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.CaaRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("caa-record-set")
public class CaaRecordSetFinder extends AzureFinder<CaaRecordSet, CaaRecordSetResource> {
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
     * The Name of the Caa Record Set.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<CaaRecordSet> findAllAzure(Azure client) {
        return client.dnsZones().list().stream().map(o -> o.caaRecordSets().list()).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    protected List<CaaRecordSet> findAzure(Azure client, Map<String, String> filters) {
        DnsZone dnsZone = client.dnsZones().getById(filters.get("dns-zone-id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            if (filters.containsKey("name")) {
                return Collections.singletonList(dnsZone.caaRecordSets().getByName(filters.get("name")));
            } else {
                return dnsZone.caaRecordSets().list();
            }
        }
    }
}
