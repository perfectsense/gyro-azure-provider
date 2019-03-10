package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerHttpProbe;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithLBRuleOrNatOrCreate;
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithCreateAndInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithCreateAndInboundNatRule;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.util.ArrayList;
import java.util.HashMap;
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
 *             load-balancer-name: "load-balancer-example"
 *             resource-group-name: $(azure::resource-group resource-group-lb-example | resource-group-name)
 *             sku-basic: true
 *
 *
 *             public-frontend
 *                 public-frontend-name: "public-frontend"
 *                 public-ip-address-name: $(azure::public-ip-address public-ip-address | public-ip-address-name)
 *
 *                 inbound-nat-rule
 *                     inbound-nat-rule-name: "test-nat-rule"
 *                     frontend-name: "public-frontend"
 *                     frontend-port: 80
 *                     protocol: "TCP"
 *                 end
 *             end
 *
 *             backend-pool
 *                 backend-pool-name: "backend-pool-name"
 *                 virtual-machine-ids: [$(azure::virtual-machine virtual-machine-example-lb | virtual-machine-id)]
 *             end
 *
 *             load-balancer-rule
 *                 load-balancer-rule-name: "test-rule"
 *                 backend-port: 80
 *                 floating-ip: false
 *                 frontend-name: "public-frontend"
 *                 frontend-port: 443
 *                 idle-timeout-in-minutes: 8
 *                 protocol: "TCP"
 *                 backend-pool-name: "backend-pool-name"
 *                 health-check-probe-name: "healthcheck-tcp"
 *             end
 *
 *             health-check-probe-tcp
 *                 health-probe-name: "healthcheck-tcp"
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
@ResourceName("load-balancer")
public class LoadBalancerResource extends AzureResource {

    private List<BackendPool> backendPool;
    private Map<String, Frontend> frontends;
    private List<HealthCheckProbeHttp> healthCheckProbeHttp;
    private List<HealthCheckProbeTcp> healthCheckProbeTcp;
    private List<InboundNatPool> inboundNatPool;
    private List<InboundNatRule> inboundNatRule;
    private String loadBalancerId;
    private String loadBalancerName;
    private List<LoadBalancerRule> loadBalancerRule;
    private List<PrivateFrontend> privateFrontend;
    private List<PublicFrontend> publicFrontend;
    private String resourceGroupName;
    private Boolean skuBasic;
    private Map<String, String> tags;

    /**
     * The backend pools associated with the load balancer. (Required)
     */
    public List<BackendPool> getBackendPool() {
        return backendPool;
    }

    public void setBackendPool(List<BackendPool> backendPool) {
        this.backendPool = backendPool;
    }

    @ResourceDiffProperty(updatable = true)
    public Map<String, Frontend> getFrontends() {
        if (frontends == null) {
            frontends = new HashMap<>();
        }

        getPrivateFrontend()
                .stream()
                .forEach(frontend -> frontends.put(frontend.getPrivateFrontendName(), frontend));

        getPublicFrontend().stream()
                .forEach(frontend -> frontends.put(frontend.getPublicFrontendName(), frontend));

        return frontends;
    }

    public void setFrontends(Map<String, Frontend> frontends) {
        this.frontends = frontends;
    }

    /**
     * The http probes associated with the load balancer. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<HealthCheckProbeHttp> getHealthCheckProbeHttp() {
        return healthCheckProbeHttp;
    }

    public void setHealthCheckProbeHttp(List<HealthCheckProbeHttp> healthCheckProbeHttp) {
        this.healthCheckProbeHttp = healthCheckProbeHttp;
    }

    /**
     * The tcp probes associated with the load balancer. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<HealthCheckProbeTcp> getHealthCheckProbeTcp() {
        return healthCheckProbeTcp;
    }

    public void setHealthCheckProbeTcp(List<HealthCheckProbeTcp> healthCheckProbeTcp) {
        this.healthCheckProbeTcp = healthCheckProbeTcp;
    }

    /**
     * The inbound nat pools associated with the load balancer. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<InboundNatPool> getInboundNatPool() {
        if (inboundNatPool == null) {
            inboundNatPool = new ArrayList<>();
        }

        return inboundNatPool;
    }

    public void setInboundNatPool(List<InboundNatPool> inboundNatPool) {
        this.inboundNatPool = inboundNatPool;
    }

    /**
     * The inbound nat rules associated with the load balancer. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<InboundNatRule> getInboundNatRule() {
        if (inboundNatRule == null) {
            inboundNatRule = new ArrayList<>();
        }

        return inboundNatRule;
    }

    public void setInboundNatRule(List<InboundNatRule> inboundNatRule) {
        this.inboundNatRule = inboundNatRule;
    }

    @ResourceOutput
    public String getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(String loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    /**
     * The name of the load balancer. (Required)
     */
    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    /**
     * The load balancer rules associated with the load balancer. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<LoadBalancerRule> getLoadBalancerRule() {
        return loadBalancerRule;
    }

    public void setLoadBalancerRule(List<LoadBalancerRule> loadBalancerRule) {
        this.loadBalancerRule = loadBalancerRule;
    }

    /**
     * The private frontends associated with the load balancer. (Optional)
     */
    public List<PrivateFrontend> getPrivateFrontend() {
        if (privateFrontend == null) {
            privateFrontend = new ArrayList<>();
        }

        return privateFrontend;
    }

