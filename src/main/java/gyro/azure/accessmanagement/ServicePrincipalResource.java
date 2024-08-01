/*
 * Copyright 2022, Brightspot, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.accessmanagement;

import java.util.Set;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

/**
 * Creates a service principal.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::service-principal service-principal-example
 *         name: "application-service-principal-example"
 *         application: $(azure::application application-example)
 *     end
 *
 */
@Type("service-principal")
public class ServicePrincipalResource extends AzureResource implements Copyable<ServicePrincipal> {

    private String name;
    private ApplicationResource application;

    private String id;

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
     * The application the service principal is going to be created for.
     */
    @Required
    public ApplicationResource getApplication() {
        return application;
    }

    public void setApplication(ApplicationResource application) {
        this.application = application;
    }

    /**
     * The id of the service principal.
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
    public void copyFrom(ServicePrincipal model) {
        setName(model.name());
        setApplication(findById(ApplicationResource.class, model.applicationId()));
        setId(model.id());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        ServicePrincipal servicePrincipal = client.accessManagement().servicePrincipals().getByName(getName());

        if (servicePrincipal == null) {
            return false;
        }

        copyFrom(servicePrincipal);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        ServicePrincipal servicePrincipal = client.accessManagement().servicePrincipals()
            .define(getName())
            .withExistingApplication(getApplication().getApplicationId())
            .create();

        copyFrom(servicePrincipal);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.accessManagement().servicePrincipals().deleteById(getId());
    }
}
