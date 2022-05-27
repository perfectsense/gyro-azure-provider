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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancer.DefinitionStages.WithCreate;
import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import com.azure.resourcemanager.network.models.LoadBalancerPrivateFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.LoadBalancerTcpProbe;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.TransportProtocol;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

import static com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool.UpdateDefinitionStages.*;

/**
 * Creates a load balancer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         azure::load-balancer load-balancer-example
 *             name: "load-balancer-example"
 *             resource-group: $(azure::resource-group resource-group-lb-example)
 *
 *             public-frontend
 *                 name: "public-frontend"
 *                 public-ip-address: $(azure::public-ip-address public-ip-address)
 *             end
 *
 *             load-balancer-rule
 *                 name: "test-rule"
 *                 backend-port: 80
 *                 floating-ip: false
 *                 frontend-name: "public-frontend"
 *                 frontend-port: 443
 *                 idle-timeout-in-minutes: 8
 *                 protocol: "TCP"
 *                 backend-name: "backend-name"
 *                 health-check-probe-name: "healthcheck-tcp"
 *             end
 *
 *             health-check-probe-tcp
 *                 name: "healthcheck-tcp"
 *                 interval: 5
 *                 port: 80
 *                 probes: 2
 *                 protocol: "TCP"
 *             end
 *
 *             tags: {
 *                     Name: "load-balancer-example"
 *             }
 *         end
 */
@Type("load-balancer")
public class LoadBalancerResource extends AzureResource implements Copyable<LoadBalancer> {

    private Set<HealthCheckProbeHttp> healthCheckProbeHttp;
    private Set<HealthCheckProbeTcp> healthCheckProbeTcp;
    private String id;
    private String name;
    private Set<LoadBalancerRule> loadBalancerRule;
    private Set<PrivateFrontend> privateFrontend;
    private Set<PublicFrontend> publicFrontend;
    private ResourceGroupResource resourceGroup;
    private SKU_TYPE skuType;
    private Map<String, String> tags;
    private Set<InboundNatPool> inboundNatPool;
    private Set<InboundNatRule> inboundNatRule;

    /**
     * The Health Check Http Probes associated with the Load Balancer.
     *
     * @subresource gyro.azure.network.HealthCheckProbeHttp
     */
    @Updatable
    public Set<HealthCheckProbeHttp> getHealthCheckProbeHttp() {
        if (healthCheckProbeHttp == null) {
            healthCheckProbeHttp = new HashSet<>();
        }

        return healthCheckProbeHttp;
    }

    public void setHealthCheckProbeHttp(Set<HealthCheckProbeHttp> healthCheckProbeHttp) {
        this.healthCheckProbeHttp = healthCheckProbeHttp;
    }

    /**
     * The Health Check Tcp Probes associated with the Load Balancer.
     *
     * @subresource gyro.azure.network.HealthCheckProbeTcp
     */
    @Updatable
    public Set<HealthCheckProbeTcp> getHealthCheckProbeTcp() {
        if (healthCheckProbeTcp == null) {
            healthCheckProbeTcp = new HashSet<>();
        }

        return healthCheckProbeTcp;
    }

    public void setHealthCheckProbeTcp(Set<HealthCheckProbeTcp> healthCheckProbeTcp) {
        this.healthCheckProbeTcp = healthCheckProbeTcp;
    }

    /**
     * The ID of the Load Balancer.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Load Balancer.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Load Balancer rules associated with the Load Balancer.
     *
     * @subresource gyro.azure.network.LoadBalancerRule
     */
    @Required
    @Updatable
    public Set<LoadBalancerRule> getLoadBalancerRule() {
        if (loadBalancerRule == null) {
            loadBalancerRule = new HashSet<>();
        }

        return loadBalancerRule;
    }

    public void setLoadBalancerRule(Set<LoadBalancerRule> loadBalancerRule) {
        this.loadBalancerRule = loadBalancerRule;
    }

    /**
     * The Private Frontend associated with the Load Balancer.
     *
     * @subresource gyro.azure.network.PrivateFrontend
     */
    @Updatable
    public Set<PrivateFrontend> getPrivateFrontend() {
        if (privateFrontend == null) {
            privateFrontend = new HashSet<>();
        }

        return privateFrontend;
    }

