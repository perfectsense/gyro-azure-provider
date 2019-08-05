package gyro.azure;

import com.microsoft.azure.management.Azure;
import com.microsoft.rest.RestClient;

public class AzureClient {

    private final Azure client;
    private final RestClient restClient;

    public AzureClient(Azure client, RestClient restClient) {
        this.client = client;
        this.restClient = restClient;
    }

    public Azure getClient() {
        return client;
    }

    public RestClient getRestClient() {
        return restClient;
    }
}
