package gyro.azure.resources;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("resource-group")
public class ResourceGroupFinder extends AzureFinder<ResourceGroup, ResourceGroupResource> {
    private String name;

    /**
     * The name of the Resource Group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<ResourceGroup> findAllAzure(Azure client) {
        return client.resourceGroups().list();
    }

    @Override
    protected List<ResourceGroup> findAzure(Azure client, Map<String, String> filters) {
        if (client.resourceGroups().contain(filters.get("name"))) {
            return Collections.singletonList(client.resourceGroups().getByName(filters.get("name")));
        } else {
            return Collections.emptyList();
        }
    }
}