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

import com.azure.resourcemanager.containerservice.models.ManagedClusterManagedOutboundIpProfile;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class ClusterManagedOutboundIpProfile extends Diffable implements Copyable<ManagedClusterManagedOutboundIpProfile> {

    private Integer count;

    /**
     * The desired number of outbound IPs created/managed by Azure.
     */
    @Required
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public void copyFrom(ManagedClusterManagedOutboundIpProfile model) {
        setCount(model.count());
    }

    @Override
    public String primaryKey() {
        return getCount().toString();
    }

    protected ManagedClusterManagedOutboundIpProfile toOutboundIpProfile() {
        return new ManagedClusterManagedOutboundIpProfile().withCount(getCount());
    }
}
