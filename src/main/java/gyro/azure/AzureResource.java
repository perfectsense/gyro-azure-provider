package gyro.azure;

import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;

public abstract class AzureResource extends Resource {

    protected static AzureClient createClient(AzureCredentials credentials) {
        return credentials.createClient();
    }

    protected Azure createClient() {
        AzureClient azureClient = AzureResource.createClient(credentials(AzureCredentials.class));
        return azureClient.getClient();
    }

    protected String getRegion() {
        return credentials(AzureCredentials.class).getRegion();
    }

}
