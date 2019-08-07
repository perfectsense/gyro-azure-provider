package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("load-balancer")
public class LoadBalancerFinder extends AzureFinder<LoadBalancer, LoadBalancerResource> {
    private String id;

    /**
     * The ID of the Load Balancer.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<LoadBalancer> findAllAzure(Azure client) {
        return client.loadBalancers().list();
    }

    @Override
    protected List<LoadBalancer> findAzure(Azure client, Map<String, String> filters) {
        LoadBalancer loadBalancer = client.loadBalancers().getById(filters.get("id"));
        if (loadBalancer == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(loadBalancer);
        }
    }
}
