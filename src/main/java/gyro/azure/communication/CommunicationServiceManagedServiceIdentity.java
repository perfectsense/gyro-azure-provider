/*
 * Copyright 2024, Brightspot, Inc.
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

package gyro.azure.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.azure.resourcemanager.communication.models.ManagedServiceIdentity;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.communication.models.UserAssignedIdentity;
import gyro.azure.Copyable;
import gyro.azure.identity.IdentityResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;

public class CommunicationServiceManagedServiceIdentity extends Diffable implements Copyable<ManagedServiceIdentity> {

    private List<IdentityResource> userAssignedIdentity;
    private String tenantId;
    private String principalId;
    private String type;

    /**
     * The identity to be associated with the application gateway.
     */
    @Required
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
        UUID tenantUuid = managedServiceIdentity.tenantId();
        setTenantId(tenantUuid == null ? null : tenantUuid.toString());

        UUID principalUuid = managedServiceIdentity.principalId();
        setPrincipalId(principalUuid == null ? null : principalUuid.toString());

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
        UserAssignedIdentity userAssignedIdentity = new UserAssignedIdentity();
        Map<String, UserAssignedIdentity> map = new HashMap<>();

        getUserAssignedIdentity().forEach(o -> map.put(o.getId(), userAssignedIdentity));
        return new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
            .withUserAssignedIdentities(map);
    }
}