    public void setPrivateFrontend(Set<PrivateFrontend> privateFrontend) {
        this.privateFrontend = privateFrontend;
    }

    /**
     * The Public Frontend associated with the Load Balancer.
     *
     * @subresource gyro.azure.network.PublicFrontend
     */
    @Updatable
    public Set<PublicFrontend> getPublicFrontend() {
        if (publicFrontend == null) {
            publicFrontend = new HashSet<>();
        }

        return publicFrontend;
    }

    public void setPublicFrontend(Set<PublicFrontend> publicFrontend) {
        this.publicFrontend = publicFrontend;
    }

    /**
     * The Resource Group under which the Load Balancer would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Specifies the sku type for the Load Balancer. Defaults to ``BASIC``.
     */
    @ValidStrings({ "BASIC", "STANDARD" })
    public SKU_TYPE getSkuType() {
        if (skuType == null) {
            skuType = SKU_TYPE.BASIC;
        }

        return skuType;
    }

    public void setSkuType(SKU_TYPE skuType) {
        this.skuType = skuType;
    }

    /**
     * The tags associated with the Load Balancer.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The Inbound Nat Pools Associated with the Load Balancer.
     *
     * @subresource gyro.azure.network.InboundNatPool
     */
    @Updatable
    public Set<InboundNatPool> getInboundNatPool() {
        if (inboundNatPool == null) {
            inboundNatPool = new HashSet<>();
        }

        return inboundNatPool;
    }

    public void setInboundNatPool(Set<InboundNatPool> inboundNatPool) {
        this.inboundNatPool = inboundNatPool;
    }

    /**
     * The Inbound Nat Rules associated with the Load Balancer. Nat rules may not be associated with a Load Balancer if a Nat Pool is associated.
     *
     * @subresource gyro.azure.network.InboundNatRule
     */
    @Updatable
    public Set<InboundNatRule> getInboundNatRule() {
        if (inboundNatRule == null) {
            inboundNatRule = new HashSet<>();
        }

        return inboundNatRule;
    }

    public void setInboundNatRule(Set<InboundNatRule> inboundNatRule) {
        this.inboundNatRule = inboundNatRule;
    }

