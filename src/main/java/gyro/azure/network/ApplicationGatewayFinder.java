package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("application-gateway")
public class ApplicationGatewayFinder extends AzureFinder<ApplicationGateway, ApplicationGatewayResource> {
    private String id;

    /**
     * The ID of the Application Gateway.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ApplicationGateway> findAllAzure(Azure client) {
        return client.applicationGateways().list();
    }

    @Override
    protected List<ApplicationGateway> findAzure(Azure client, Map<String, String> filters) {
        ApplicationGateway applicationGateway = client.applicationGateways().getById(filters.get("id"));
        if (applicationGateway == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(applicationGateway);
        }
    }
}
