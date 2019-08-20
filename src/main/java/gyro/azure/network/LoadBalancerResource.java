package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerHttpProbe;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithCreate;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 *
 *                 inbound-nat-rule
 *                     name: "test-nat-rule"
 *                     frontend-name: "public-frontend"
 *                     frontend-port: 80
 *                     protocol: "TCP"
 *                 end
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

    public enum SKU_TYPE { STANDARD, BASIC }

    /**
     * The Health Check Http Probes associated with the Load Balancer. (Optional)
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
     * The Health Check Tcp Probes associated with the Load Balancer. (Optional)
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
     * The name of the Load Balancer. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Load Balancer rules associated with the Load Balancer. (Required)
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
     * The Private Frontend associated with the Load Balancer. (Optional)
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
     * The Public Frontend associated with the Load Balancer. (Optional)
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
     * The Resource Group under which the Load Balancer would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Specifies the sku type for the Load Balancer. Valid Values are ``BASIC`` or ``STANDARD``. Defaults to ``BASIC``.
     */
    @Updatable
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
     * The tags associated with the Load Balancer. (Optional)
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

        //public getAllFrontend
        getPublicFrontend().clear();
        for (Map.Entry<String, LoadBalancerPublicFrontend> publicFrontend : loadBalancer.publicFrontends().entrySet()) {
            PublicFrontend frontendPublic = newSubresource(PublicFrontend.class);
            frontendPublic.copyFrom(publicFrontend.getValue());
            getPublicFrontend().add(frontendPublic);
        }

        //private getAllFrontend
        getPrivateFrontend().clear();
        for (Map.Entry<String, LoadBalancerPrivateFrontend> privateFrontend : loadBalancer.privateFrontends().entrySet()) {
            PrivateFrontend frontendPrivate = newSubresource(PrivateFrontend.class);
            frontendPrivate.copyFrom(privateFrontend.getValue());
            getPrivateFrontend().add(frontendPrivate);
        }

        //load balancing rules
        getLoadBalancerRule().clear();
        for (Map.Entry<String, LoadBalancingRule> rule  : loadBalancer.loadBalancingRules().entrySet()) {
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
        Azure client = createClient();

        LoadBalancer loadBalancer = client.loadBalancers().getById(getId());

        if (loadBalancer == null) {
            return false;
        }

        copyFrom(loadBalancer);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        LoadBalancer.DefinitionStages.WithLBRuleOrNat lb = client.loadBalancers()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        WithCreate buildLoadBalancer = null;

        //define the nat pools and rules
        for (Map.Entry<String, Frontend> frontends : getAllFrontends().entrySet()) {
            Frontend front = frontends.getValue();
            if (front.getInboundNatPool() != null) {
                for (InboundNatPool natPool: front.getInboundNatPool()) {

                    buildLoadBalancer = lb.defineInboundNatPool(natPool.getName())
                        .withProtocol(TransportProtocol.fromString(natPool.getProtocol()))
                        .fromFrontend(natPool.getFrontendName())
                        .fromFrontendPortRange(natPool.getFrontendPortStart(), natPool.getFrontendPortEnd())
                        .toBackendPort(natPool.getBackendPort())
                        .attach();
                }
            }

            if (front.getInboundNatRule() != null) {
                for (InboundNatRule natRule : front.getInboundNatRule()) {
                    buildLoadBalancer = lb.defineInboundNatRule(natRule.getName())
                        .withProtocol(TransportProtocol.fromString(natRule.getProtocol()))
                        .fromFrontend(natRule.getFrontendName())
                        .fromFrontendPort(natRule.getFrontendPort())
                        .withFloatingIP(natRule.getFloatingIp())
                        .toBackendPort(natRule.getBackendPort())
                        .attach();
                }
            }
        }

        //define load balancer rules
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

        //define the health check probes
        for (HealthCheckProbeHttp probe : getHealthCheckProbeHttp()) {
            //http
            buildLoadBalancer.defineHttpProbe(probe.getName())
                .withRequestPath(probe.getRequestPath())
                .withPort(probe.getPort())
                .withIntervalInSeconds(probe.getInterval())
                .withNumberOfProbes(probe.getProbes())
                .attach();
        }

        for (HealthCheckProbeTcp probe : getHealthCheckProbeTcp()) {
            //tcp
            buildLoadBalancer.defineTcpProbe(probe.getName())
                .withPort(probe.getPort())
                .withIntervalInSeconds(probe.getInterval())
                .withNumberOfProbes(probe.getProbes())
                .attach();
        }

        //define the public getAllFrontend
        for (PublicFrontend publicFrontend : getPublicFrontend()) {

            PublicIPAddress ip = client.publicIPAddresses().getById(publicFrontend.getPublicIpAddress().getId());

            buildLoadBalancer.definePublicFrontend(publicFrontend.getName())
                .withExistingPublicIPAddress(ip)
                .attach();
        }

        //define the private getAllFrontend
        LoadBalancerPrivateFrontend.DefinitionStages.WithAttach withAttachPrivate;
        for (PrivateFrontend privateFrontend : getPrivateFrontend()) {

            Network network = client.networks().getById(privateFrontend.getNetwork().getId());

            withAttachPrivate = buildLoadBalancer.definePrivateFrontend(privateFrontend.getName())
                .withExistingSubnet(network, privateFrontend.getSubnetName());

            if (privateFrontend.getPrivateIpAddress() != null) {
                withAttachPrivate.withPrivateIPAddressStatic(privateFrontend.getPrivateIpAddress());
            } else {
                withAttachPrivate.withPrivateIPAddressDynamic();
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
        Azure client = createClient();

        LoadBalancer loadBalancer = client.loadBalancers().getById(getId());

        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        LoadBalancer.Update updateLoadBalancer = loadBalancer.update();


        //Update health check probe Http
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

        //Update health check probe Tcp
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

        //Update private frontend
        if (changedFieldNames.contains("private-frontend")) {
            for (PrivateFrontend privateFrontend : currentResource.getPrivateFrontend()) {
                for (InboundNatRule rule : privateFrontend.getInboundNatRule()) {
                    updateLoadBalancer = updateLoadBalancer.withoutInboundNatRule(rule.getName());
                }

                for (InboundNatPool pool : privateFrontend.getInboundNatPool()) {
                    updateLoadBalancer = updateLoadBalancer.withoutInboundNatPool(pool.getName());
                }

                updateLoadBalancer = updateLoadBalancer.withoutFrontend(privateFrontend.getName());
            }

            LoadBalancerPrivateFrontend.UpdateDefinitionStages.WithAttach withAttachPrivate;
            for (PrivateFrontend privateFrontend : getPrivateFrontend()) {

                Network network = client.networks().getById(privateFrontend.getNetwork().getId());

                withAttachPrivate = updateLoadBalancer.definePrivateFrontend(privateFrontend.getName())
                    .withExistingSubnet(network, privateFrontend.getSubnetName());

                if (privateFrontend.getPrivateIpAddress() != null) {
                    withAttachPrivate.withPrivateIPAddressStatic(privateFrontend.getPrivateIpAddress());
                } else {
                    withAttachPrivate.withPrivateIPAddressDynamic();
                }

                withAttachPrivate.attach();

                addNatRulesAndPools(privateFrontend, updateLoadBalancer);
            }
        }

        //Update public frontend
        if (changedFieldNames.contains("public-frontend")) {
            for (PublicFrontend publicFrontend : currentResource.getPublicFrontend()) {
                for (InboundNatRule rule : publicFrontend.getInboundNatRule()) {
                    updateLoadBalancer = updateLoadBalancer.withoutInboundNatRule(rule.getName());
                }

                for (InboundNatPool pool : publicFrontend.getInboundNatPool()) {
                    updateLoadBalancer = updateLoadBalancer.withoutInboundNatPool(pool.getName());
                }

                updateLoadBalancer = updateLoadBalancer.withoutFrontend(publicFrontend.getName());
            }

            for (PublicFrontend publicFrontend : getPublicFrontend()) {
                PublicIPAddress ip = client.publicIPAddresses().getById(publicFrontend.getPublicIpAddress().getId());

                updateLoadBalancer = updateLoadBalancer
                    .definePublicFrontend(publicFrontend.getName())
                    .withExistingPublicIPAddress(ip)
                    .attach();

                addNatRulesAndPools(publicFrontend, updateLoadBalancer);
            }
        }

        //Update Load balancer rules
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

        //tags
        updateLoadBalancer.withTags(getTags());

        LoadBalancer response = updateLoadBalancer.apply();

        copyFrom(response);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.loadBalancers().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }

    private Map<String, Frontend> getAllFrontends() {
        Map<String, Frontend> frontends = new HashMap<>();

        getPrivateFrontend().forEach(frontend -> frontends.put(frontend.getName(), frontend));

        getPublicFrontend().forEach(frontend -> frontends.put(frontend.getName(), frontend));

        return frontends;
    }

    private void addNatRulesAndPools(Frontend frontend, LoadBalancer.Update updateLoadBalancer) {
        addNatPools(new ArrayList<>(frontend.getInboundNatPool()), updateLoadBalancer);
        addNatRules(new ArrayList<>(frontend.getInboundNatRule()), updateLoadBalancer);
    }

    private void addNatPools(List<InboundNatPool> pools, LoadBalancer.Update updateLoadBalancer) {
        LoadBalancerInboundNatPool.UpdateDefinitionStages.WithProtocol withName;
        LoadBalancerInboundNatPool.UpdateDefinitionStages.WithFrontend withProtocol;
        LoadBalancerInboundNatPool.UpdateDefinitionStages.WithFrontendPortRange withFrontend;
        LoadBalancerInboundNatPool.UpdateDefinitionStages.WithBackendPort withPortRange;
        LoadBalancerInboundNatPool.UpdateDefinitionStages.WithAttach withBackendPort;

        for (InboundNatPool pool : pools) {

            withName = updateLoadBalancer.defineInboundNatPool(pool.getName());

            withProtocol =
                (LoadBalancerInboundNatPool.UpdateDefinitionStages.WithFrontend) withName.withProtocol(TransportProtocol.fromString(pool.getProtocol()));

            withFrontend = (LoadBalancerInboundNatPool.UpdateDefinitionStages.WithFrontendPortRange) withProtocol.fromFrontend(pool.getFrontendName());

            withPortRange = (LoadBalancerInboundNatPool.UpdateDefinitionStages.WithBackendPort) withFrontend.fromFrontendPortRange(pool.getFrontendPortStart(), pool.getFrontendPortEnd());

            withBackendPort = (LoadBalancerInboundNatPool.UpdateDefinitionStages.WithAttach) withPortRange.toBackendPort(pool.getBackendPort());

            withBackendPort.attach();
        }
    }

    private void addNatRules(List<InboundNatRule> rules, LoadBalancer.Update updateLoadBalancer) {
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
}
