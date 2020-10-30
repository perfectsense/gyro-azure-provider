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
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import gyro.core.validation.Required;

/**
 * Creates a private frontend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    private-frontend
 *        name: "private-frontend"
 *        network: $(azure::network load-balancer-network-example)
 *        subnet-name: "subnet2"
 *    end
 */
public class PrivateFrontend extends Diffable implements Copyable<LoadBalancerPrivateFrontend> {
    private String name;
    private String privateIpAddress;
    private String subnetName;
    private NetworkResource network;

    /**
     * The name of the Private Frontend.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Private IP Address associated with the Private Frontend.
     */
    @Updatable
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The Network where the Subnet is found for the Private Frontend.
     */
    @Required
    @Updatable
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The name of the Subnet that is associated with the Private Frontend.
     */
    @Required
    @Updatable
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    @Override
    public void copyFrom(LoadBalancerPrivateFrontend privateFrontend) {
        setName(privateFrontend.name());
        setPrivateIpAddress(privateFrontend.privateIPAddress());
        setSubnetName(privateFrontend.subnetName());
        setNetwork(findById(NetworkResource.class, privateFrontend.networkId()));
    }

    public String primaryKey() {
        return getName();
    }
}
