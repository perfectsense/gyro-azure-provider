package gyro.azure.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.network.models.ManagedServiceIdentity;
import com.azure.resourcemanager.network.models.ManagedServiceIdentityUserAssignedIdentities;
import com.azure.resourcemanager.network.models.ResourceIdentityType;
import gyro.azure.Copyable;
import gyro.azure.identity.IdentityResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;

public class ApplicationGatewayManagedServiceIdentity extends Diffable implements Copyable<ManagedServiceIdentity> {

    private List<IdentityResource> userAssignedIdentity;
    private String tenantId;
    private String principalId;
    private String type;

    /**
     * The identity to be associated with the application gateway.
     */
    @Required
    @Updatable
    @CollectionMax(1)
    public List<IdentityResource> getUserAssignedIdentity() {
        if (userAssignedIdentity == null) {
            userAssignedIdentity = new ArrayList<>();
        }

        return userAssignedIdentity;
    }

    public void setUserAssignedIdentity(List<IdentityResource> userAssignedIdentity) {
        this.userAssignedIdentity = userAssignedIdentity;
    }

    /**
     * The tenant id of the service identity.
     */
    @Output
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * The principal id of the service identity.
     */
    @Output
    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    /**
     * The type of the service identity.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ManagedServiceIdentity managedServiceIdentity) {
        setTenantId(managedServiceIdentity.tenantId());
        setPrincipalId(managedServiceIdentity.principalId());
        setUserAssignedIdentity(
            managedServiceIdentity.userAssignedIdentities() != null ? managedServiceIdentity.userAssignedIdentities()
                .keySet()
                .stream()
                .map(o -> findById(IdentityResource.class, o))
                .collect(Collectors.toList()) : null);
        setType(managedServiceIdentity.type().toString());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ManagedServiceIdentity toManagedServiceIdentity() {
        ManagedServiceIdentityUserAssignedIdentities value = new ManagedServiceIdentityUserAssignedIdentities();
        Map<String, ManagedServiceIdentityUserAssignedIdentities> map = new HashMap<>();

        getUserAssignedIdentity().forEach(o -> map.put(o.getId(), value));
        return new ManagedServiceIdentity().withType(ResourceIdentityType.USER_ASSIGNED)
            .withUserAssignedIdentities(map);
    }
}
