package gyro.azure;

import gyro.core.GyroException;
import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.io.IOException;

public abstract class AzureResource extends Resource {
    private Azure client;

    protected Azure createClient() {
        if (client == null) {
            AzureCredentials azureCredentials = (AzureCredentials) resourceCredentials();

            try {
                client = Azure.configure()
                    .withLogLevel(LogLevel.valueOf(azureCredentials.getLogLevel()))
                    .authenticate(new File(azureCredentials.getCredentialFilePath()))
                    .withDefaultSubscription();
            } catch (IOException e) {
                throw new GyroException("File not found");
            }
        }

        return client;
    }

    protected String getRegion() {
        AzureCredentials azureCredentials = (AzureCredentials) resourceCredentials();
        return azureCredentials.getRegion();
    }
}
