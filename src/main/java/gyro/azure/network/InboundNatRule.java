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

import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.TransportProtocol;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a nat rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         inbound-nat-rule
 *             name: "test-nat-rule"
 *             frontend-name: "test-frontend"
 *             frontend-port: 80
 *             protocol: "TCP"
 *         end
 */
public class InboundNatRule extends Diffable implements Copyable<LoadBalancerInboundNatRule> {

    private Integer backendPort;
    private Boolean floatingIp;
    private String frontendName;
    private Integer frontendPort;
    private String name;
    private String protocol;

    /**
     * The backend port that receives network traffic for the Inbound Nat Rule.
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
     * Determines whether floating ip support is enabled for the Inbound Nat Rule. Defaults to ``false``.
     */
    @Updatable
    public Boolean getFloatingIp() {
        if (floatingIp == null) {
            floatingIp = false;
        }

        return floatingIp;
    }

    public void setFloatingIp(Boolean floatingIp) {
        this.floatingIp = floatingIp;
    }

    /**
     * The frontend associated with the Inbound Nat Rule.
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
     * The name of the Inbound Nat Rule.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The frontend port that receives network traffic for the Inbound Nat Rule.
     */
    @Required
    @Updatable
    public Integer getFrontendPort() {
        return frontendPort;
    }

    public void setFrontendPort(Integer frontendPort) {
        this.frontendPort = frontendPort;
    }

    /**
     * The protocol used by the Inbound Nat Rule.
     */
    @Required
    @ValidStrings({"TCP", "UDP"})
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void copyFrom(LoadBalancerInboundNatRule natRule) {
        setBackendPort(natRule.backendPort());
        setFloatingIp(natRule.floatingIPEnabled());
        setFrontendName(natRule.frontend() != null ? natRule.frontend().name() : null);
        setFrontendPort(natRule.frontendPort());
        setName(natRule.name());
        setProtocol(natRule.protocol() == TransportProtocol.TCP ? "TCP" : "UDP");
    }

    public String primaryKey() {
        return getName();
    }

}