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

import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

/**
 * Creates a public frontend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         public-frontend
 *             name: "public-frontend-name"
 *             public-ip-address: $(azure::public-ip-address public-ip-address)
 *         end
 */
public class PublicFrontend extends Diffable implements Copyable<LoadBalancerPublicFrontend> {

    private String name;
    private PublicIpAddressResource publicIpAddress;

    /**
     * The name of the Public Frontend.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Public IP Address associated with the Public Frontend.
     */
    @Updatable
    @Required
    public PublicIpAddressResource getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(PublicIpAddressResource publicIpAddressName) {
        this.publicIpAddress = publicIpAddressName;
    }

    @Override
    public void copyFrom(LoadBalancerPublicFrontend publicFrontend) {
        setName(publicFrontend.name());
        setPublicIpAddress(findById(PublicIpAddressResource.class, publicFrontend.getPublicIpAddress().id()));
    }

    public String primaryKey() {
        return getName();
    }
}
