package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("network-security-group")
public class NetworkSecurityGroupFinder extends AzureFinder<NetworkSecurityGroup, NetworkSecurityGroupResource> {
    private String id;

    /**
     * The ID of the Network Security Group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    protected List<NetworkSecurityGroup> findAllAzure(Azure client) {
        return client.networkSecurityGroups().list();
    }

    @Override
    protected List<NetworkSecurityGroup> findAzure(Azure client, Map<String, String> filters) {
        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(filters.get("id"));
        if (networkSecurityGroup == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(networkSecurityGroup);
        }
    }
}