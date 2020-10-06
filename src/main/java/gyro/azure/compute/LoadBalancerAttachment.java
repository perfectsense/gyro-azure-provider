/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.azure.compute;

import gyro.azure.network.LoadBalancerResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

import java.util.HashSet;
import java.util.Set;

public class LoadBalancerAttachment extends Diffable {
    private LoadBalancerResource loadBalancer;
    private Set<String> backends;
    private Set<String> inboundNatPools;

    /**
     * The Load Balancer to be attached as internal/public-internet type to a Scale Set.
     */
    @Required
    @Updatable
    public LoadBalancerResource getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerResource loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * The corresponding Load Balancer Backends.
     */
    @Required
    @Updatable
    public Set<String> getBackends() {
        if (backends == null) {
            backends = new HashSet<>();
        }

        return backends;
    }

    public void setBackends(Set<String> backends) {
        this.backends = backends;
    }

    /**
     * The corresponding Load Balancer Inbound Nat Pools.
     */
    @Updatable
    public Set<String> getInboundNatPools() {
        if (inboundNatPools == null) {
            inboundNatPools = new HashSet<>();
        }

        return inboundNatPools;
    }

    public void setInboundNatPools(Set<String> inboundNatPools) {
        this.inboundNatPools = inboundNatPools;
    }

    @Override
    public String primaryKey() {
        return "loadbalancer";
    }
}
