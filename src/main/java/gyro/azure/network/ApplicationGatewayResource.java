package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayRedirectConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceName("application-gateway")
public class ApplicationGatewayResource extends AzureResource {
    private String resourceGroupName;
    private String networkId;
    private String publicIpAddressName;
    private String subnet;
    private String applicationGatewayName;
    private List<RequestRoutingRule> requestRoutingRule;
    private List<Listener> listener;
    private List<Backend> backend;
    private List<BackendHttpConfiguration> backendHttpConfiguration;
    private List<RedirectConfiguration> redirectConfiguration;
    private List<Probe> probe;
    private String skuSize;
    private Integer instanceCount;
    private Map<String, String> tags;
    private Boolean enableHttp2;
    private Boolean privateFrontEnd;

    private String applicationGatewayId;

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getPublicIpAddressName() {
        return publicIpAddressName;
    }

    public void setPublicIpAddressName(String publicIpAddressName) {
        this.publicIpAddressName = publicIpAddressName;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getApplicationGatewayName() {
        return applicationGatewayName;
    }

    public void setApplicationGatewayName(String applicationGatewayName) {
        this.applicationGatewayName = applicationGatewayName;
    }

    @ResourceDiffProperty(updatable = true)
    public List<RequestRoutingRule> getRequestRoutingRule() {
        if (requestRoutingRule == null) {
            requestRoutingRule = new ArrayList<>();
        }

        return requestRoutingRule;
    }

    public void setRequestRoutingRule(List<RequestRoutingRule> requestRoutingRule) {
        this.requestRoutingRule = requestRoutingRule;
    }

    @ResourceDiffProperty(updatable = true)
    public List<Listener> getListener() {
        if (listener == null) {
            listener = new ArrayList<>();
        }

        return listener;
    }

    public void setListener(List<Listener> listener) {
        this.listener = listener;
    }

    @ResourceDiffProperty(updatable = true)
    public List<Backend> getBackend() {
        if (backend == null) {
            backend = new ArrayList<>();
        }

        return backend;
    }

    public void setBackend(List<Backend> backend) {
        this.backend = backend;
    }

    @ResourceDiffProperty(updatable = true)
    public List<BackendHttpConfiguration> getBackendHttpConfiguration() {
        if (backendHttpConfiguration == null) {
            backendHttpConfiguration = new ArrayList<>();
        }

        return backendHttpConfiguration;
    }

    public void setBackendHttpConfiguration(List<BackendHttpConfiguration> backendHttpConfiguration) {
        this.backendHttpConfiguration = backendHttpConfiguration;
    }

    @ResourceDiffProperty(updatable = true)
    public List<RedirectConfiguration> getRedirectConfiguration() {
        if (redirectConfiguration == null) {
            redirectConfiguration = new ArrayList<>();
        }

        return redirectConfiguration;
    }

    public void setRedirectConfiguration(List<RedirectConfiguration> redirectConfiguration) {
        this.redirectConfiguration = redirectConfiguration;
    }

    @ResourceDiffProperty(updatable = true)
    public List<Probe> getProbe() {
        if (probe == null) {
            probe = new ArrayList<>();
        }

        return probe;
    }

    public void setProbe(List<Probe> probe) {
        this.probe = probe;
    }

    @ResourceDiffProperty(updatable = true)
    public String getSkuSize() {
        return skuSize != null ? skuSize.toUpperCase() : null;
    }

    public void setSkuSize(String skuSize) {
        this.skuSize = skuSize;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
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

    @ResourceDiffProperty(updatable = true)
    public Boolean getEnableHttp2() {
        if (enableHttp2 == null) {
            enableHttp2 = false;
        }

        return enableHttp2;
    }

    public void setEnableHttp2(Boolean enableHttp2) {
        this.enableHttp2 = enableHttp2;
    }

    public Boolean getPrivateFrontEnd() {
        if (privateFrontEnd == null) {
            privateFrontEnd = false;
        }

        return privateFrontEnd;
    }

    public void setPrivateFrontEnd(Boolean privateFrontEnd) {
        this.privateFrontEnd = privateFrontEnd;
    }

    public String getApplicationGatewayId() {
        return applicationGatewayId;
    }

    public void setApplicationGatewayId(String applicationGatewayId) {
        this.applicationGatewayId = applicationGatewayId;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ApplicationGateway applicationGateway = client.applicationGateways().getById(getApplicationGatewayId());

        loadApplicationGateway(applicationGateway);

        return true;
    }

    @Override
    public void create() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Azure client = createClient();

        ApplicationGateway.DefinitionStages.WithRequestRoutingRule withRequestRoutingRule = client.applicationGateways()
            .define(getApplicationGatewayName()).withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroupName());

        ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate attach;

        if (getPrivateFrontEnd()) {
            attach = withRequestRoutingRule
                .defineRequestRoutingRule("Default_rule")
                .fromPrivateFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(80)
                .toBackendIPAddress("10.0.0.0")
                .attach();
        } else {
            attach = withRequestRoutingRule
                .defineRequestRoutingRule("Default_rule")
                .fromPublicFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(80)
                .toBackendIPAddress("10.0.0.0")
                .attach();
        }

        ApplicationGateway.DefinitionStages.WithCreate withCreate;
        if (getEnableHttp2()) {
            withCreate = attach.withHttp2();
        } else {
            withCreate = attach.withoutHttp2();
        }

        ApplicationGateway applicationGateway = withCreate.withExistingPublicIPAddress(
                client.publicIPAddresses().getByResourceGroup(getResourceGroupName(), getPublicIpAddressName())
            )
            .withInstanceCount(getInstanceCount())
            .withSize(ApplicationGatewaySkuName.fromString(getSkuSize()))
            .withTags(getTags())
            .withExistingSubnet(getNetworkId(), getSubnet())
            .create();

        stopWatch.stop();

        System.out.println("\n\n -->Initial create time : " + (stopWatch.getTime() / 60000));

        stopWatch.reset();

        stopWatch.start();

        setApplicationGatewayId(applicationGateway.id());

        ApplicationGateway.Update update = applicationGateway.update();

        // add relevant rule

        for (Listener listener : getListener()) {
            update = listener.createListener(update);
        }

        for (Backend backend : getBackend()) {
            update = backend.createBackend(update);
        }

        for (BackendHttpConfiguration backendHttpConfiguration : getBackendHttpConfiguration()) {
            update = backendHttpConfiguration.createBackendHttpConfiguration(update);
        }

        for (RedirectConfiguration redirectConfiguration: getRedirectConfiguration()) {
            update = redirectConfiguration.createRedirectConfiguration(update);
        }

        for (Probe probe: getProbe()) {
            update = probe.createProbe(update);
        }

        for (RequestRoutingRule requestRoutingRule : getRequestRoutingRule()) {
            update = requestRoutingRule.createRequestRoutingRule(update);
        }

        applicationGateway = update.apply();

        stopWatch.stop();

        System.out.println("\n\n -->Rule create time : " + (stopWatch.getTime() / 60000));

        stopWatch.reset();

        stopWatch.start();

        // remove default rule

        Set<String> requestRoutingRuleNames = getRequestRoutingRule().stream()
            .map(RequestRoutingRule::getRequestRoutingRuleName).collect(Collectors.toSet());

        List<String> requestRoutingRuleDeleteList = applicationGateway
            .requestRoutingRules().keySet().stream()
            .filter(o -> !requestRoutingRuleNames.contains(o))
            .collect(Collectors.toList());

        update = applicationGateway.update();

        for (String requestRoutingRuleName : requestRoutingRuleDeleteList) {
            update = update.withoutRequestRoutingRule(requestRoutingRuleName);
        }

        applicationGateway = update.apply();

        stopWatch.stop();

        System.out.println("\n\n -->Remove default rule time : " + (stopWatch.getTime() / 60000));

        stopWatch.reset();

        stopWatch.start();

        update = applicationGateway.update();

        // remove default listeners, backend and backendHttpConfig

        Set<String> listenerNames = getListener().stream().map(Listener::getListenerName).collect(Collectors.toSet());

        List<String> listenerDeleteList = applicationGateway
            .listeners().keySet().stream()
            .filter(o -> !listenerNames.contains(o))
            .collect(Collectors.toList());

        for (String listenerName : listenerDeleteList) {
            update = update.withoutListener(listenerName);
        }

        Set<String> backendNames = getBackend().stream().map(Backend::getBackendName).collect(Collectors.toSet());

        List<String> backendDeleteList = applicationGateway
            .backends().keySet().stream()
            .filter(o -> !backendNames.contains(o))
            .collect(Collectors.toList());

        for (String backendName : backendDeleteList) {
            update = update.withoutBackend(backendName);
        }

        Set<String> backendHttpConfigurationNames = getBackendHttpConfiguration().stream()
            .map(BackendHttpConfiguration::getBackendHttpConfigurationName).collect(Collectors.toSet());

        List<String> backendHttpConfigurationDeleteList = applicationGateway
            .backendHttpConfigurations().keySet().stream()
            .filter(o -> !backendHttpConfigurationNames.contains(o))
            .collect(Collectors.toList());

        for (String backendHttpConfigurationName : backendHttpConfigurationDeleteList) {
            update = update.withoutBackendHttpConfiguration(backendHttpConfigurationName);
        }

        applicationGateway = update.apply();

        loadApplicationGateway(applicationGateway);

        stopWatch.stop();

        System.out.println("\n\n -->Remove listeners, backend and httpconfig time : " + (stopWatch.getTime() / 60000));
    }

