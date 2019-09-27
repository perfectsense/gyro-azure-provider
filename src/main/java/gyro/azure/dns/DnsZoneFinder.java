package gyro.azure.dns;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsZone;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query dns zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dns-zone: $(external-query azure::dns-zone {})
 */
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
        if (ObjectUtils.isBlank(filters.get("id"))) {
            throw new GyroException("'id' is required.");
        }

        DnsZone dnsZone = client.dnsZones().getById(filters.get("id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(dnsZone);
        }
    }
}
