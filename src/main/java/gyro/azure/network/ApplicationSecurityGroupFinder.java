package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationSecurityGroup;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query application security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    application-security-group: $(external-query azure::application-security-group {})
 */
@Type("application-security-group")
public class ApplicationSecurityGroupFinder extends AzureFinder<ApplicationSecurityGroup, ApplicationSecurityGroupResource> {
    private String id;

    /**
     * The ID of the Application Security Group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ApplicationSecurityGroup> findAllAzure(Azure client) {
        return client.applicationSecurityGroups().list();
    }

    @Override
    protected List<ApplicationSecurityGroup> findAzure(Azure client, Map<String, String> filters) {
        ApplicationSecurityGroup applicationSecurityGroup = client.applicationSecurityGroups().getById(filters.get("id"));
        if (applicationSecurityGroup == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(applicationSecurityGroup);
        }
    }
}
