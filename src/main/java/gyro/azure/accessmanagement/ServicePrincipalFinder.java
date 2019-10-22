package gyro.azure.accessmanagement;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query service-principal.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    service-principal: $(external-query azure::service-principal {name: "gyro"})
 */
@Type("service-principal")
public class ServicePrincipalFinder extends AzureFinder<ServicePrincipal, ServicePrincipalResource> {
    private String name;
    private String id;

    /**
     * The name of the service principal.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the service principal.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<ServicePrincipal> findAllAzure(Azure client) {
        return client.accessManagement().servicePrincipals().list();
    }

    @Override
    protected List<ServicePrincipal> findAzure(Azure client, Map<String, String> filters) {
        ServicePrincipal servicePrincipal = null;
        if (filters.containsKey("id")) {
            servicePrincipal = client.accessManagement().servicePrincipals().getById(filters.get("id"));
        } else if (filters.containsKey("name")) {
            servicePrincipal = client.accessManagement().servicePrincipals().getByName(filters.get("name"));
        } else {
            throw new GyroException("Either 'id' or 'name' is required");
        }

        if (servicePrincipal != null) {
            return Collections.singletonList(servicePrincipal);
        } else {
            return Collections.emptyList();
        }
    }
}