    @Override
    public void copyFrom(LoadBalancer loadBalancer) {
        //http probes
        getHealthCheckProbeHttp().clear();
        for (Map.Entry<String, LoadBalancerHttpProbe> httpProbe : loadBalancer.httpProbes().entrySet()) {
            HealthCheckProbeHttp probeHttp = newSubresource(HealthCheckProbeHttp.class);
            probeHttp.copyFrom(httpProbe.getValue());
            getHealthCheckProbeHttp().add(probeHttp);
        }

        //tcp probes
        getHealthCheckProbeTcp().clear();
        for (Map.Entry<String, LoadBalancerTcpProbe> tcpProbe : loadBalancer.tcpProbes().entrySet()) {
            HealthCheckProbeTcp probeTcp = newSubresource(HealthCheckProbeTcp.class);
            probeTcp.copyFrom(tcpProbe.getValue());
            getHealthCheckProbeTcp().add(probeTcp);
        }

        getInboundNatPool().clear();
        getInboundNatRule().clear();

        //public getAllFrontend
        getPublicFrontend().clear();
        for (Map.Entry<String, LoadBalancerPublicFrontend> publicFrontend : loadBalancer.publicFrontends().entrySet()) {
            PublicFrontend frontendPublic = newSubresource(PublicFrontend.class);
            frontendPublic.copyFrom(publicFrontend.getValue());
            getPublicFrontend().add(frontendPublic);
            getInboundNatPool().addAll(publicFrontend.getValue().inboundNatPools().values().stream().map(o -> {
                InboundNatPool inboundNatPool = newSubresource(InboundNatPool.class);
                inboundNatPool.copyFrom(o);
                return inboundNatPool;
            }).collect(Collectors.toSet()));

            getInboundNatRule().addAll(publicFrontend.getValue().inboundNatRules().values().stream().map(o -> {
                InboundNatRule inboundNatRule = newSubresource(InboundNatRule.class);
                inboundNatRule.copyFrom(o);
                return inboundNatRule;
            }).collect(Collectors.toSet()));
        }

        //private getAllFrontend
        getPrivateFrontend().clear();
        for (Map.Entry<String, LoadBalancerPrivateFrontend> privateFrontend : loadBalancer.privateFrontends()
            .entrySet()) {
            PrivateFrontend frontendPrivate = newSubresource(PrivateFrontend.class);
            frontendPrivate.copyFrom(privateFrontend.getValue());
            getPrivateFrontend().add(frontendPrivate);

            getInboundNatPool().addAll(privateFrontend.getValue().inboundNatPools().values().stream().map(o -> {
                InboundNatPool inboundNatPool = newSubresource(InboundNatPool.class);
                inboundNatPool.copyFrom(o);
                return inboundNatPool;
            }).collect(Collectors.toSet()));

            getInboundNatRule().addAll(privateFrontend.getValue().inboundNatRules().values().stream().map(o -> {
                InboundNatRule inboundNatRule = newSubresource(InboundNatRule.class);
                inboundNatRule.copyFrom(o);
                return inboundNatRule;
            }).collect(Collectors.toSet()));
        }

        //load balancing rules
        getLoadBalancerRule().clear();
        for (Map.Entry<String, LoadBalancingRule> rule : loadBalancer.loadBalancingRules().entrySet()) {
            LoadBalancerRule loadBalancerRule = newSubresource(LoadBalancerRule.class);
            loadBalancerRule.copyFrom(rule.getValue());
            getLoadBalancerRule().add(loadBalancerRule);
        }

        setId(loadBalancer.id());
        setName(loadBalancer.name());
        setResourceGroup(findById(ResourceGroupResource.class, loadBalancer.resourceGroupName()));
        setSkuType(loadBalancer.sku() == LoadBalancerSkuType.BASIC ? SKU_TYPE.BASIC : SKU_TYPE.STANDARD);

        setTags(loadBalancer.tags());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        LoadBalancer loadBalancer = client.loadBalancers().getById(getId());

        if (loadBalancer == null) {
            return false;
        }

        copyFrom(loadBalancer);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        LoadBalancer.DefinitionStages.WithLBRuleOrNat lb = client.loadBalancers()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        WithCreate buildLoadBalancer = null;

        // define the nat pools and rules
        if (!getInboundNatPool().isEmpty()) {
            for (InboundNatPool natPool : getInboundNatPool()) {

                buildLoadBalancer = lb.defineInboundNatPool(natPool.getName())
                    .withProtocol(TransportProtocol.fromString(natPool.getProtocol()))
                    .fromFrontend(natPool.getFrontendName())
                    .fromFrontendPortRange(natPool.getFrontendPortStart(), natPool.getFrontendPortEnd())
                    .toBackendPort(natPool.getBackendPort())
                    .attach();
            }
        }

        if (!getInboundNatRule().isEmpty()) {
            for (InboundNatRule natRule : getInboundNatRule()) {
                buildLoadBalancer = lb.defineInboundNatRule(natRule.getName())
                    .withProtocol(TransportProtocol.fromString(natRule.getProtocol()))
                    .fromFrontend(natRule.getFrontendName())
                    .fromFrontendPort(natRule.getFrontendPort())
                    .withFloatingIP(natRule.getFloatingIp())
                    .toBackendPort(natRule.getBackendPort())
                    .attach();
            }
        }

        // define load balancer rules
        for (LoadBalancerRule rule : getLoadBalancerRule()) {

            buildLoadBalancer = lb.defineLoadBalancingRule(rule.getName())
                .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                .fromFrontend(rule.getFrontendName())
                .fromFrontendPort(rule.getFrontendPort())
                .toBackend(rule.getBackendName())
                .toBackendPort(rule.getBackendPort())
                .withProbe(rule.getHealthCheckProbeName())
                .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                .withFloatingIP(rule.getFloatingIp())
                .attach();
        }

        // define the health check probes
        for (HealthCheckProbeHttp probe : getHealthCheckProbeHttp()) {
            // http
            buildLoadBalancer.defineHttpProbe(probe.getName())
                .withRequestPath(probe.getRequestPath())
                .withPort(probe.getPort())
                .withIntervalInSeconds(probe.getInterval())
                .withNumberOfProbes(probe.getProbes())
                .attach();
        }

        for (HealthCheckProbeTcp probe : getHealthCheckProbeTcp()) {
            // tcp
            buildLoadBalancer.defineTcpProbe(probe.getName())
                .withPort(probe.getPort())
                .withIntervalInSeconds(probe.getInterval())
                .withNumberOfProbes(probe.getProbes())
                .attach();
        }

        // define the public getAllFrontend
        for (PublicFrontend publicFrontend : getPublicFrontend()) {

            PublicIpAddress ip = client.publicIpAddresses().getById(publicFrontend.getPublicIpAddress().getId());

            buildLoadBalancer.definePublicFrontend(publicFrontend.getName())
                .withExistingPublicIpAddress(ip)
                .attach();
        }

        // define the private getAllFrontend
        LoadBalancerPrivateFrontend.DefinitionStages.WithAttach withAttachPrivate;
        for (PrivateFrontend privateFrontend : getPrivateFrontend()) {

            Network network = client.networks().getById(privateFrontend.getNetwork().getId());

            withAttachPrivate = buildLoadBalancer.definePrivateFrontend(privateFrontend.getName())
                .withExistingSubnet(network, privateFrontend.getSubnetName());

            if (privateFrontend.getPrivateIpAddress() != null) {
                withAttachPrivate.withPrivateIpAddressStatic(privateFrontend.getPrivateIpAddress());
            } else {
                withAttachPrivate.withPrivateIpAddressDynamic();
            }
            withAttachPrivate.attach();
        }

        LoadBalancer loadBalancer = buildLoadBalancer
            .withSku(getSkuType() == SKU_TYPE.BASIC ? LoadBalancerSkuType.BASIC : LoadBalancerSkuType.STANDARD)
            .withTags(getTags()).create();

        copyFrom(loadBalancer);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createResourceManagerClient();

        LoadBalancer loadBalancer = client.loadBalancers().getById(getId());

        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        LoadBalancer.Update updateLoadBalancer = loadBalancer.update();

        // Update health check probe Http
        if (changedFieldNames.contains("health-check-probe-http")) {
            for (HealthCheckProbeHttp httpProbe : currentResource.getHealthCheckProbeHttp()) {
                updateLoadBalancer = updateLoadBalancer.withoutProbe(httpProbe.getName());
            }

            for (HealthCheckProbeHttp httpProbe : getHealthCheckProbeHttp()) {
                updateLoadBalancer = updateLoadBalancer
                    .defineHttpProbe(httpProbe.getName())
                    .withRequestPath(httpProbe.getRequestPath())
                    .withIntervalInSeconds(httpProbe.getInterval())
                    .withNumberOfProbes(httpProbe.getProbes())
                    .withPort(httpProbe.getPort())
                    .attach();
            }
        }

        // Update health check probe Tcp
        if (changedFieldNames.contains("health-check-probe-tcp")) {
            for (HealthCheckProbeTcp tcpProbe : currentResource.getHealthCheckProbeTcp()) {
                updateLoadBalancer = updateLoadBalancer.withoutProbe(tcpProbe.getName());
            }

            for (HealthCheckProbeTcp tcpProbe : getHealthCheckProbeTcp()) {
                updateLoadBalancer = updateLoadBalancer
                    .defineTcpProbe(tcpProbe.getName())
                    .withPort(tcpProbe.getPort())
                    .withIntervalInSeconds(tcpProbe.getInterval())
                    .withNumberOfProbes(tcpProbe.getProbes())
                    .attach();
            }
        }

        // Update private frontend
        if (changedFieldNames.contains("private-frontend")) {
            for (PrivateFrontend privateFrontend : currentResource.getPrivateFrontend()) {
                updateLoadBalancer = updateLoadBalancer.withoutFrontend(privateFrontend.getName());
            }

            LoadBalancerPrivateFrontend.UpdateDefinitionStages.WithAttach withAttachPrivate;
            for (PrivateFrontend privateFrontend : getPrivateFrontend()) {

                Network network = client.networks().getById(privateFrontend.getNetwork().getId());

                withAttachPrivate = updateLoadBalancer.definePrivateFrontend(privateFrontend.getName())
                    .withExistingSubnet(network, privateFrontend.getSubnetName());

                if (privateFrontend.getPrivateIpAddress() != null) {
                    withAttachPrivate.withPrivateIpAddressStatic(privateFrontend.getPrivateIpAddress());
                } else {
                    withAttachPrivate.withPrivateIpAddressDynamic();
                }

                withAttachPrivate.attach();
            }
        }

        // Update public frontend
        if (changedFieldNames.contains("public-frontend")) {
            for (PublicFrontend publicFrontend : currentResource.getPublicFrontend()) {
                updateLoadBalancer = updateLoadBalancer.withoutFrontend(publicFrontend.getName());
            }

            for (PublicFrontend publicFrontend : getPublicFrontend()) {
                PublicIpAddress ip = client.publicIpAddresses().getById(publicFrontend.getPublicIpAddress().getId());

                updateLoadBalancer = updateLoadBalancer
                    .definePublicFrontend(publicFrontend.getName())
                    .withExistingPublicIpAddress(ip)
                    .attach();
            }
        }

        // Update Load balancer rules
        if (changedFieldNames.contains("load-balancer-rule")) {
            for (LoadBalancerRule rule : currentResource.getLoadBalancerRule()) {
                updateLoadBalancer = updateLoadBalancer.withoutLoadBalancingRule(rule.getName());
            }

            for (LoadBalancerRule rule : getLoadBalancerRule()) {
                updateLoadBalancer = updateLoadBalancer
                    .defineLoadBalancingRule(rule.getName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackend(rule.getBackendName())
                    .withProbe(rule.getHealthCheckProbeName())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                    .withFloatingIP(rule.getFloatingIp())
                    .attach();
            }
        }

        if (changedFieldNames.contains("inbound-nat-pool")) {
            for (InboundNatPool natPool : currentResource.getInboundNatPool()) {
                updateLoadBalancer = updateLoadBalancer.withoutInboundNatPool(natPool.getName());
            }

            addNatPools(getInboundNatPool(), updateLoadBalancer);
        }

        if (changedFieldNames.contains("inbound-nat-rule")) {
            for (InboundNatRule rule : currentResource.getInboundNatRule()) {
                updateLoadBalancer = updateLoadBalancer.withoutInboundNatRule(rule.getName());
            }

            addNatRules(getInboundNatRule(), updateLoadBalancer);
        }

        // tags
        updateLoadBalancer.withTags(getTags());

        LoadBalancer response = updateLoadBalancer.apply();

        copyFrom(response);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        client.loadBalancers().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }

    private void addNatPools(Set<InboundNatPool> pools, LoadBalancer.Update updateLoadBalancer) {
        WithProtocol withName;
        WithFrontend withProtocol;
        WithFrontendPortRange withFrontend;
        WithBackendPort withPortRange;
        WithAttach withBackendPort;

        for (InboundNatPool pool : pools) {

            withName = updateLoadBalancer.defineInboundNatPool(pool.getName());

            withProtocol =
                (WithFrontend) withName.withProtocol(TransportProtocol.fromString(pool.getProtocol()));

            withFrontend = (WithFrontendPortRange) withProtocol.fromFrontend(pool.getFrontendName());

            withPortRange = (WithBackendPort) withFrontend.fromFrontendPortRange(
                pool.getFrontendPortStart(),
                pool.getFrontendPortEnd());

            withBackendPort = (WithAttach) withPortRange.toBackendPort(pool.getBackendPort());

            withBackendPort.attach();
        }
    }

    private void addNatRules(Set<InboundNatRule> rules, LoadBalancer.Update updateLoadBalancer) {
        for (InboundNatRule rule : rules) {
            updateLoadBalancer
                .defineInboundNatRule(rule.getName())
                .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                .fromFrontend(rule.getFrontendName())
                .fromFrontendPort(rule.getFrontendPort())
                .toBackendPort(rule.getBackendPort())
                .withFloatingIP(rule.getFloatingIp())
                .attach();
        }
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (!getInboundNatRule().isEmpty() && !getInboundNatPool().isEmpty()) {
            errors.add(new ValidationError(
                this,
                "inbound-nat-rule",
                "'inbound-nat-rule' cannot be set when 'inbound-nat-pool' is set."));
        }

        return errors;
    }

    public enum SKU_TYPE {
        STANDARD,
        BASIC
    }
}
