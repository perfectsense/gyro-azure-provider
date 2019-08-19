package gyro.azure.dns;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsZone;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("dns-zone")
public class DnsZoneFinder extends AzureFinder<DnsZone, DnsZoneResource> {
    private String id;

    /**
     * The ID of the DNS Zone.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<DnsZone> findAllAzure(Azure client) {
        return client.dnsZones().list();
    }

    @Override
    protected List<DnsZone> findAzure(Azure client, Map<String, String> filters) {
        DnsZone dnsZone = client.dnsZones().getById(filters.get("id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(dnsZone);
        }
    }
}
