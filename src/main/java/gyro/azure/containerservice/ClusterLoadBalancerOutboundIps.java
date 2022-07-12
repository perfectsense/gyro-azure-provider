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

import com.azure.resourcemanager.containerservice.models.ManagedClusterLoadBalancerProfileOutboundIPs;
import com.azure.resourcemanager.containerservice.models.ResourceReference;
import gyro.azure.Copyable;
import gyro.azure.network.PublicIpAddressResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class ClusterLoadBalancerOutboundIps extends Diffable implements Copyable<ManagedClusterLoadBalancerProfileOutboundIPs> {

    private List<PublicIpAddressResource> publicIps;

    /**
     * The list of public ips.
     */
    @Required
    @Updatable
    public List<PublicIpAddressResource> getPublicIps() {
        if (publicIps == null) {
            publicIps = new ArrayList<>();
        }

        return publicIps;
    }

    public void setPublicIps(List<PublicIpAddressResource> publicIps) {
        this.publicIps = publicIps;
    }

    @Override
    public void copyFrom(ManagedClusterLoadBalancerProfileOutboundIPs model) {
        setPublicIps(model.publicIPs().stream()
            .map(ResourceReference::id)
            .map(id -> findById(PublicIpAddressResource.class, id))
            .collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterLoadBalancerProfileOutboundIPs toOutboundIPs() {
        return new ManagedClusterLoadBalancerProfileOutboundIPs()
            .withPublicIPs(getPublicIps()
                .stream()
                .map(o -> new ResourceReference().withId(o.getId()))
                .collect(Collectors.toList()));
    }
}
