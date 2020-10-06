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

package gyro.azure.network;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;

import com.microsoft.azure.management.network.LoadBalancerBackend;
import gyro.core.validation.Required;

/**
 * Creates a nic backend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    nic-backend
 *        load-balancer: $(azure::load-balancer load-balancer-example)
 *        backend-name: "backend-pool-one"
 *    end
 */
public class NicBackend extends Diffable implements Copyable<LoadBalancerBackend> {

    private String backendName;
    private LoadBalancerResource loadBalancer;

    /**
     * The name of the backend pool present on the Load Balancer to associate with the IP configuration.
     */
    @Required
    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

    /**
     * The Load Balancer to associate the IP Configuration to.
     */
    @Required
    public LoadBalancerResource getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerResource loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void copyFrom(LoadBalancerBackend backend) {
        setBackendName(backend.name());
        setLoadBalancer(findById(LoadBalancerResource.class, backend.parent().id()));
    }

    public String primaryKey() {
        return String.format("%s %s", getLoadBalancer().getName(), getBackendName());
    }
}
