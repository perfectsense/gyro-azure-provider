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
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithCreate;
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
 *             name: "load-balancer-example"
 *             resource-group-name: $(azure::resource-group resource-group-lb-example | resource-group-name)
 *             sku-basic: true
 *
 *
 *             public-frontend
 *                 name: "public-frontend"
 *                 public-ip-address-name: $(azure::public-ip-address public-ip-address | public-ip-address-name)
 *
 *                 inbound-nat-rule
 *                     name: "test-nat-rule"
 *                     frontend-name: "public-frontend"
 *                     frontend-port: 80
 *                     protocol: "TCP"
 *                 end
 *             end
 *
 *             backend-pool
 *                 name: "backend-pool-name"
 *                 virtual-machine-ids: [$(azure::virtual-machine virtual-machine-example-lb | virtual-machine-id)]
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
 *                 backend-pool-name: "backend-pool-name"
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
@ResourceName("load-balancer")
public class LoadBalancerResource extends AzureResource {

    private List<BackendPool> backendPool;
    private Map<String, Frontend> frontends;
    private List<HealthCheckProbeHttp> healthCheckProbeHttp;
    private List<HealthCheckProbeTcp> healthCheckProbeTcp;
    private String id;
    private String name;
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
        if (backendPool == null) {
            backendPool = new ArrayList<>();
        }

