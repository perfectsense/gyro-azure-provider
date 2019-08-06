package gyro.azure;

import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;

public abstract class AzureResource extends Resource {

    protected static Azure createClient(AzureCredentials credentials) {
        return credentials.createClient();
    }

    protected Azure createClient() {
        return AzureResource.createClient(credentials(AzureCredentials.class));
    }

    protected String getRegion() {
        return credentials(AzureCredentials.class).getRegion();
    }

}
