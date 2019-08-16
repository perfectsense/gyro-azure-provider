package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("network")
public class NetworkFinder extends AzureFinder<Network, NetworkResource> {
    private String id;

    /**
     * The ID of the Network.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Network> findAllAzure(Azure client) {
        return client.networks().list();
    }

    @Override
    protected List<Network> findAzure(Azure client, Map<String, String> filters) {
        Network network = client.networks().getById(filters.get("id"));
        if (network == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(network);
        }
    }
}