    @Override
    public void update(Resource resource, Set<String> set) {
        Azure client = createClient();

        ApplicationGateway applicationGateway = client.applicationGateways().getById(getApplicationGatewayId());

        ApplicationGateway.Update update = applicationGateway.update();

        ApplicationGatewayResource oldApplicationGatewayResource = (ApplicationGatewayResource) resource;

        update = saveTags(oldApplicationGatewayResource.getTags(), update);

        //update = saveRequestRoutingRule(oldApplicationGatewayResource.getRequestRoutingRule(), update);

        //update = saveListener(oldApplicationGatewayResource.getListener(), update);

        //update = saveRedirectConfiguration(oldApplicationGatewayResource.getRedirectConfiguration(), update);

        //update = saveBackendHttpConfiguration(oldApplicationGatewayResource.getBackendHttpConfiguration(), update);

        update = saveProbe(oldApplicationGatewayResource.getProbe(), update);

        update = saveBackend(oldApplicationGatewayResource.getBackend(), update);

        applicationGateway = update.apply();

        loadApplicationGateway(applicationGateway);
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.applicationGateways().deleteById(getApplicationGatewayId());
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("application gateway");

        if (!ObjectUtils.isBlank(getApplicationGatewayName())) {
            sb.append(" - ").append(getApplicationGatewayName());
        }

        if (!ObjectUtils.isBlank(getApplicationGatewayId())) {
            sb.append(" - ").append(getApplicationGatewayId());
        }

        return sb.toString();
    }

