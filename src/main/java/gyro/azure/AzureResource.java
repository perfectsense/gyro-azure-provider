package gyro.azure;

import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;

public abstract class AzureResource extends Resource {

    protected Azure createClient() {
        return ((AzureCredentials) credentials()).createClient();
    }

    protected String getRegion() {
        return ((AzureCredentials) credentials()).getRegion();
    }

}
