package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.PublicIPAddress;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query public ip address.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    public-ip-address: $(external-query azure::public-ip-address {})
 */
@Type("public-ip-address")
public class PublicIpAddressFinder extends AzureFinder<PublicIPAddress, PublicIpAddressResource> {
    private String id;

    /**
     * The ID of the Public IP Address.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<PublicIPAddress> findAllAzure(Azure client) {
        return client.publicIPAddresses().list();
    }

    @Override
    protected List<PublicIPAddress> findAzure(Azure client, Map<String, String> filters) {
        PublicIPAddress publicIPAddress = client.publicIPAddresses().getById(filters.get("id"));
        if (publicIPAddress == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(publicIPAddress);
        }
    }
}