    public void setPrivateFrontend(List<PrivateFrontend> privateFrontend) {
        this.privateFrontend = privateFrontend;
    }

    /**
     * The public frontends associated with the load balancer. (Optional)
     */
    public List<PublicFrontend> getPublicFrontend() {
        if (publicFrontend == null) {
            publicFrontend = new ArrayList<>();
        }

        return publicFrontend;
    }

    public void setPublicFrontend(List<PublicFrontend> publicFrontend) {
        this.publicFrontend = publicFrontend;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * Specifies if the sku type is basic or standard. Defaults to true. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getSkuBasic() {
        if (skuBasic == null) {
            skuBasic = true;
        }

        return skuBasic;
    }

    public void setSkuBasic(Boolean skuBasic) {
        this.skuBasic = skuBasic;
    }

    /**
     * The tags associated with the load baalancer. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
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
    public boolean refresh() {
        Azure client = createClient();

        //backend pool
        getBackendPool().clear();
        LoadBalancer loadBalancer = client.loadBalancers().getById(getLoadBalancerId());
        for (Map.Entry<String, LoadBalancerBackend> backend : loadBalancer.backends().entrySet()) {
            getBackendPool().add(new BackendPool(backend.getValue()));
        }

        //nat pools
        getInboundNatPool();
        for (Map.Entry<String, LoadBalancerInboundNatPool> natPool : loadBalancer.inboundNatPools().entrySet()) {
            getInboundNatPool().add(new InboundNatPool(natPool.getValue()));
        }

        //nat rules
        getInboundNatRule().clear();
        for (Map.Entry<String, LoadBalancerInboundNatRule> natRule : loadBalancer.inboundNatRules().entrySet()) {
            getInboundNatRule().add(new InboundNatRule(natRule.getValue()));
        }

        //health check probes
        //http
        getHealthCheckProbeHttp().clear();
        for (Map.Entry<String, LoadBalancerHttpProbe> httpProbe : loadBalancer.httpProbes().entrySet()) {
            getHealthCheckProbeHttp()
                    .add(new HealthCheckProbeHttp(httpProbe.getValue()));
        }

        //tcp
        getHealthCheckProbeTcp().clear();
        for (Map.Entry<String, LoadBalancerTcpProbe> tcpProbe : loadBalancer.tcpProbes().entrySet()) {
            getHealthCheckProbeTcp()
                    .add(new HealthCheckProbeTcp(tcpProbe.getValue()));
        }

        getPrivateFrontend().clear();
        for (Map.Entry<String, LoadBalancerPublicFrontend> publicFrontend : loadBalancer.publicFrontends().entrySet()) {
            getPublicFrontend().add(new PublicFrontend(publicFrontend.getValue()));
        }

        getPrivateFrontend();
        for (Map.Entry<String, LoadBalancerPrivateFrontend> privateFrontend : loadBalancer.privateFrontends().entrySet()) {
            getPrivateFrontend().add(new PrivateFrontend(privateFrontend.getValue()));
        }

        //load balancing rules
        getLoadBalancerRule().clear();
        for (Map.Entry<String, LoadBalancingRule> rule  : loadBalancer.loadBalancingRules().entrySet()) {
            getLoadBalancerRule().add(new LoadBalancerRule(rule.getValue()));
        }

        setLoadBalancerId(loadBalancer.id());
        setSkuBasic(loadBalancer.sku() == LoadBalancerSkuType.BASIC ? true : false);
        setTags(loadBalancer.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        //get necessary load balancer
        LoadBalancer.DefinitionStages.WithLBRuleOrNat lb = client.loadBalancers()
                .define(getLoadBalancerName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());

        LoadBalancer.DefinitionStages.WithLBRuleOrNatOrCreate chain = null;

        // loop over load balancer rules
        // include frontend
        // backend
        // and probe
        for (LoadBalancerRule rule : getLoadBalancerRule()) {
            chain = lb.defineLoadBalancingRule(rule.getLoadBalancerRuleName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackend(rule.getBackendPool().getBackendPoolName())
                    .toBackendPort(rule.getBackendPort())
                    .withProbe(rule.getHealthCheckProbe().getHealthProbeName())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
            //use frontend configuration to set inbound nat pools
            for (InboundNatPool natPool : ipConfiguration.getInboundNatPool()) {
                chain.defineInboundNatPool(natPool.getInboundNatPoolName())
                        .withProtocol(TransportProtocol.fromString(natPool.getProtocol()))
                        .fromExistingSubnet("load-balancer-network-example", "subnet1")
                        .fromFrontendPortRange(natPool.getFrontendPortRangeStart(), natPool.getFrontendPortRangeEnd())
                        .toBackendPort(natPool.getBackendPort())
                        .attach();
            }

            //backend pool
            LoadBalancerBackend.DefinitionStages.Blank<LoadBalancer.DefinitionStages.WithCreate> backendCreate = chain.defineBackend(rule.getBackendPool().getBackendPoolName());
            if (!rule.getBackendPool().getVirtualMachineIds().isEmpty()) {
                backendCreate.withExistingVirtualMachines(toBackend(rule.getBackendPool().getVirtualMachineIds()));
            }
            backendCreate.attach();

            //health check probe
            HealthCheckProbe probe = rule.getHealthCheckProbe();
            if (probe.getProtocol().equals("TCP")) {
                chain.defineTcpProbe(probe.getHealthProbeName())
                        .withPort(probe.getPort())
                        .withIntervalInSeconds(probe.getInterval())
                        .withNumberOfProbes(probe.getProbes())
                        .attach();
            } else {
                chain.defineHttpProbe(probe.getHealthProbeName())
                        .withRequestPath(probe.getPath())
                        .withPort(probe.getPort())
                        .withIntervalInSeconds(probe.getInterval())
                        .withNumberOfProbes(probe.getProbes())
                        .attach();
            }
        }

            LoadBalancerPublicFrontend.DefinitionStages.WithAttach withAttach = null;


            } else {
                withAttachPrivate.withPrivateIPAddressDynamic();
            }
            withAttachPrivate.attach();
        }

        //add tags
        chain.withTags(getTags()).create();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        LoadBalancer loadBalancer = client.loadBalancers().getById(getLoadBalancerId());

        //updates
        for (BackendPool pool : getBackendPool()) {
            loadBalancer.update()
                    .updateBackend(pool.getBackendPoolName());
        }

        for (HealthCheckProbeHttp httpProbe : getHealthCheckProbeHttp()) {
            loadBalancer.update()
                    .updateHttpProbe(httpProbe.getHealthCheckProbeName())
                    .withIntervalInSeconds(httpProbe.getInterval())
                    .withNumberOfProbes(httpProbe.getProbes())
                    .withRequestPath(httpProbe.getRequestPath())
                    .withPort(httpProbe.getPort());
        }

        for (HealthCheckProbeTcp tcpProbe : getHealthCheckProbeTcp()) {
            loadBalancer.update()
                    .updateTcpProbe(tcpProbe.getHealthCheckProbeName())
                    .withIntervalInSeconds(tcpProbe.getInterval())
                    .withNumberOfProbes(tcpProbe.getProbes())
                    .withPort(tcpProbe.getPort());
        }

        for (InboundNatPool pool : getInboundNatPool()) {
            loadBalancer.update()
                    .updateInboundNatPool(pool.getInboundNatPoolName())
                    .withProtocol(TransportProtocol.fromString(pool.getProtocol()))
                    .fromFrontend(pool.getFrontendName())
                    .fromFrontendPortRange(pool.getFrontendPortRangeStart(), pool.getFrontendPortRangeEnd())
                    .toBackendPort(pool.getBackendPort());
        }

        for (InboundNatRule rule : getInboundNatRule()) {
            loadBalancer.update()
                    .updateInboundNatRule(rule.getInboundNatRuleName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .withFloatingIP(rule.getFloatingIp())
                    .toBackendPort(rule.getBackendPort());
        }

        for (LoadBalancerRule rule : getLoadBalancerRule()) {
            loadBalancer.update()
                    .updateLoadBalancingRule(rule.getLoadBalancerRuleName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackendPort(rule.getBackendPort())
                    .withFloatingIP(rule.getFloatingIp())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes());
        }

        for (PrivateFrontend privateFrontend : getPrivateFrontend()) {
            LoadBalancerPrivateFrontend.Update withAttachPrivate;
            Network network = client.networks().getById(privateFrontend.getNetworkId());

            withAttachPrivate = loadBalancer.update()
                    .updatePrivateFrontend(privateFrontend.getPrivateFrontendName())
                    .withExistingSubnet(network, privateFrontend.getSubnetName());

            if (privateFrontend.getPrivateIpAddress() != null) {
                withAttachPrivate.withExistingSubnet(network, privateFrontend.getSubnetName());
            } else {
                withAttachPrivate.withPrivateIPAddressDynamic();
            }
        }

        for (PublicFrontend publicFrontend : getPublicFrontend()) {
            PublicIPAddress ip = client.publicIPAddresses()
                    .getByResourceGroup(getResourceGroupName(), publicFrontend.getPublicIpAddressName());
            loadBalancer.update()
                    .updatePublicFrontend(publicFrontend.getPublicFrontendName())
                    .withExistingPublicIPAddress(ip);
        }

        //tags
        loadBalancer
                .updateTags()
                .withTags(getTags());


        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        //backend pools
        List<BackendPool> backendAdditions = new ArrayList<>(getBackendPool());
        backendAdditions.removeAll(currentResource.getBackendPool());

        List<BackendPool> backendSubtractions = new ArrayList<>(currentResource.getBackendPool());
        backendSubtractions.removeAll(getBackendPool());

        for (BackendPool pool : backendAdditions) {
            loadBalancer.update()
                    .defineBackend(pool.getBackendPoolName())
                    .withExistingVirtualMachines(toBackend(pool.getVirtualMachineIds()))
                    .attach();
        }

        for (BackendPool pool : backendSubtractions) {
            loadBalancer.update()
                    .withoutBackend(pool.getBackendPoolName())
                    .apply();
        }

        //health check probes
        List<HealthCheckProbeHttp> httpProbeAdditions = new ArrayList<>(getHealthCheckProbeHttp());
        httpProbeAdditions.removeAll(currentResource.getHealthCheckProbeHttp());

        List<HealthCheckProbeHttp> httpProbeSubtractions = new ArrayList<>(currentResource.getHealthCheckProbeHttp());
        httpProbeSubtractions.removeAll(getHealthCheckProbeHttp());

        for (HealthCheckProbeHttp httpProbe : httpProbeAdditions) {
            loadBalancer.update()
                    .defineHttpProbe(httpProbe.getHealthCheckProbeName())
                    .withRequestPath(httpProbe.getRequestPath())
                    .withIntervalInSeconds(httpProbe.getInterval())
                    .withNumberOfProbes(httpProbe.getProbes())
                    .withPort(httpProbe.getPort())
                    .attach();
        }

        for (HealthCheckProbeHttp httpProbe : httpProbeSubtractions) {
            loadBalancer.update()
                    .withoutProbe(httpProbe.getHealthCheckProbeName())
                    .apply();
        }

        //update nat rule
        List<InboundNatRule> ruleAdditions = new ArrayList<>(getInboundNatRule());
        ruleAdditions.removeAll(currentResource.getInboundNatRule());

        List<InboundNatRule> ruleSubtractions = new ArrayList<>(currentResource.getInboundNatRule());
        ruleSubtractions.removeAll(getInboundNatRule());

        for (InboundNatRule rule : ruleAdditions) {
            loadBalancer.update()
                    .defineInboundNatRule(rule.getInboundNatRuleName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackendPort(rule.getBackendPort())
                    .withFloatingIP(rule.getFloatingIp())
                    .attach();
        }

        for (InboundNatRule rule : ruleSubtractions) {
            loadBalancer.update()
                    .withoutInboundNatRule(rule.getInboundNatRuleName())
                    .apply();
        }

        //update nat pool
        List<InboundNatPool> poolAdditions = new ArrayList<>(getInboundNatPool());
        poolAdditions.removeAll(currentResource.getInboundNatPool());

        List<InboundNatPool> poolSubtractions = new ArrayList<>(currentResource.getInboundNatPool());
        poolSubtractions.removeAll(getInboundNatPool());

        for (InboundNatPool pool : poolAdditions) {
            loadBalancer.update()
                    .defineInboundNatPool(pool.getInboundNatPoolName())
                    .withProtocol(TransportProtocol.fromString(pool.getProtocol()))
                    .attach();
        }

        for (InboundNatPool pool : poolSubtractions) {
            loadBalancer.update()
                    .withoutInboundNatPool(pool.getInboundNatPoolName())
                    .apply();
        }

        //load balancing rules
        List<LoadBalancerRule> ruleAddition = new ArrayList<>(getLoadBalancerRule());
        ruleAddition.removeAll(currentResource.getLoadBalancerRule());

        List<LoadBalancerRule> ruleSubtraction = new ArrayList<>(currentResource.getLoadBalancerRule());
        ruleSubtraction.removeAll(getLoadBalancerRule());

        for (LoadBalancerRule rule : ruleAddition) {
            loadBalancer.update()
                    .defineLoadBalancingRule(rule.getLoadBalancerRuleName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackend(rule.getBackendPoolName())
                    .withProbe(rule.getHealthCheckProbeName())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                    .withFloatingIP(rule.getFloatingIp())
                    .attach();
        }

        for (LoadBalancerRule rule : ruleSubtraction) {
            loadBalancer.update()
                    .withoutLoadBalancingRule(rule.getLoadBalancerRuleName())
                    .apply();
        }

        //private frontends
        List<PrivateFrontend> privateAdditions = new ArrayList<>(getPrivateFrontend());
        privateAdditions.removeAll(currentResource.getPrivateFrontend());

        List<PrivateFrontend> privateSubtractions = new ArrayList<>(currentResource.getPrivateFrontend());
        privateSubtractions.removeAll(getPrivateFrontend());

        LoadBalancerPrivateFrontend.UpdateDefinitionStages.WithAttach withAttachPrivate;
        for (PrivateFrontend privateFrontend : privateAdditions) {
            Network network = client.networks().getById(privateFrontend.getNetworkId());

            withAttachPrivate = loadBalancer.update().definePrivateFrontend(privateFrontend.getPrivateFrontendName())
                    .withExistingSubnet(network, privateFrontend.getSubnetName());

            if (privateFrontend.getPrivateIpAddress() != null) {
                withAttachPrivate.withPrivateIPAddressStatic(privateFrontend.getPrivateIpAddress());
            } else {
                withAttachPrivate.withPrivateIPAddressDynamic();
            }
            withAttachPrivate.attach();
        }

        for (PrivateFrontend privateFrontend : privateSubtractions) {
            loadBalancer.update()
                    .withoutFrontend(privateFrontend.getPrivateFrontendName())
                    .apply();
        }

        //public frontends
        List<PublicFrontend> publicAdditions = new ArrayList<>(getPublicFrontend());
        publicAdditions.removeAll(currentResource.getPublicFrontend());

        List<PublicFrontend> publicSubtractions = new ArrayList<>(currentResource.getPublicFrontend());
        publicSubtractions.removeAll(getPublicFrontend());

        for (PublicFrontend publicFrontend : publicAdditions) {
            PublicIPAddress ip = client.publicIPAddresses()
                    .getByResourceGroup(getResourceGroupName(), publicFrontend.getPublicIpAddressName());

            loadBalancer.update()
                    .definePublicFrontend(publicFrontend.getPublicFrontendName())
                    .withExistingPublicIPAddress(ip)
                    .attach();
        }

        for (PublicFrontend publicFrontend : publicSubtractions) {
            loadBalancer.update()
                    .withoutFrontend(publicFrontend.getPublicFrontendName())
                    .apply();
        }
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.loadBalancers()
                .deleteByResourceGroup(getResourceGroupName(), getLoadBalancerName());
    }

    @Override
    public String toDisplayString() {
        return "load balancer " + getLoadBalancerName();
    }

    public List<HasNetworkInterfaces> toBackend(List<String> vmIds) {
        Azure client = createClient();

        List<HasNetworkInterfaces> virtualMachines = new ArrayList<>();
        vmIds.stream().forEach(vm -> virtualMachines.add(client.virtualMachines().getById(vm)));

        return virtualMachines;
    }
}
