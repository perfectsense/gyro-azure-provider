package gyro.azure.accessmanagement;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.Set;

/**
 * Creates a service principal.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::service-principal service-principal-example
 *         name: "service-principal-example"
 *         application-id: "ae4d20c4-31ce-42b9-8418-2dcdc8ba1f68"
 *     end
 */
@Type("service-principal")
public class ServicePrincipalResource extends AzureResource implements Copyable<ServicePrincipal> {
    private String applicationId;
    private String name;
    private String id;

    /**
     * Existing application ID. (Required)
     */
    @Required
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * The name of the service principal.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the service principal.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(ServicePrincipal servicePrincipal) {
        setApplicationId(servicePrincipal.applicationId());
        setId(servicePrincipal.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ServicePrincipal servicePrincipal = client.accessManagement().servicePrincipals().getById(getId());

        if (servicePrincipal == null) {
            return false;
        }

        copyFrom(servicePrincipal);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        ServicePrincipal servicePrincipal = client.accessManagement().servicePrincipals()
            .define(getName())
            .withExistingApplication(getApplicationId())
            .create();

        copyFrom(servicePrincipal);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        client.accessManagement().servicePrincipals().deleteById(getId());
    }
}
