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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancer;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query load balancer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    load-balancer: $(external-query azure::load-balancer {})
 */
@Type("load-balancer")
public class LoadBalancerFinder extends AzureFinder<LoadBalancer, LoadBalancerResource> {
    private String id;

    /**
     * The ID of the Load Balancer.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<LoadBalancer> findAllAzure(Azure client) {
        return client.loadBalancers().list();
    }

    @Override
    protected List<LoadBalancer> findAzure(Azure client, Map<String, String> filters) {
        LoadBalancer loadBalancer = client.loadBalancers().getById(filters.get("id"));
        if (loadBalancer == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(loadBalancer);
        }
    }
}
