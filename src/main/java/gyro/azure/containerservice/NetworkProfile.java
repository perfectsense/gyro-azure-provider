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

import com.azure.resourcemanager.containerservice.models.ContainerServiceNetworkProfile;
import com.azure.resourcemanager.containerservice.models.LoadBalancerSku;
import com.azure.resourcemanager.containerservice.models.NetworkMode;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.containerservice.models.NetworkPolicy;
import com.azure.resourcemanager.containerservice.models.OutboundType;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import org.apache.commons.lang3.StringUtils;

public class NetworkProfile extends Diffable implements Copyable<ContainerServiceNetworkProfile> {

    private String dnsServiceIp;
    private String networkPlugin;
    private String networkMode;
    private String networkPolicy;
    private ClusterLoadBalancerProfile loadBalancerProfile;
    private String loadBalancerSku;
    private ClusterNatGatewayProfile natGatewayProfile;
    private String outboundType;
    private String podCidr;
    private String serviceCidr;

    /**
     * The dns service ip for the network profile.
     */
    @Updatable
    public String getDnsServiceIp() {
        return dnsServiceIp;
    }

    public void setDnsServiceIp(String dnsServiceIp) {
        this.dnsServiceIp = dnsServiceIp;
    }

    /**
     * The network plugin for the network profile.
     */
    @Required
    @ValidStrings({"azure", "kubenet"})
    public String getNetworkPlugin() {
        return networkPlugin;
    }

    public void setNetworkPlugin(String networkPlugin) {
        this.networkPlugin = networkPlugin;
    }

    /**
     * The network mode for the network profile.
     */
    @Updatable
    @ValidStrings({"transparent", "bridge"})
    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    /**
     * The network policy for the network profile.
     */
    @Updatable
    @ValidStrings({"calico", "azure"})
    public String getNetworkPolicy() {
        return networkPolicy;
    }

    public void setNetworkPolicy(String networkPolicy) {
        this.networkPolicy = networkPolicy;
    }

    /**
     * The loadbalancer config for the network profile.
     *
     * @subresource gyro.azure.containerservice.ClusterLoadBalancerProfile
     */
    @Updatable
    public ClusterLoadBalancerProfile getLoadBalancerProfile() {
        return loadBalancerProfile;
    }

    public void setLoadBalancerProfile(ClusterLoadBalancerProfile loadBalancerProfile) {
        this.loadBalancerProfile = loadBalancerProfile;
    }

    /**
     * The load balancer sku for the network profile.
     */
    @Updatable
    @ValidStrings({"Standard", "Basic"})
    public String getLoadBalancerSku() {
        return loadBalancerSku;
    }

    public void setLoadBalancerSku(String loadBalancerSku) {
        this.loadBalancerSku = loadBalancerSku;
    }

    /**
     * The natgateway config for the network profile.
     *
     * @subresource gyro.azure.containerservice.ClusterNatGatewayProfile
     */
    @Updatable
    public ClusterNatGatewayProfile getNatGatewayProfile() {
        return natGatewayProfile;
    }

    public void setNatGatewayProfile(ClusterNatGatewayProfile natGatewayProfile) {
        this.natGatewayProfile = natGatewayProfile;
    }

    /**
     * The outbound type for the network profile.
     */
    @Updatable
    @ValidStrings({"loadBalancer", "userDefinedRouting", "managedNATGateway", "userAssignedNATGateway"})
    public String getOutboundType() {
        return outboundType;
    }

    public void setOutboundType(String outboundType) {
        this.outboundType = outboundType;
    }

    /**
     * The pod cidr for the network profile.
     */
    @Updatable
    public String getPodCidr() {
        return podCidr;
    }

    public void setPodCidr(String podCidr) {
        this.podCidr = podCidr;
    }

    /**
     * The service cidr for the network profile.
     */
    @Updatable
    public String getServiceCidr() {
        return serviceCidr;
    }

    public void setServiceCidr(String serviceCidr) {
        this.serviceCidr = serviceCidr;
    }

    @Override
    public void copyFrom(ContainerServiceNetworkProfile model) {
        setDnsServiceIp(model.dnsServiceIp());
        setNetworkPlugin(model.networkPlugin().toString());
        setNetworkMode(model.networkMode() != null ? model.networkMode().toString() : null);
        setNetworkPolicy(model.networkPolicy() != null ? model.networkPolicy().toString() : null);
        setLoadBalancerSku(model.loadBalancerSku() != null ? model.loadBalancerSku().toString() : null);
        setOutboundType(model.outboundType() != null ? model.outboundType().toString() : null);
        setPodCidr(model.podCidr());
        setServiceCidr(model.serviceCidr());

        ClusterNatGatewayProfile clusterNatGatewayProfile = null;
        if (model.natGatewayProfile() != null) {
            clusterNatGatewayProfile = newSubresource(ClusterNatGatewayProfile.class);
            clusterNatGatewayProfile.copyFrom(model.natGatewayProfile());
        }
        setNatGatewayProfile(clusterNatGatewayProfile);

        ClusterLoadBalancerProfile clusterLoadBalancerProfile = null;
        if (model.loadBalancerProfile() != null) {
            clusterLoadBalancerProfile = newSubresource(ClusterLoadBalancerProfile.class);
            clusterLoadBalancerProfile.copyFrom(model.loadBalancerProfile());
        }
        setLoadBalancerProfile(clusterLoadBalancerProfile);
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ContainerServiceNetworkProfile toNetworkProfile() {
        ContainerServiceNetworkProfile networkProfile = new ContainerServiceNetworkProfile();

        if (!StringUtils.isBlank(getNetworkPlugin())) {
            networkProfile.withNetworkPlugin(NetworkPlugin.fromString(getNetworkPlugin()));
        }

        if (!StringUtils.isBlank(getNetworkPolicy())) {
            networkProfile.withNetworkPolicy(NetworkPolicy.fromString(getNetworkPolicy()));
        }

        if (!StringUtils.isBlank(getNetworkMode())) {
            networkProfile.withNetworkMode(NetworkMode.fromString(getNetworkMode()));
        }

        if (!StringUtils.isBlank(getDnsServiceIp())) {
            networkProfile.withDnsServiceIp(getDnsServiceIp());
        }

        if (!StringUtils.isBlank(getPodCidr())) {
            networkProfile.withPodCidr(getPodCidr());
        }

        if (!StringUtils.isBlank(getOutboundType())) {
            networkProfile.withOutboundType(OutboundType.fromString(getOutboundType()));
        }

        if (!StringUtils.isBlank(getServiceCidr())) {
            networkProfile.withServiceCidr(getServiceCidr());
        }

        if (!StringUtils.isBlank(getLoadBalancerSku())) {
            networkProfile.withLoadBalancerSku(LoadBalancerSku.fromString(getLoadBalancerSku()));
        }

        if (getLoadBalancerProfile() != null) {
            networkProfile.withLoadBalancerProfile(getLoadBalancerProfile().toClusterLoadBalancerProfile());
        }

        if (getNatGatewayProfile() != null) {
            networkProfile.withNatGatewayProfile(getNatGatewayProfile().toNatGatewayProfile());
        }

        return networkProfile;
    }
}
