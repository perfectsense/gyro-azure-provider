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

import com.azure.resourcemanager.containerservice.models.ManagedClusterLoadBalancerProfile;
import com.azure.resourcemanager.containerservice.models.ResourceReference;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class ClusterLoadBalancerProfile extends Diffable implements Copyable<ManagedClusterLoadBalancerProfile> {

    private Integer allocatedOutboundPorts;
    private List<String> effectiveOutboundIps;
    private Boolean enableMultipleStandardLoadBalancers;
    private Integer idleTimeoutInMinutes;
    private ClusterLoadBalancerManagedOutboundIps managedOutboundIps;
    private ClusterLoadBalancerOutboundIpPrefixes outboundIpPrefixes;
    private ClusterLoadBalancerOutboundIps outboundIps;

    /**
     * The allocated outbound ports for the load balancer profile.
     */
    public Integer getAllocatedOutboundPorts() {
        return allocatedOutboundPorts;
    }

    public void setAllocatedOutboundPorts(Integer allocatedOutboundPorts) {
        this.allocatedOutboundPorts = allocatedOutboundPorts;
    }

    /**
     * A list of effective outbound ips for the load balancer profile.
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
     * If set to ``true`` enables multiple standard load balancer. Defaults to ``false``.
     */
    public Boolean getEnableMultipleStandardLoadBalancers() {
        if (enableMultipleStandardLoadBalancers == null) {
            enableMultipleStandardLoadBalancers = false;
        }

        return enableMultipleStandardLoadBalancers;
    }

    public void setEnableMultipleStandardLoadBalancers(Boolean enableMultipleStandardLoadBalancers) {
        this.enableMultipleStandardLoadBalancers = enableMultipleStandardLoadBalancers;
    }

    /**
     * The idle timeouts in minutes for the load balancer profile.
     */
    public Integer getIdleTimeoutInMinutes() {
        return idleTimeoutInMinutes;
    }

    public void setIdleTimeoutInMinutes(Integer idleTimeoutInMinutes) {
        this.idleTimeoutInMinutes = idleTimeoutInMinutes;
    }

    /**
     * The managed outbound ip config for the load balancer profile.
     *
     * @subresource gyro.azure.containerservice.ClusterLoadBalancerManagedOutboundIps
     */
    @Updatable
    public ClusterLoadBalancerManagedOutboundIps getManagedOutboundIps() {
        return managedOutboundIps;
    }

    public void setManagedOutboundIPs(ClusterLoadBalancerManagedOutboundIps managedOutboundIps) {
        this.managedOutboundIps = managedOutboundIps;
    }

    /**
     * The load balancer outbound ip prefixes config for the load balancer profile.
     *
     * @subresource gyro.azure.containerservice.ClusterLoadBalancerOutboundIpPrefixes
     */
    @Updatable
    public ClusterLoadBalancerOutboundIpPrefixes getOutboundIpPrefixes() {
        return outboundIpPrefixes;
    }

    public void setOutboundIpPrefixes(ClusterLoadBalancerOutboundIpPrefixes outboundIpPrefixes) {
        this.outboundIpPrefixes = outboundIpPrefixes;
    }

    /**
     * The load balancer outbound ips config for the load balancer profile.
     *
     * @subresource gyro.azure.containerservice.ClusterLoadBalancerOutboundIps
     */
    @Updatable
    public ClusterLoadBalancerOutboundIps getOutboundIps() {
        return outboundIps;
    }

    public void setOutboundIps(ClusterLoadBalancerOutboundIps outboundIps) {
        this.outboundIps = outboundIps;
    }

    @Override
    public void copyFrom(ManagedClusterLoadBalancerProfile model) {
        setAllocatedOutboundPorts(model.allocatedOutboundPorts());
        setEffectiveOutboundIps(model.effectiveOutboundIPs().stream().map(ResourceReference::id).collect(Collectors.toList()));
        setEnableMultipleStandardLoadBalancers(model.enableMultipleStandardLoadBalancers());
        setIdleTimeoutInMinutes(model.idleTimeoutInMinutes());

        ClusterLoadBalancerManagedOutboundIps managedOutboundIps = null;
        if (model.managedOutboundIPs() != null) {
            managedOutboundIps = newSubresource(ClusterLoadBalancerManagedOutboundIps.class);
            managedOutboundIps.copyFrom(model.managedOutboundIPs());
        }
        setManagedOutboundIPs(managedOutboundIps);

        ClusterLoadBalancerOutboundIps outboundIps = null;
        if (model.outboundIPs() != null) {
            outboundIps = newSubresource(ClusterLoadBalancerOutboundIps.class);
            outboundIps.copyFrom(model.outboundIPs());
        }
        setOutboundIps(outboundIps);

        ClusterLoadBalancerOutboundIpPrefixes outboundIpPrefixes = null;
        if (model.outboundIpPrefixes() != null) {
            outboundIpPrefixes = newSubresource(ClusterLoadBalancerOutboundIpPrefixes.class);
            outboundIpPrefixes.copyFrom(model.outboundIpPrefixes());
        }
        setOutboundIpPrefixes(outboundIpPrefixes);
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterLoadBalancerProfile toClusterLoadBalancerProfile() {
        ManagedClusterLoadBalancerProfile profile = new ManagedClusterLoadBalancerProfile();

        if (getAllocatedOutboundPorts() != null) {
            profile.withAllocatedOutboundPorts(getAllocatedOutboundPorts());
        }

        profile.withEnableMultipleStandardLoadBalancers(getEnableMultipleStandardLoadBalancers());

        if (getIdleTimeoutInMinutes() != null) {
            profile.withIdleTimeoutInMinutes(getIdleTimeoutInMinutes());
        }

        if (getManagedOutboundIps() != null) {
            profile.withManagedOutboundIPs(getManagedOutboundIps().toManagedOutboundIps());
        }

        if (getOutboundIpPrefixes() != null) {
            profile.withOutboundIpPrefixes(getOutboundIpPrefixes().toOutboundIpPrefixes());
        }

        if (getOutboundIps() != null) {
            profile.withOutboundIPs(getOutboundIps().toOutboundIPs());
        }

        if (getEffectiveOutboundIps().isEmpty()) {
            profile.withEffectiveOutboundIPs(getEffectiveOutboundIps()
                .stream()
                .map(o -> new ResourceReference().withId(o))
                .collect(Collectors.toList()));
        }

        return profile;
    }
}