    private void loadApplicationGateway(ApplicationGateway applicationGateway) {
        setApplicationGatewayId(applicationGateway.id());
        setInstanceCount(applicationGateway.instanceCount());
        setEnableHttp2(applicationGateway.isHttp2Enabled());
        setSkuSize(applicationGateway.sku().name().toString());
        setPrivateFrontEnd(applicationGateway.isPrivate());
        setTags(applicationGateway.tags());

        getBackend().clear();
        for (ApplicationGatewayBackend applicationGatewayBackend : applicationGateway.backends().values()) {
            Backend backend = new Backend(applicationGatewayBackend);
            getBackend().add(backend);
        }

        getListener().clear();
        for (ApplicationGatewayListener applicationGatewayListener : applicationGateway.listeners().values()) {
            Listener listener = new Listener(applicationGatewayListener);
            getListener().add(listener);
        }

        getBackendHttpConfiguration().clear();
        for (ApplicationGatewayBackendHttpConfiguration backendHttpConfig : applicationGateway.backendHttpConfigurations().values()) {
            BackendHttpConfiguration backendHttpConfiguration = new BackendHttpConfiguration(backendHttpConfig);
            getBackendHttpConfiguration().add(backendHttpConfiguration);
        }

        getRedirectConfiguration().clear();
        for (ApplicationGatewayRedirectConfiguration applicationGatewayRedirectConfig: applicationGateway.redirectConfigurations().values()) {
            RedirectConfiguration redirectConfiguration = new RedirectConfiguration(applicationGatewayRedirectConfig);
            getRedirectConfiguration().add(redirectConfiguration);
        }

        getProbe().clear();
        for (ApplicationGatewayProbe applicationGatewayProbe : applicationGateway.probes().values()) {
            Probe probe = new Probe(applicationGatewayProbe);
            getProbe().add(probe);
        }

        getRequestRoutingRule().clear();
        for (ApplicationGatewayRequestRoutingRule applicationGatewayRequestRoutingRule: applicationGateway.requestRoutingRules().values()) {
            RequestRoutingRule requestRoutingRule = new RequestRoutingRule(applicationGatewayRequestRoutingRule);
            getRequestRoutingRule().add(requestRoutingRule);
        }
    }

