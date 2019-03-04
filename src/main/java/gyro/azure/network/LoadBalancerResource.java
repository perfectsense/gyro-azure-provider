package gyro.azure.network;

import gyro.azure.AzureResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerBackend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ResourceName("load-balancer")
public class LoadBalancerResource extends AzureResource {

    private Frontend frontend;
    private String loadBalancerName;
    private List<LoadBalancerRule> loadBalancerRule;
    private String resourceGroupName;
    private Map<String, String> tags;

    public Frontend getFrontend() {
        return frontend;
    }

    public void setFrontend(Frontend frontend) {
        this.frontend = frontend;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    public List<LoadBalancerRule> getLoadBalancerRule() {
        return loadBalancerRule;
    }

    public void setLoadBalancerRule(List<LoadBalancerRule> loadBalancerRule) {
        this.loadBalancerRule = loadBalancerRule;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

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
                    .fromFrontend(rule.getFrontendIpConfiguration().getFrontendIpConfigurationName())
                    .fromFrontendPort(rule.getFrontendPort())
                    .toBackend(rule.getBackendPool().getBackendPoolName())
                    .toBackendPort(rule.getBackendPort())
                    .withProbe(rule.getHealthCheckProbe().getHealthProbeName())
                    .withIdleTimeoutInMinutes(rule.getIdleTimeoutInMinutes())
                    .attach();

            //use frontend configuration to set inbound nat rules
            FrontendIpConfiguration ipConfiguration = rule.getFrontendIpConfiguration();
            for (InboundNatRule natRule : ipConfiguration.getInboundNatRule()) {
                chain.defineInboundNatRule(natRule.getInboundNatRuleName())
                    .withProtocol(TransportProtocol.fromString(natRule.getProtocol()))
                        .fromExistingSubnet("load-balancer-network-example", "subnet1")
                        .fromFrontendPort(rule.getFrontendPort())
                    .attach();
            }
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

        //define public or private frontend and attach
        Frontend frontend = getFrontend();
        if (getFrontend().getPublicFrontEnd() == true) {
            LoadBalancerPublicFrontend.DefinitionStages.WithAttach withAttach = null;
            withAttach = chain.definePublicFrontend(frontend.getFrontendName())
                    .withExistingPublicIPAddress(frontend.getStaticPublicIpAddress());

            withAttach.attach();

        } else {
            LoadBalancerPrivateFrontend.DefinitionStages.WithAttach withAttachPrivate = null;
            withAttachPrivate  = chain.definePrivateFrontend(frontend.getFrontendName())
                    .withExistingSubnet(frontend.getNetworkName(), frontend.getSubnet());;
            if (frontend.getStaticPublicIp() == true) {
                withAttachPrivate.withPrivateIPAddressStatic(frontend.getStaticPublicIpAddress());
            } else {
                withAttachPrivate.withPrivateIPAddressDynamic();
            }
            withAttachPrivate.attach();
        }

        //add tags
        chain.withTags(getTags()).create();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {}

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
