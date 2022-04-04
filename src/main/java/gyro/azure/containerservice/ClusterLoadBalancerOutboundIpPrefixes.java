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

import com.azure.resourcemanager.containerservice.models.ManagedClusterLoadBalancerProfileOutboundIpPrefixes;
import com.azure.resourcemanager.containerservice.models.ResourceReference;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class ClusterLoadBalancerOutboundIpPrefixes extends Diffable implements Copyable<ManagedClusterLoadBalancerProfileOutboundIpPrefixes> {

    private List<String> publicIpPrefixes;

    /**
     * The count of public ip prefixes.
     */
    @Required
    @Updatable
    public List<String> getPublicIpPrefixes() {
        if (publicIpPrefixes == null) {
            publicIpPrefixes = new ArrayList<>();
        }

        return publicIpPrefixes;
    }

    public void setPublicIpPrefixes(List<String> publicIpPrefixes) {
        this.publicIpPrefixes = publicIpPrefixes;
    }

    @Override
    public void copyFrom(ManagedClusterLoadBalancerProfileOutboundIpPrefixes model) {
        setPublicIpPrefixes(model.publicIpPrefixes().stream().map(ResourceReference::id).collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterLoadBalancerProfileOutboundIpPrefixes toOutboundIpPrefixes() {
        return new ManagedClusterLoadBalancerProfileOutboundIpPrefixes()
            .withPublicIpPrefixes(getPublicIpPrefixes()
                .stream()
                .map(o -> new ResourceReference().withId(o))
                .collect(Collectors.toList()));
    }
}