    private Update saveRequestRoutingRule(List<RequestRoutingRule> oldRequestRoutingRules, Update update) {
        Set<String> requestRoutingRuleNames = getRequestRoutingRule().stream()
            .map(RequestRoutingRule::getRequestRoutingRuleName).collect(Collectors.toSet());

        List<String> requestRoutingRuleDeleteList = oldRequestRoutingRules.stream()
            .filter(o -> !requestRoutingRuleNames.contains(o.getRequestRoutingRuleName()))
            .map(RequestRoutingRule::getRequestRoutingRuleName)
            .collect(Collectors.toList());

        for (String requestRoutingRuleName : requestRoutingRuleDeleteList) {
            update = update.withoutRequestRoutingRule(requestRoutingRuleName);
        }

        Set<String> oldRequestRoutingRuleNames = oldRequestRoutingRules.stream()
            .map(RequestRoutingRule::getRequestRoutingRuleName).collect(Collectors.toSet());

        List<RequestRoutingRule> requestRoutingRuleModificationList = getRequestRoutingRule().stream()
            .filter(o -> oldRequestRoutingRuleNames.contains(o.getRequestRoutingRuleName()))
            .collect(Collectors.toList());

        for (RequestRoutingRule requestRoutingRule : requestRoutingRuleModificationList) {
            update = requestRoutingRule.updateRequestRoutingRule(update);
        }

        List<RequestRoutingRule> requestRoutingRuleAdditionList = getRequestRoutingRule().stream()
            .filter(o -> !oldRequestRoutingRuleNames.contains(o.getRequestRoutingRuleName()))
            .collect(Collectors.toList());

        for (RequestRoutingRule requestRoutingRule : requestRoutingRuleAdditionList) {
            update = requestRoutingRule.createRequestRoutingRule(update);
        }

        return update;
    }

