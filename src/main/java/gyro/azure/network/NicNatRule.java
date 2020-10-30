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

import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import gyro.core.validation.Required;

/**
 * Creates a nic nat rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    nic-nat-rule
 *        load-balancer: $(azure::load-balancer load-balancer-example)
 *        inbound-nat-rule-name: "test-nat-rule"
 *    end
 */
public class NicNatRule extends Diffable implements Copyable<LoadBalancerInboundNatRule> {
    private LoadBalancerResource loadBalancer;
    private String inboundNatRuleName;

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

    /**
     * The name of the Inbound Nat Rule present on the Load Balancer to associate with the IP configuration.
     */
    @Required
    public String getInboundNatRuleName() {
        return inboundNatRuleName;
    }

    public void setInboundNatRuleName(String inboundNatRuleName) {
        this.inboundNatRuleName = inboundNatRuleName;
    }

    @Override
    public void copyFrom(LoadBalancerInboundNatRule rule) {
        setLoadBalancer(findById(LoadBalancerResource.class, rule.parent().id()));
        setInboundNatRuleName(rule.name());
    }

    public String primaryKey() {
        return String.format("%s %s", getLoadBalancer().getName(), getInboundNatRuleName());
    }
}
