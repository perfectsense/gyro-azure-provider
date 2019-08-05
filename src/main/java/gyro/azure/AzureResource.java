package gyro.azure;

import com.microsoft.rest.RestClient;
import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;

import java.util.ArrayList;
import java.util.List;

public abstract class AzureResource extends Resource {

    private List<RestClient> restClients = new ArrayList<>();

    protected static AzureClient createClient(AzureCredentials credentials) {
        return credentials.createClient();
    }

    protected Azure createClient() {
        AzureClient azureClient = AzureResource.createClient(credentials(AzureCredentials.class));
        restClients.add(azureClient.getRestClient());
        return azureClient.getClient();
    }

    protected String getRegion() {
        return credentials(AzureCredentials.class).getRegion();
    }

}
