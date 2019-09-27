package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkInterface;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query network interface.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    network-interface: $(external-query azure::network-interface {})
 */
@Type("network-interface")
public class NetworkInterfaceFinder extends AzureFinder<NetworkInterface, NetworkInterfaceResource> {
    private String id;

    /**
     * The ID of the Network Interface.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<NetworkInterface> findAllAzure(Azure client) {
        return client.networkInterfaces().list();
    }

    @Override
    protected List<NetworkInterface> findAzure(Azure client, Map<String, String> filters) {
        NetworkInterface networkInterface = client.networkInterfaces().getById(filters.get("id"));
        if (networkInterface == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(networkInterface);
        }
    }
}