    private Update saveListener(List<Listener> oldListeners, Update update) {
        Set<String> listenerNames = getListener().stream()
            .map(Listener::getListenerName).collect(Collectors.toSet());

        List<String> listenerDeleteList = oldListeners.stream()
            .filter(o -> !listenerNames.contains(o.getListenerName()))
            .map(Listener::getListenerName)
            .collect(Collectors.toList());

        for (String listenerName : listenerDeleteList) {
            update = update.withoutListener(listenerName);
        }

        Set<String> oldListenerNames = oldListeners.stream()
            .map(Listener::getListenerName).collect(Collectors.toSet());

        List<Listener> listenerModificationList = getListener().stream()
            .filter(o -> oldListenerNames.contains(o.getListenerName()))
            .collect(Collectors.toList());

        for (Listener listener : listenerModificationList) {
            update = listener.updateListener(update);
        }

        List<Listener> listenerAdditionList = getListener().stream()
            .filter(o -> !oldListenerNames.contains(o.getListenerName()))
            .collect(Collectors.toList());

        for (Listener listener : listenerAdditionList) {
            update = listener.createListener(update);
        }

        return update;
    }

    private Update saveRedirectConfiguration(List<RedirectConfiguration> oldRedirectConfigurations, Update update) {
        Set<String> redirectConfigurationNames = getRedirectConfiguration().stream()
            .map(RedirectConfiguration::getRedirectConfigurationName).collect(Collectors.toSet());

        List<String> redirectConfigurationDeleteList = oldRedirectConfigurations.stream()
            .filter(o -> !redirectConfigurationNames.contains(o.getRedirectConfigurationName()))
            .map(RedirectConfiguration::getRedirectConfigurationName)
            .collect(Collectors.toList());

        for (String redirectConfigurationName : redirectConfigurationDeleteList) {
            update = update.withoutRedirectConfiguration(redirectConfigurationName);
        }

        Set<String> oldRedirectConfigurationNames = oldRedirectConfigurations.stream()
            .map(RedirectConfiguration::getRedirectConfigurationName).collect(Collectors.toSet());

        List<RedirectConfiguration> redirectConfigurationModificationList = getRedirectConfiguration().stream()
            .filter(o -> oldRedirectConfigurationNames.contains(o.getRedirectConfigurationName()))
            .collect(Collectors.toList());

        for (RedirectConfiguration redirectConfiguration : redirectConfigurationModificationList) {
            update = redirectConfiguration.updateRedirectConfiguration(update);
        }

        List<RedirectConfiguration> redirectConfigurationAdditionList = getRedirectConfiguration().stream()
            .filter(o -> !oldRedirectConfigurationNames.contains(o.getRedirectConfigurationName()))
            .collect(Collectors.toList());

        for (RedirectConfiguration redirectConfiguration : redirectConfigurationAdditionList) {
            update = redirectConfiguration.createRedirectConfiguration(update);
        }

        return update;
    }

    private Update saveBackendHttpConfiguration(List<BackendHttpConfiguration> oldBackendHttpConfigurations, Update update) {
        Set<String> backendHttpConfigurationNames = getBackendHttpConfiguration().stream()
            .map(BackendHttpConfiguration::getBackendHttpConfigurationName).collect(Collectors.toSet());

        List<String> backendHttpConfigurationDeleteList = oldBackendHttpConfigurations.stream()
            .filter(o -> !backendHttpConfigurationNames.contains(o.getBackendHttpConfigurationName()))
            .map(BackendHttpConfiguration::getBackendHttpConfigurationName)
            .collect(Collectors.toList());

        for (String backendHttpConfigurationName : backendHttpConfigurationDeleteList) {
            update = update.withoutBackendHttpConfiguration(backendHttpConfigurationName);
        }

        Set<String> oldBackendHttpConfigurationNames = oldBackendHttpConfigurations.stream()
            .map(BackendHttpConfiguration::getBackendHttpConfigurationName).collect(Collectors.toSet());

        List<BackendHttpConfiguration> backendHttpConfigurationModificationList = getBackendHttpConfiguration().stream()
            .filter(o -> oldBackendHttpConfigurationNames.contains(o.getBackendHttpConfigurationName()))
            .collect(Collectors.toList());

        for (BackendHttpConfiguration backendHttpConfiguration : backendHttpConfigurationModificationList) {
            update = backendHttpConfiguration.updateBackendHttpConfiguration(update);
        }

        List<BackendHttpConfiguration> backendHttpConfigurationAdditionList = getBackendHttpConfiguration().stream()
            .filter(o -> !oldBackendHttpConfigurationNames.contains(o.getBackendHttpConfigurationName()))
            .collect(Collectors.toList());

        for (BackendHttpConfiguration backendHttpConfiguration : backendHttpConfigurationAdditionList) {
            update = backendHttpConfiguration.createBackendHttpConfiguration(update);
        }

        return update;
    }

