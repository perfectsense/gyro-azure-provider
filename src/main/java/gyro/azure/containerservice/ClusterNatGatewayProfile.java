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

package gyro.azure.containerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.resourcemanager.containerservice.models.ManagedClusterNatGatewayProfile;
import com.azure.resourcemanager.containerservice.models.ResourceReference;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class ClusterNatGatewayProfile extends Diffable implements Copyable<ManagedClusterNatGatewayProfile> {

    private List<String> effectiveOutboundIps;
    private Integer idleTimeoutInMinutes;
    private ClusterManagedOutboundIpProfile managedOutboundIpProfile;

    /**
     * A list of effective outbound ips for the nat gateway profile.
     */
    public List<String> getEffectiveOutboundIps() {
        if (effectiveOutboundIps == null) {
            effectiveOutboundIps = new ArrayList<>();
        }

        return effectiveOutboundIps;
    }

    public void setEffectiveOutboundIps(List<String> effectiveOutboundIps) {
        this.effectiveOutboundIps = effectiveOutboundIps;
    }

    /**
     * Idle timeout in minutes in for the nat gateway profile.
     */
    @Required
    public Integer getIdleTimeoutInMinutes() {
        return idleTimeoutInMinutes;
    }

    public void setIdleTimeoutInMinutes(Integer idleTimeoutInMinutes) {
        this.idleTimeoutInMinutes = idleTimeoutInMinutes;
    }

    /**
     * The managed outbound ip profile config for the nat gateway profile.
     *
     * @subresource gyro.azure.containerservice.ClusterManagedOutboundIpProfile
     */
    public ClusterManagedOutboundIpProfile getManagedOutboundIpProfile() {
        return managedOutboundIpProfile;
    }

    public void setManagedOutboundIpProfile(ClusterManagedOutboundIpProfile managedOutboundIpProfile) {
        this.managedOutboundIpProfile = managedOutboundIpProfile;
    }

    @Override
    public void copyFrom(ManagedClusterNatGatewayProfile model) {
        setEffectiveOutboundIps(model.effectiveOutboundIPs() != null
            ? model.effectiveOutboundIPs().stream()
            .map(ResourceReference::id)
            .collect(Collectors.toList())
            : null);

        setIdleTimeoutInMinutes(model.idleTimeoutInMinutes());

        ClusterManagedOutboundIpProfile managedOutboundIpProfile = null;
        if (model.managedOutboundIpProfile() != null) {
            managedOutboundIpProfile = newSubresource(ClusterManagedOutboundIpProfile.class);
            managedOutboundIpProfile.copyFrom(model.managedOutboundIpProfile());
        }
        setManagedOutboundIpProfile(managedOutboundIpProfile);
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterNatGatewayProfile toNatGatewayProfile() {
        ManagedClusterNatGatewayProfile profile = new ManagedClusterNatGatewayProfile();

        if (!getEffectiveOutboundIps().isEmpty()) {
            profile.withEffectiveOutboundIPs(getEffectiveOutboundIps()
                .stream()
                .map(o -> new ResourceReference().withId(o))
                .collect(Collectors.toList()));
        }

        if (getIdleTimeoutInMinutes() != null) {
            profile.withIdleTimeoutInMinutes(getIdleTimeoutInMinutes());
        }

        if (getManagedOutboundIpProfile() != null) {
            profile.withManagedOutboundIpProfile(getManagedOutboundIpProfile().toOutboundIpProfile());
        }

        return profile;
    }
}