        return backendPool;
    }

    public void setBackendPool(List<BackendPool> backendPool) {
        this.backendPool = backendPool;
    }

    @ResourceDiffProperty(updatable = true)
    public Map<String, Frontend> frontends() {
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

    /**
     * The http probes associated with the load balancer. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<HealthCheckProbeHttp> getHealthCheckProbeHttp() {
        if (healthCheckProbeHttp == null) {
            healthCheckProbeHttp = new ArrayList<>();
        }

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
        if (healthCheckProbeTcp == null) {
            healthCheckProbeTcp = new ArrayList<>();
        }

        return healthCheckProbeTcp;
    }

    public void setHealthCheckProbeTcp(List<HealthCheckProbeTcp> healthCheckProbeTcp) {
        this.healthCheckProbeTcp = healthCheckProbeTcp;
    }

    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the load balancer. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The load balancer rules associated with the load balancer. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<LoadBalancerRule> getLoadBalancerRule() {
        if (loadBalancerRule == null) {
            loadBalancerRule = new ArrayList<>();
        }

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
     * The tags associated with the load balancer. (Optional)
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

        LoadBalancer loadBalancer = client.loadBalancers().getById(getId());

        //backend pools
        getBackendPool().clear();
        for (Map.Entry<String, LoadBalancerBackend> backend : loadBalancer.backends().entrySet()) {
            getBackendPool().add(new BackendPool(backend.getValue()));
        }

        //http probes
        getHealthCheckProbeHttp().clear();
        for (Map.Entry<String, LoadBalancerHttpProbe> httpProbe : loadBalancer.httpProbes().entrySet()) {
            getHealthCheckProbeHttp()
                    .add(new HealthCheckProbeHttp(httpProbe.getValue()));
        }

        //tcp probes
        getHealthCheckProbeTcp().clear();
        for (Map.Entry<String, LoadBalancerTcpProbe> tcpProbe : loadBalancer.tcpProbes().entrySet()) {
            getHealthCheckProbeTcp()
                    .add(new HealthCheckProbeTcp(tcpProbe.getValue()));
        }

        //public frontends
        getPublicFrontend().clear();
        for (Map.Entry<String, LoadBalancerPublicFrontend> publicFrontend : loadBalancer.publicFrontends().entrySet()) {
            getPublicFrontend().add(new PublicFrontend(publicFrontend.getValue()));
        }

        //private frontends
        getPrivateFrontend().clear();
        for (Map.Entry<String, LoadBalancerPrivateFrontend> privateFrontend : loadBalancer.privateFrontends().entrySet()) {
            getPrivateFrontend().add(new PrivateFrontend(privateFrontend.getValue()));
        }

        //load balancing rules
        getLoadBalancerRule().clear();
        for (Map.Entry<String, LoadBalancingRule> rule  : loadBalancer.loadBalancingRules().entrySet()) {
            getLoadBalancerRule().add(new LoadBalancerRule(rule.getValue()));
        }

        setId(loadBalancer.id());
        setSkuBasic(loadBalancer.sku() == LoadBalancerSkuType.BASIC ? true : false);

        getTags().clear();
        setTags(loadBalancer.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        LoadBalancer.DefinitionStages.WithLBRuleOrNat lb = client.loadBalancers()
                .define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());

        WithCreate buildLoadBalancer = null;

        //define the nat pools and rules
        for (Map.Entry<String, Frontend> frontends : frontends().entrySet()) {
            Frontend front = frontends.getValue();
            if (front.getInboundNatPool() != null) {
                for (InboundNatPool natPool: front.getInboundNatPool()) {

                    buildLoadBalancer = lb.defineInboundNatPool(natPool.getName())
                            .withProtocol(TransportProtocol.fromString(natPool.getProtocol()))
                            .fromFrontend(natPool.getFrontendName())
                            .fromFrontendPortRange(natPool.getFrontendPortRangeStart(), natPool.getFrontendPortRangeEnd())
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

        //define the backend pools
        for (BackendPool backendPool : getBackendPool()) {
            LoadBalancerBackend.DefinitionStages.Blank<WithCreate> backendCreate;

            backendCreate = buildLoadBalancer.defineBackend(backendPool.getName());

            if (!backendPool.getVirtualMachineIds().isEmpty()) {
                backendCreate.withExistingVirtualMachines(toBackend(backendPool.getVirtualMachineIds()));
            }
            backendCreate.attach();
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

        //define the public frontends
        for (PublicFrontend publicFrontend : getPublicFrontend()) {

            PublicIPAddress ip = client.publicIPAddresses()
                    .getByResourceGroup(getResourceGroupName(), publicFrontend.getPublicIpAddressName());

            buildLoadBalancer.definePublicFrontend(publicFrontend.getName())
                    .withExistingPublicIPAddress(ip)
                    .attach();
        }

        //define the private frontends
        for (PrivateFrontend privateFrontend : getPrivateFrontend()) {

            Network network = client.networks().getById(privateFrontend.getNetworkId());

            withAttachPrivate = buildLoadBalancer.definePrivateFrontend(privateFrontend.getName())
                    .withExistingSubnet(network, privateFrontend.getSubnetName());

            if (privateFrontend.getPrivateIpAddress() != null) {
                withAttachPrivate.withPrivateIPAddressStatic(privateFrontend.getPrivateIpAddress());
            } else {
                withAttachPrivate.withPrivateIPAddressDynamic();
            }
            withAttachPrivate.attach();
        }

        //define load balancer rules
        for (LoadBalancerRule rule : getLoadBalancerRule()) {

            buildLoadBalancer = lb.defineLoadBalancingRule(rule.getName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackend(rule.getBackendPoolName())
                    .toBackendPort(rule.getBackendPort())
                    .withProbe(rule.getHealthCheckProbeName())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                    .withFloatingIP(rule.getFloatingIp())
                    .attach();
        }

        LoadBalancer loadBalancer = buildLoadBalancer
                .withSku(getSkuBasic() ? LoadBalancerSkuType.BASIC : LoadBalancerSkuType.STANDARD)
                .withTags(getTags()).create();

        setId(loadBalancer.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        LoadBalancer loadBalancer = client.loadBalancers().getById(getId());

        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        LoadBalancer.Update updateLoadBalancer = loadBalancer.update();

        //Performs creates and deletes of new and removed elements

        //create added and delete removed backend pools
        List<BackendPool> backendAdditions = new ArrayList<>(getBackendPool());
        backendAdditions.removeAll(currentResource.getBackendPool());

        List<BackendPool> backendSubtractions = new ArrayList<>(currentResource.getBackendPool());
        backendSubtractions.removeAll(getBackendPool());

        for (BackendPool pool : backendAdditions) {

            updateLoadBalancer
                    .defineBackend(pool.getName())
                    .withExistingVirtualMachines(toBackend(pool.getVirtualMachineIds()))
                    .attach();
        }

        for (BackendPool pool : backendSubtractions) {

            updateLoadBalancer
                    .withoutBackend(pool.getName());
        }

        //create added and delete removed health check probes
        //http
        List<HealthCheckProbeHttp> httpProbeAdditions = new ArrayList<>(getHealthCheckProbeHttp());
        httpProbeAdditions.removeAll(currentResource.getHealthCheckProbeHttp());

        List<HealthCheckProbeHttp> httpProbeSubtractions = new ArrayList<>(currentResource.getHealthCheckProbeHttp());
        httpProbeSubtractions.removeAll(getHealthCheckProbeHttp());

        for (HealthCheckProbeHttp httpProbe : httpProbeAdditions) {

            updateLoadBalancer
                    .defineHttpProbe(httpProbe.getName())
                    .withRequestPath(httpProbe.getRequestPath())
                    .withIntervalInSeconds(httpProbe.getInterval())
                    .withNumberOfProbes(httpProbe.getProbes())
                    .withPort(httpProbe.getPort())
                    .attach();
        }

        for (HealthCheckProbeHttp httpProbe : httpProbeSubtractions) {

            updateLoadBalancer
                    .withoutProbe(httpProbe.getName());
        }

        //tcp
        List<HealthCheckProbeTcp> tcpProbeAdditions = new ArrayList<>(getHealthCheckProbeTcp());
        tcpProbeAdditions.removeAll(currentResource.getHealthCheckProbeTcp());

        List<HealthCheckProbeTcp> tcpProbeSubtractions = new ArrayList<>(currentResource.getHealthCheckProbeTcp());
        tcpProbeSubtractions.removeAll(getHealthCheckProbeTcp());

        for (HealthCheckProbeTcp tcpProbe : tcpProbeAdditions) {

            updateLoadBalancer
                    .defineTcpProbe(tcpProbe.getName())
                    .withPort(tcpProbe.getPort())
                    .withIntervalInSeconds(tcpProbe.getInterval())
                    .withNumberOfProbes(tcpProbe.getProbes())
                    .attach();
        }

        for (HealthCheckProbeTcp tcpProbe : tcpProbeSubtractions) {

            updateLoadBalancer
                    .withoutProbe(tcpProbe.getName());
        }

        //create added and delete removed private frontends
        List<PrivateFrontend> privateAdditions = new ArrayList<>(getPrivateFrontend());
        privateAdditions.removeAll(currentResource.getPrivateFrontend());

        List<PrivateFrontend> privateSubtractions = new ArrayList<>(currentResource.getPrivateFrontend());
        privateSubtractions.removeAll(getPrivateFrontend());

        LoadBalancerPrivateFrontend.UpdateDefinitionStages.WithAttach withAttachPrivate;
        for (PrivateFrontend privateFrontend : privateAdditions) {

            Network network = client.networks().getById(privateFrontend.getNetworkId());

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

        for (PrivateFrontend privateFrontend : privateSubtractions) {
            removeNatRulesAndPools(privateFrontend, updateLoadBalancer);
            updateLoadBalancer
                    .withoutFrontend(privateFrontend.getName());
        }

        //create added and delete removed public frontends
        List<PublicFrontend> publicAdditions = new ArrayList<>(getPublicFrontend());
        publicAdditions.removeAll(currentResource.getPublicFrontend());

        List<PublicFrontend> publicSubtractions = new ArrayList<>(currentResource.getPublicFrontend());
        publicSubtractions.removeAll(getPublicFrontend());

        for (PublicFrontend publicFrontend : publicAdditions) {
            PublicIPAddress ip = client.publicIPAddresses()
                    .getByResourceGroup(getResourceGroupName(), publicFrontend.getPublicIpAddressName());

            updateLoadBalancer
                    .definePublicFrontend(publicFrontend.getName())
                    .withExistingPublicIPAddress(ip)
                    .attach();

            addNatRulesAndPools(publicFrontend, updateLoadBalancer);
        }

        for (PublicFrontend publicFrontend : publicSubtractions) {
            removeNatRulesAndPools(publicFrontend, updateLoadBalancer);
            updateLoadBalancer
                    .withoutFrontend(publicFrontend.getName());
        }

        //create added and delete removed load balancer rules
        List<LoadBalancerRule> ruleAdditions = new ArrayList<>(getLoadBalancerRule());
        ruleAdditions.removeAll(currentResource.getLoadBalancerRule());

        List<LoadBalancerRule> ruleSubtractions = new ArrayList<>(currentResource.getLoadBalancerRule());
        ruleSubtractions.removeAll(getLoadBalancerRule());

        for (LoadBalancerRule rule : ruleAdditions) {

            updateLoadBalancer
                    .defineLoadBalancingRule(rule.getName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackend(rule.getBackendPoolName())
                    .withProbe(rule.getHealthCheckProbeName())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                    .withFloatingIP(rule.getFloatingIp())
                    .attach();
        }

        for (LoadBalancerRule rule : ruleSubtractions) {

            updateLoadBalancer
                    .withoutLoadBalancingRule(rule.getName());
        }


        //Performs updates of existing elements
        //http probes
        for (HealthCheckProbeHttp httpProbe : getHealthCheckProbeHttp()) {
            if (!httpProbeAdditions.contains(httpProbe) && !httpProbeSubtractions.contains(httpProbe)) {

                updateLoadBalancer
                        .updateHttpProbe(httpProbe.getName())
                        .withIntervalInSeconds(httpProbe.getInterval())
                        .withNumberOfProbes(httpProbe.getProbes())
                        .withRequestPath(httpProbe.getRequestPath())
                        .withPort(httpProbe.getPort());
            }
        }

        //tcp probes
        for (HealthCheckProbeTcp tcpProbe : getHealthCheckProbeTcp()) {
            if (!tcpProbeAdditions.contains(tcpProbe) && !tcpProbeSubtractions.contains(tcpProbe)) {

                updateLoadBalancer
                        .updateTcpProbe(tcpProbe.getName())
                        .withIntervalInSeconds(tcpProbe.getInterval())
                        .withNumberOfProbes(tcpProbe.getProbes())
                        .withPort(tcpProbe.getPort());
            }
        }

        //private frontends
        LoadBalancerPrivateFrontend.Update withAttachPrivateIp;
        for (PrivateFrontend privateFrontend : getPrivateFrontend()) {
            if (!privateAdditions.contains(privateFrontend) && !privateSubtractions.contains(privateFrontend)) {

                Network network = client.networks().getById(privateFrontend.getNetworkId());

                withAttachPrivateIp = updateLoadBalancer
                        .updatePrivateFrontend(privateFrontend.getName())
                        .withExistingSubnet(network, privateFrontend.getSubnetName());

                if (privateFrontend.getPrivateIpAddress() != null) {
                    withAttachPrivateIp.withPrivateIPAddressStatic(privateFrontend.getPrivateIpAddress());
                } else {
                    withAttachPrivateIp.withPrivateIPAddressDynamic();
                }

                updateNatPoolsAndRules(privateFrontend, currentResource.frontends().get(privateFrontend.getName()), updateLoadBalancer);
            }
        }

        //public frontends
        for (PublicFrontend publicFrontend : getPublicFrontend()) {
            if (!publicAdditions.contains(publicFrontend) && !publicSubtractions.contains(publicFrontend)) {

                PublicIPAddress ip = client.publicIPAddresses()
                        .getByResourceGroup(getResourceGroupName(), publicFrontend.getPublicIpAddressName());

                updateLoadBalancer
                        .updatePublicFrontend(publicFrontend.getName())
                        .withExistingPublicIPAddress(ip);

                updateNatPoolsAndRules(publicFrontend, currentResource.frontends().get(publicFrontend.getName()), updateLoadBalancer);
            }
        }

        //load balancer rules
        for (LoadBalancerRule rule : getLoadBalancerRule()) {
            if (!ruleAdditions.contains(rule) && !ruleSubtractions.contains(rule)) {

                updateLoadBalancer
                        .updateLoadBalancingRule(rule.getName())
                        .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                        .fromFrontend(rule.getFrontendName())
                        .fromFrontendPort(rule.getFrontendPort())
                        .toBackendPort(rule.getBackendPort())
                        .withFloatingIP(rule.getFloatingIp())
                        .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                        .withProbe(rule.getHealthCheckProbeName());
            }
        }

        //tags
        updateLoadBalancer
                .withTags(getTags());

        updateLoadBalancer.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.loadBalancers()
                .deleteByResourceGroup(getResourceGroupName(), getName());
    }

    @Override
    public String toDisplayString() {
        return "load balancer " + getName();
    }

    private List<HasNetworkInterfaces> toBackend(List<String> vmIds) {
        Azure client = createClient();

        List<HasNetworkInterfaces> virtualMachines = new ArrayList<>();
        vmIds.stream().forEach(vm -> virtualMachines.add(client.virtualMachines().getById(vm)));

        return virtualMachines;
    }

    private void updateNatPoolsAndRules(Frontend frontend, Frontend currentResource, LoadBalancer.Update updateLoadBalancer) {
        List<InboundNatRule> ruleAdditions = new ArrayList<>(frontend.getInboundNatRule());
        ruleAdditions.removeAll(currentResource.getInboundNatRule());

        List<InboundNatRule> ruleSubtractions = new ArrayList<>(currentResource.getInboundNatRule());
        ruleSubtractions.removeAll(frontend.getInboundNatRule());

        List<InboundNatPool> poolAdditions = new ArrayList<>(frontend.getInboundNatPool());
        poolAdditions.removeAll(currentResource.getInboundNatPool());

        List<InboundNatPool> poolSubtractions = new ArrayList<>(currentResource.getInboundNatPool());
        poolSubtractions.removeAll(frontend.getInboundNatPool());

        removeNatPools(poolSubtractions, updateLoadBalancer);
        removeNatRules(ruleSubtractions, updateLoadBalancer);

        addNatPools(poolAdditions, updateLoadBalancer);
        addNatRules(ruleAdditions, updateLoadBalancer);

        for (InboundNatPool pool : frontend.getInboundNatPool()) {
            if (!poolAdditions.contains(pool) && !poolSubtractions.contains(pool)) {
                updateNatPool(pool, updateLoadBalancer);
            }
        }

        for (InboundNatRule rule : frontend.getInboundNatRule()) {
            if (!ruleAdditions.contains(rule) && !ruleSubtractions.contains(rule)) {
                updateNatRule(rule, updateLoadBalancer);
            }
        }
    }

    private void addNatRulesAndPools(Frontend frontend, LoadBalancer.Update updateLoadBalancer) {
        addNatPools(frontend.getInboundNatPool(), updateLoadBalancer);
        addNatRules(frontend.getInboundNatRule(), updateLoadBalancer);
    }

    private void removeNatRulesAndPools(Frontend frontend, LoadBalancer.Update updateLoadBalancer) {
        removeNatPools(frontend.getInboundNatPool(), updateLoadBalancer);
        removeNatRules(frontend.getInboundNatRule(), updateLoadBalancer);
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

            withPortRange = (LoadBalancerInboundNatPool.UpdateDefinitionStages.WithBackendPort) withFrontend.fromFrontendPortRange(pool.getFrontendPortRangeStart(), pool.getFrontendPortRangeEnd());

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

    private void removeNatPools(List<InboundNatPool> pools, LoadBalancer.Update updateLoadBalancer) {
        for (InboundNatPool pool : pools) {

            updateLoadBalancer
                    .withoutInboundNatPool(pool.getName());
        }
    }

    private void removeNatRules(List<InboundNatRule> rules, LoadBalancer.Update updateLoadBalancer) {
        for (InboundNatRule rule : rules) {

            updateLoadBalancer
                    .withoutInboundNatPool(rule.getName());
        }
    }

    private void updateNatPool(InboundNatPool pool, LoadBalancer.Update updateLoadBalancer) {

            updateLoadBalancer
                    .updateInboundNatPool(pool.getName())
                    .withProtocol(TransportProtocol.fromString(pool.getProtocol()))
                    .fromFrontend(pool.getFrontendName())
                    .fromFrontendPortRange(pool.getFrontendPortRangeStart(), pool.getFrontendPortRangeEnd())
                    .toBackendPort(pool.getBackendPort());
    }

    private void updateNatRule(InboundNatRule rule, LoadBalancer.Update updateLoadBalancer) {

            updateLoadBalancer
                    .updateInboundNatRule(rule.getName())
                    .withProtocol(TransportProtocol.fromString(rule.getProtocol()))
                    .fromFrontend(rule.getFrontendName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackendPort(rule.getBackendPort())
                    .withFloatingIP(rule.getFloatingIp());
    }
}
