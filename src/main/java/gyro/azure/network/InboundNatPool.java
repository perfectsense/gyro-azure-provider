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

import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.TransportProtocol;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a nat pool.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         inbound-nat-pool
 *             name: "testnatpool"
 *             frontend-name: "test-frontend"
 *             backend-port: 80
 *             protocol: "TCP"
 *             frontend-port-start: 80
 *             frontend-port-end: 89
 *         end
 */
public class InboundNatPool extends Diffable implements Copyable<LoadBalancerInboundNatPool> {

    private Integer backendPort;
    private String frontendName;
    private Integer frontendPortStart;
    private Integer frontendPortEnd;
    private String name;
    private String protocol;

    /**
     * The port number that network traffic is sent to for the Inbound Nat Pool.
     */
    @Required
    @Updatable
    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    /**
     * The name of the frontend that the Inbound Nat Pool is associated with.
     */
    @Required
    @Updatable
    public String getFrontendName() {
        return frontendName;
    }

    public void setFrontendName(String frontendName) {
        this.frontendName = frontendName;
    }

    /**
     * The starting number of the frontend port for the Inbound Nat Pool.
     */
    @Required
    @Updatable
    public Integer getFrontendPortStart() {
        return frontendPortStart;
    }

    public void setFrontendPortStart(Integer frontendPortStart) {
        this.frontendPortStart = frontendPortStart;
    }

    /**
     * The ending number of the frontend port for the Inbound Nat Pool.
     */
    @Required
    @Updatable
    public Integer getFrontendPortEnd() {
        return frontendPortEnd;
    }

    public void setFrontendPortEnd(Integer frontendPortEnd) {
        this.frontendPortEnd = frontendPortEnd;
    }

    /**
     * The name of the Inbound Nat Pool.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The protocol used by the Inbound Nat Pool.
     */
    @Required
    @ValidStrings({ "TCP", "UDP" })
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void copyFrom(LoadBalancerInboundNatPool natPool) {
        setBackendPort(natPool.backendPort());
        setFrontendName(natPool.frontend() != null ? natPool.frontend().name() : null);
        setFrontendPortStart(natPool.frontendPortRangeStart());
        setFrontendPortEnd(natPool.frontendPortRangeEnd());
        setName(natPool.name());
        setProtocol(natPool.protocol() == TransportProtocol.TCP ? "TCP" : "UDP");
    }

    public String primaryKey() {
        return getName();
    }

}
