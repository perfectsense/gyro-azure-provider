package gyro.azure.accessmanagement;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query active directory group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    active-directory-group: $(external-query azure::active-directory-group {name: "gyro"})
 */
@Type("active-directory-group")
public class ActiveDirectoryGroupFinder extends AzureFinder<ActiveDirectoryGroup, ActiveDirectoryGroupResource> {
    private String name;
    private String id;

    /**
     * The name of the group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ActiveDirectoryGroup> findAllAzure(Azure client) {
        return client.accessManagement().activeDirectoryGroups().list();
    }

    @Override
    protected List<ActiveDirectoryGroup> findAzure(Azure client, Map<String, String> filters) {
        ActiveDirectoryGroup group = null;
        if (filters.containsKey("id")) {
            group = client.accessManagement().activeDirectoryGroups().getById(filters.get("id"));
        } else if (filters.containsKey("name")) {
            group = client.accessManagement().activeDirectoryGroups().getByName(filters.get("name"));
        } else {
            throw new GyroException("Either 'id' or 'name' is required");
        }

        if (group != null) {
            return Collections.singletonList(group);
        } else {
            return Collections.emptyList();
        }
    }
}
