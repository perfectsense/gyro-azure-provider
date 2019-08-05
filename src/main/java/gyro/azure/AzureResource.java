package gyro.azure;

import com.microsoft.rest.RestClient;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;
import gyro.core.scope.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    protected void closeRestClients() {
        restClients.forEach(RestClient::close);
        restClients.clear();
    }

    protected abstract boolean doRefresh();

    @Override
    public final boolean refresh() {
        boolean isRefreshed = doRefresh();
        closeRestClients();
        return isRefreshed;
    }

    protected abstract void doCreate(GyroUI gyroUI, State state);

    @Override
    public final void create(GyroUI gyroUI, State state) {
        doCreate(gyroUI, state);
        closeRestClients();
    }

    protected abstract void doUpdate(GyroUI gyroUI, State state, Resource resource, Set<String> set);

    @Override
    public final void update(GyroUI gyroUI, State state, Resource resource, Set<String> set) {
        doUpdate(gyroUI, state, resource, set);
        closeRestClients();
    }

    protected abstract void doDelete(GyroUI gyroUI, State state);

    @Override
    public final void delete(GyroUI gyroUI, State state) {
        doDelete(gyroUI, state);
        closeRestClients();
    }

}
