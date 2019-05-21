package gyro.azure;

import gyro.core.GyroException;
import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;

import java.io.IOException;

public abstract class AzureResource extends Resource {
    private Azure client;

    protected Azure createClient() {
        if (client == null) {
            AzureCredentials azureCredentials = (AzureCredentials) credentials();
            Azure.Authenticated authenticated = azureCredentials.findCredentials(true);

            try {
                if (authenticated != null) {
                    client = authenticated.withDefaultSubscription();
                }
            } catch (IOException ex) {
                throw new GyroException(ex.getMessage(), ex);
            }
        }

        return client;
    }

    protected String getRegion() {
        AzureCredentials azureCredentials = (AzureCredentials) credentials();
        return azureCredentials.getRegion();
    }
}
