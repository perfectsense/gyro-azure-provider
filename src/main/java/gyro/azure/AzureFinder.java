package gyro.azure;

import com.microsoft.azure.management.Azure;
import gyro.core.finder.Finder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AzureFinder<M, R extends AzureResource> extends Finder<R> {
    protected abstract List<M> findAllAzure(Azure client);

    protected abstract List<M> findAzure(Azure client, Map<String, String> filters);

    @Override
    public List<R> find(Map<String, String> filters) {
        return findAzure(newClient(), filters).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    @Override
    public List<R> findAll() {
        return findAllAzure(newClient()).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    private Azure newClient() {
        return AzureResource.createClient(credentials(AzureCredentials.class));
    }

    @SuppressWarnings("unchecked")
    private R newResource(M model) {
        R resource = newResource();

        if (resource instanceof Copyable) {
            ((Copyable<M>) resource).copyFrom(model);
        }

        return resource;
    }
}