    private Update saveProbe(List<Probe> oldProbes, Update update) {
        Set<String> probeNames = getProbe().stream()
            .map(Probe::getProbeName).collect(Collectors.toSet());

        List<String> probeDeleteList = oldProbes.stream()
            .filter(o -> !probeNames.contains(o.getProbeName()))
            .map(Probe::getProbeName)
            .collect(Collectors.toList());

        for (String probeName : probeDeleteList) {
            update = update.withoutProbe(probeName);
        }

        Set<String> oldProbeNames = oldProbes.stream()
            .map(Probe::getProbeName).collect(Collectors.toSet());

        List<Probe> probeModificationList = getProbe().stream()
            .filter(o -> oldProbeNames.contains(o.getProbeName()))
            .collect(Collectors.toList());

        for (Probe probe : probeModificationList) {
            update = probe.updateProbe(update);
        }

        List<Probe> probeAdditionList = getProbe().stream()
            .filter(o -> !oldProbeNames.contains(o.getProbeName()))
            .collect(Collectors.toList());

        for (Probe probe : probeAdditionList) {
            update = probe.createProbe(update);
        }

        return update;
    }

    private Update saveBackend(List<Backend> oldBackends, Update update) {
        Set<String> backendNames = getBackend().stream()
            .map(Backend::getBackendName).collect(Collectors.toSet());

        List<String> backendDeleteList = oldBackends.stream()
            .filter(o -> !backendNames.contains(o.getBackendName()))
            .map(Backend::getBackendName)
            .collect(Collectors.toList());

        for (String backendName : backendDeleteList) {
            update = update.withoutBackend(backendName);
        }

        Set<String> oldBackendNames = oldBackends.stream()
            .map(Backend::getBackendName).collect(Collectors.toSet());

        Map<String, Backend> oldBackendMap = oldBackends.stream()
            .collect(Collectors.toMap(Backend::getBackendName, o -> o));

        List<Backend> backendModificationList = getBackend().stream()
            .filter(o -> oldBackendNames.contains(o.getBackendName()))
            .collect(Collectors.toList());

        for (Backend backend : backendModificationList) {
            Backend oldBackend = oldBackendMap.get(backend.getBackendName());
            update = backend.updateBackend(update, oldBackend.getIpAddresses(), oldBackend.getFqdns());
        }

        List<Backend> backendAdditionList = getBackend().stream()
            .filter(o -> !oldBackendNames.contains(o.getBackendName()))
            .collect(Collectors.toList());

        for (Backend backend : backendAdditionList) {
            update = backend.createBackend(update);
        }

        return update;
    }

    private Update saveTags(Map<String, String> oldTags, Update update) {
        Map<String, String> addTags;

        if (oldTags.isEmpty()) {
            addTags = getTags();
        } else {
            List<String> removeTags = oldTags.keySet().stream()
                .filter(o -> !getTags().containsKey(o))
                .collect(Collectors.toList());

            for (String tag: removeTags) {
                update = update.withoutTag(tag);
            }

            addTags = getTags().entrySet().stream()
                .filter(o -> oldTags.containsKey(o.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        if (!addTags.isEmpty()) {
            update = update.withTags(addTags);
        }

        return update;
    }
}
