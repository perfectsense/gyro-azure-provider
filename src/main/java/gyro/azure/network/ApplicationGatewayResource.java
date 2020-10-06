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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthStatus;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfigurationHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendServerHealth;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayRedirectConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRuleType;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGatewayTier;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an Application Gateway.
 *
 * Azure Application Gateways are managed using a combination of resource configuration and commands.
 *
 * Create an Azure Application Gateway using the ``azure::application-gateway`` resource. After the Application Gateway is
 * created use the ``gyro azure application-gateway`` command to manage certificates within the application gateway.
 * See documentation below on how to create, add, or remove a certificate from an application gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::application-gateway application-gateway-example
 *         name: "application-gateway-example"
 *         resource-group: $(azure::resource-group resource-group-example-AG)
 *         network: $(azure::network network-example-AG)
 *         subnet: "subnet1"
 *         public-ip-address: $(azure::public-ip-address public-ip-address-example-AG)
 *         sku-tier: "STANDARD"
 *         sku-size: "STANDARD_SMALL"
 *         instance-count: 1
 *         enable-http2: true
 *         tags: {
 *             Name: "application-gateway-example"
 *         }
 *
 *         request-routing-rule
 *             name: "request-routing-rule-example"
 *             listener: "listener-example"
 *             backend: "backend-example"
 *             backend-http-configuration: "backend-http-configuration-example"
 *         end
 *
 *         request-routing-rule
 *             name: "request-routing-rule-2-example"
 *             listener: "listener-example-2"
 *             redirect-configuration: "redirect-configuration-example"
 *         end
 *
 *         redirect-configuration
 *             name: "redirect-configuration-example"
 *             type: "Temporary"
 *             target-listener: "listener-example-3"
 *             include-query-string: true
 *             include-path: true
 *         end
 *
 *         listener
 *             name: "listener-example"
 *             port: 81
 *         end
 *
 *         listener
 *             name: "listener-example-2"
 *             port: 82
 *         end
 *
 *         listener
 *             name: "listener-example-3"
 *             port: 83
 *         end
 *
 *         backend
 *             name: "backend-example"
 *             ip-addresses: [
 *                 "10.0.0.2",
 *                 "10.0.0.3"
 *             ]
 *         end
 *
 *         backend-http-configuration
 *             name: "backend-http-configuration-example"
 *             port: 8080
 *             cookie-name: "something"
 *             enable-affinity-cookie: false
 *             probe: "probe-example"
 *             connection-draining-timeout: 30
 *             host-header: "something"
 *             host-header-from-backend: false
 *             backend-path: "something"
 *         end
 *
 *         probe
 *             name: "probe-example"
 *             host-name: "www.google.com"
 *             path: "/path"
 *             interval: 40
 *             timeout: 40
 *             unhealthy-threshold: 4
 *             https-protocol: false
 *             http-response-codes: [
 *                 "200-210"
 *             ]
 *             http-response-body-match: "body"
 *         end
 *
 *     end
 *
 * Certificate Commands
 * --------------------
 *
 * The following set of commands allow you to manage certificates in an application gateway. Before using these commands
 * you must have already created an ``azure::application-gateway``. The application gateway must be managed by Gyro. Ensure a proper
 * access policy is added to the key vault for the service principal you are using.
 *
 * **Add Certificate**
 *
 * Adds a certificate to an application gateway using your certificate file (.pfx).
 *
 * .. code::
 *
 *     gyro azure application-gateway add-certificate <application-gateway-name> <cert-name> <path> --password <password>
 *
 * - ``application-gateway-name`` - The name of the application gateway resource defined in your config where you want to create your certificate.
 * - ``cert-name`` - The name of the certificate that you want to create when you import the certificate file.
 * - ``cert-path`` - The path pointing to the certificate file to be uploaded. Only ``.pfx`` files are supported.
 * - ``password`` - An optional password if the certificate file was encrypted with one.
 *
 * **Import Certificate**
 *
 * Imports a certificate to an application gateway from your vault. For the import to work make sure the vault is in the soft delete phase and give appropriate access policy to a managed identity to the vault that you have also added to the application gateway.
 *
 * .. code::
 *
 *     gyro azure application-gateway import-certificate <application-gateway-name> <cert-name> <path> --password <password>
 *
 * - ``application-gateway-name`` - The name of the application gateway resource defined in your config where you want to import your certificate.
 * - ``cert-name`` - The name of the certificate that you want to create when you import the certificate.
 * - ``vault-name`` - The name of the key-vault resource defined in your config from which you want to import the certificate from.
 * - ``vault-cert-name`` - The name of the certificate in the vault that you want to import.
 *
 * **Remove Certificate**
 *
 * Remove a certificate from the application gateway.
 *
 * .. code::
 *
 *     gyro azure application-gateway remove-certificate <application-gateway-name> <cert-name>
 *
 * - ``application-gateway-name`` - The name of the application gateway resource defined in your config from which to remove the certificate.
 * - ``cert-name`` - The name of the certificate that you want to remove.
 *
 * **List Certificate**
 *
 * List certificates of an application gateway.
 *
 * .. code::
 *
 *     gyro azure vault list-certificate <application-gateway-name>
 *
 * - ``application-gateway-name`` - The name of the vault resource defined in your config that you want to list certificates from.
 *
 */
@Type("application-gateway")
public class ApplicationGatewayResource extends AzureResource implements Copyable<ApplicationGateway> {
    private ResourceGroupResource resourceGroup;
    private NetworkResource network;
    private PublicIpAddressResource publicIpAddress;
    private String subnet;
    private String name;
    private Set<RequestRoutingRule> requestRoutingRule;
    private Set<Listener> listener;
    private Set<Backend> backend;
    private Set<BackendHttpConfiguration> backendHttpConfiguration;
    private Set<RedirectConfiguration> redirectConfiguration;
    private Set<Probe> probe;
    private String skuSize;
    private String skuTier;
    private Integer instanceCount;
    private Map<String, String> tags;
    private Boolean enableHttp2;
    private Boolean privateFrontEnd;
    private Set<String> availabilityZones;
    private ApplicationGatewayManagedServiceIdentity managedServiceIdentity;

    private String id;

    /**
     * The resource group under which the Application Gateway would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The Network which would be associated with the Application Gateway.
     */
    @Required
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The Public IP Address associated with the Application Gateway.
     */
    @Required
    public PublicIpAddressResource getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(PublicIpAddressResource publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    /**
     * One of the subnet name from the assigned virtual network for the Application Gateway.
     */
    @Required
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Name of the Application Gateway.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Request routing rule for the Application Gateway.
     *
     * @subresource gyro.azure.network.RequestRoutingRule
     */
    @Required
    @Updatable
    public Set<RequestRoutingRule> getRequestRoutingRule() {
        if (requestRoutingRule == null) {
            requestRoutingRule = new HashSet<>();
        }

        return requestRoutingRule;
    }

    public void setRequestRoutingRule(Set<RequestRoutingRule> requestRoutingRule) {
        this.requestRoutingRule = requestRoutingRule;
    }

    /**
     * Listener for the Application Gateway.
     *
     * @subresource gyro.azure.network.Listener
     */
    @Required
    @Updatable
    public Set<Listener> getListener() {
        if (listener == null) {
            listener = new HashSet<>();
        }

        return listener;
    }

    public void setListener(Set<Listener> listener) {
        this.listener = listener;
    }

    /**
     * Backend for the Application Gateway. Required if no redirect configuration present.
     *
     * @subresource gyro.azure.network.Backend
     */
    @Updatable
    public Set<Backend> getBackend() {
        if (backend == null) {
            backend = new HashSet<>();
        }

        return backend;
    }

    public void setBackend(Set<Backend> backend) {
        this.backend = backend;
    }

    /**
     * Backend http configuration for the Application Gateway. Required if no redirect configuration present.
     *
     * @subresource gyro.azure.network.BackendHttpConfiguration
     */
    @Updatable
    public Set<BackendHttpConfiguration> getBackendHttpConfiguration() {
        if (backendHttpConfiguration == null) {
            backendHttpConfiguration = new HashSet<>();
        }

        return backendHttpConfiguration;
    }

    public void setBackendHttpConfiguration(Set<BackendHttpConfiguration> backendHttpConfiguration) {
        this.backendHttpConfiguration = backendHttpConfiguration;
    }

    /**
     * Redirect configuration for the Application Gateway. Required if no backend present.
     *
     * @subresource gyro.azure.network.RedirectConfiguration
     */
    @Updatable
    public Set<RedirectConfiguration> getRedirectConfiguration() {
        if (redirectConfiguration == null) {
            redirectConfiguration = new HashSet<>();
        }

        return redirectConfiguration;
    }

    public void setRedirectConfiguration(Set<RedirectConfiguration> redirectConfiguration) {
        this.redirectConfiguration = redirectConfiguration;
    }

    /**
     * Probe for the Application Gateway.
     *
     * @subresource gyro.azure.network.Probe
     */
    @Updatable
    public Set<Probe> getProbe() {
        if (probe == null) {
            probe = new HashSet<>();
        }

        return probe;
    }

    public void setProbe(Set<Probe> probe) {
        this.probe = probe;
    }

    /**
     * The SKU for the Application Gateway. Valid values are ``STANDARD_SMALL`` or ``STANDARD_MEDIUM`` or ``STANDARD_LARGE`` or ``WAF_MEDIUM`` or ``WAF_LARGE`` or ``STANDARD_V2`` or ``WAF_V2``.
     */
    @Required
    @ValidStrings({"STANDARD_SMALL", "STANDARD_MEDIUM", "STANDARD_LARGE", "WAF_MEDIUM", "WAF_LARGE", "STANDARD_V2", "WAF_V2"})
    @Updatable
    public String getSkuSize() {
        return skuSize != null ? skuSize.toUpperCase() : null;
    }

    public void setSkuSize(String skuSize) {
        this.skuSize = skuSize;
    }

    /**
     * The SKU for the Application Gateway. Valid Values are ``STANDARD``, ``STANDARD_V2``, ``WAF``, ``WAF_V2``.
     */
    @Required
    @ValidStrings({"STANDARD", "STANDARD_V2", "WAF", "WAF_V2"})
    @Updatable
    public String getSkuTier() {
        return skuTier != null
                ? skuTier.toUpperCase()
                : null;
    }

    public void setSkuTier(String skuTier) {
        this.skuTier = skuTier;
    }

    /**
     * Number of instances to scale for the Application Gateway.
     */
    @Required
    @Updatable
    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    /**
     * Tags for the Application Gateway.
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
     * Enable http2 for the Application Gateway. Defaults to false.
     */
    @Updatable
    public Boolean getEnableHttp2() {
        if (enableHttp2 == null) {
            enableHttp2 = false;
        }

        return enableHttp2;
    }

    public void setEnableHttp2(Boolean enableHttp2) {
        this.enableHttp2 = enableHttp2;
    }

    /**
     * Private front end for the Application Gateway. Defaults to false.
     */
    public Boolean getPrivateFrontEnd() {
        if (privateFrontEnd == null) {
            privateFrontEnd = false;
        }

        return privateFrontEnd;
    }

    public void setPrivateFrontEnd(Boolean privateFrontEnd) {
        this.privateFrontEnd = privateFrontEnd;
    }

    /**
     * Availability Zones this Application Gateway should be deployed to redundancy.
     * Valid values are ``1``, ``2``, ``3``.
     */
    @ValidStrings({"1", "2", "3"})
    public Set<String> getAvailabilityZones() {
        return availabilityZones == null
                ? availabilityZones = new HashSet<>()
                : availabilityZones;
    }

    public void setAvailabilityZones(Set<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The managed service identity configuration for the application gateway.
     *
     * @subresource gyro.azure.network.ApplicationGatewayManagedServiceIdentity
     */
    @Updatable
    public ApplicationGatewayManagedServiceIdentity getManagedServiceIdentity() {
        return managedServiceIdentity;
    }

    public void setManagedServiceIdentity(ApplicationGatewayManagedServiceIdentity managedServiceIdentity) {
        this.managedServiceIdentity = managedServiceIdentity;
    }

    /**
     * The ID of the application gateway.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Integer> backendHealth() {
        Map<String, Integer> healthMap = new HashMap<>();
        int total = 0;

        Azure client = createClient();
        ApplicationGateway applicationGateway = client.applicationGateways().getById(getId());

        if (applicationGateway != null) {
            Map<String, ApplicationGatewayBackendHealth> backendHealthMap = applicationGateway.checkBackendHealth();
            for (Map.Entry<String, ApplicationGatewayBackendHealth> backendHealthMapEntry : backendHealthMap.entrySet()) {
                ApplicationGatewayBackendHealth backendHealth = backendHealthMapEntry.getValue();

                Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpHealthMap = backendHealth.httpConfigurationHealths();
                for (Map.Entry<String, ApplicationGatewayBackendHttpConfigurationHealth> httpHealthMapEntry : httpHealthMap.entrySet()) {
                    ApplicationGatewayBackendHttpConfigurationHealth httpHealth = httpHealthMapEntry.getValue();

                    Map<String, ApplicationGatewayBackendServerHealth> serverHealthMap = httpHealth.serverHealths();
                    for (Map.Entry<String, ApplicationGatewayBackendServerHealth> serverHealthMapEntry : serverHealthMap.entrySet()) {
                        ApplicationGatewayBackendServerHealth serverHealth = serverHealthMapEntry.getValue();
                        ApplicationGatewayBackendHealthStatus healthStatus = serverHealth.status();
                        if (healthStatus != null) {
                            int count = healthMap.getOrDefault(healthStatus.toString(), 0);
                            healthMap.put(healthStatus.toString(), count + 1);
                        }
                        total++;
                    }
                }
            }
        }

        healthMap.put("Total", total);
        return healthMap;
    }

    @Override
    public void copyFrom(ApplicationGateway applicationGateway) {
        setId(applicationGateway.id());
        setInstanceCount(applicationGateway.instanceCount());
        setEnableHttp2(applicationGateway.isHttp2Enabled());
        setSkuSize(applicationGateway.sku().name().toString());
        setSkuTier(applicationGateway.tier().toString());
        setPrivateFrontEnd(applicationGateway.isPrivate());
        setTags(applicationGateway.tags());
        setName(applicationGateway.name());
        setResourceGroup(findById(ResourceGroupResource.class, applicationGateway.resourceGroupName()));

        getAvailabilityZones().clear();
        if (applicationGateway.inner() != null
                && applicationGateway.inner().zones() != null) {
            for (String availabilityZone : applicationGateway.inner().zones()) {
                getAvailabilityZones().add(availabilityZone);
            }
        }

        getBackend().clear();
        for (ApplicationGatewayBackend applicationGatewayBackend : applicationGateway.backends().values()) {
            Backend backend = newSubresource(Backend.class);
            backend.copyFrom(applicationGatewayBackend);
            getBackend().add(backend);
        }

        getListener().clear();
        for (ApplicationGatewayListener applicationGatewayListener : applicationGateway.listeners().values()) {
            Listener listener = newSubresource(Listener.class);
            listener.copyFrom(applicationGatewayListener);
            getListener().add(listener);
        }

        getBackendHttpConfiguration().clear();
        for (ApplicationGatewayBackendHttpConfiguration backendHttpConfig : applicationGateway.backendHttpConfigurations().values()) {
            BackendHttpConfiguration backendHttpConfiguration = newSubresource(BackendHttpConfiguration.class);
            backendHttpConfiguration.copyFrom(backendHttpConfig);
            getBackendHttpConfiguration().add(backendHttpConfiguration);
        }

        getRedirectConfiguration().clear();
        for (ApplicationGatewayRedirectConfiguration applicationGatewayRedirectConfig: applicationGateway.redirectConfigurations().values()) {
            RedirectConfiguration redirectConfiguration = newSubresource(RedirectConfiguration.class);
            redirectConfiguration.copyFrom(applicationGatewayRedirectConfig);
            getRedirectConfiguration().add(redirectConfiguration);
        }

        getProbe().clear();
        for (ApplicationGatewayProbe applicationGatewayProbe : applicationGateway.probes().values()) {
            Probe probe = newSubresource(Probe.class);
            probe.copyFrom(applicationGatewayProbe);
            getProbe().add(probe);
        }

        getRequestRoutingRule().clear();
        for (ApplicationGatewayRequestRoutingRule applicationGatewayRequestRoutingRule: applicationGateway.requestRoutingRules().values()) {
            if (applicationGatewayRequestRoutingRule.ruleType().equals(ApplicationGatewayRequestRoutingRuleType.BASIC)) {
                RequestRoutingRule requestRoutingRule = newSubresource(RequestRoutingRule.class);
                requestRoutingRule.copyFrom(applicationGatewayRequestRoutingRule);
                getRequestRoutingRule().add(requestRoutingRule);
            }
        }

        setManagedServiceIdentity(null);
        if (applicationGateway.inner().identity() != null) {
            ApplicationGatewayManagedServiceIdentity identity = newSubresource(ApplicationGatewayManagedServiceIdentity.class);
            identity.copyFrom(applicationGateway.inner().identity());
            setManagedServiceIdentity(identity);
        }
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        ApplicationGateway applicationGateway = client.applicationGateways().getById(getId());

        if (applicationGateway == null) {
            return false;
        }

        copyFrom(applicationGateway);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        ApplicationGateway.DefinitionStages.WithRequestRoutingRule withRequestRoutingRule = client.applicationGateways()
            .define(getName()).withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate attach = null;

        for (RequestRoutingRule requestRoutingRule : getRequestRoutingRule()) {
            attach = requestRoutingRule.createRequestRoutingRule(withRequestRoutingRule, attach);
        }

        ApplicationGateway.DefinitionStages.WithCreate withCreate;
        if (getEnableHttp2()) {
            withCreate = attach.withHttp2();
        } else {
            withCreate = attach.withoutHttp2();
        }

        for (Listener listener : getListener()) {
            withCreate = listener.createListener(withCreate);
        }

        for (Backend backend : getBackend()) {
            withCreate = backend.createBackend(withCreate);
        }

        for (BackendHttpConfiguration backendHttpConfiguration : getBackendHttpConfiguration()) {
            withCreate = backendHttpConfiguration.createBackendHttpConfiguration(withCreate);
        }

        for (RedirectConfiguration redirectConfiguration: getRedirectConfiguration()) {
            withCreate = redirectConfiguration.createRedirectConfiguration(withCreate);
        }

        for (Probe probe: getProbe()) {
            withCreate = probe.createProbe(withCreate);
        }

        for (String availabiltyZone : getAvailabilityZones()) {
            AvailabilityZoneId availabilityZoneId = AvailabilityZoneId.fromString(availabiltyZone);
            if (availabilityZoneId != null) {
                withCreate.withAvailabilityZone(availabilityZoneId);
            }
        }

        if (getManagedServiceIdentity() != null) {
            withCreate.withIdentity(getManagedServiceIdentity().toManagedServiceIdentity());
        }

        ApplicationGateway applicationGateway = withCreate.withExistingPublicIPAddress(
            client.publicIPAddresses().getById(getPublicIpAddress().getId())
        )
            .withInstanceCount(getInstanceCount())
            .withSize(ApplicationGatewaySkuName.fromString(getSkuSize()))
            .withTier(ApplicationGatewayTier.fromString(getSkuTier()))
            .withTags(getTags())
            .withExistingSubnet(getNetwork().getId(), getSubnet())
            .create();

        copyFrom(applicationGateway);
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, Set<String> changedFieldNames) {
        Azure client = createClient();

        ApplicationGateway applicationGateway = client.applicationGateways().getById(getId());

        ApplicationGateway.Update update = applicationGateway.update()
                .withSize(ApplicationGatewaySkuName.fromString(getSkuSize()))
                .withTier(ApplicationGatewayTier.fromString(getSkuTier()));

        ApplicationGatewayResource oldApplicationGatewayResource = (ApplicationGatewayResource) resource;

        update = saveTags(oldApplicationGatewayResource.getTags(), update);

        update = saveListener(oldApplicationGatewayResource.getListener(), update);

        update = saveRedirectConfiguration(oldApplicationGatewayResource.getRedirectConfiguration(), update);

        update = saveBackendHttpConfiguration(oldApplicationGatewayResource.getBackendHttpConfiguration(), update);

        update = saveProbe(oldApplicationGatewayResource.getProbe(), update);

        update = saveBackend(oldApplicationGatewayResource.getBackend(), update);

        update = saveRequestRoutingRule(oldApplicationGatewayResource.getRequestRoutingRule(), update);


        if (changedFieldNames.contains("managed-service-identity")) {
            if (getManagedServiceIdentity() == null) {
                throw new GyroException("Cannot unset 'managed-service-identity'.");
            }

            update = update.withIdentity(getManagedServiceIdentity().toManagedServiceIdentity());
        }

        applicationGateway = update.apply();

        copyFrom(applicationGateway);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.applicationGateways().deleteById(getId());
    }

    private Update saveRequestRoutingRule(Set<RequestRoutingRule> oldRequestRoutingRules, Update update) {
        Set<String> requestRoutingRuleNames = getRequestRoutingRule().stream()
            .map(RequestRoutingRule::getName).collect(Collectors.toSet());

        List<String> requestRoutingRuleDeleteList = oldRequestRoutingRules.stream()
            .filter(o -> !requestRoutingRuleNames.contains(o.getName()))
            .map(RequestRoutingRule::getName)
            .collect(Collectors.toList());

        for (String requestRoutingRuleName : requestRoutingRuleDeleteList) {
            update = update.withoutRequestRoutingRule(requestRoutingRuleName);
        }

        Set<String> oldRequestRoutingRuleNames = oldRequestRoutingRules.stream()
            .map(RequestRoutingRule::getName).collect(Collectors.toSet());

        List<RequestRoutingRule> requestRoutingRuleModificationList = getRequestRoutingRule().stream()
            .filter(o -> oldRequestRoutingRuleNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (RequestRoutingRule requestRoutingRule : requestRoutingRuleModificationList) {
            update = requestRoutingRule.updateRequestRoutingRule(update);
        }

        List<RequestRoutingRule> requestRoutingRuleAdditionList = getRequestRoutingRule().stream()
            .filter(o -> !oldRequestRoutingRuleNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (RequestRoutingRule requestRoutingRule : requestRoutingRuleAdditionList) {
            update = requestRoutingRule.createRequestRoutingRule(update);
        }

        return update;
    }

    private Update saveListener(Set<Listener> oldListeners, Update update) {
        Set<String> listenerNames = getListener().stream()
            .map(Listener::getName).collect(Collectors.toSet());

        List<String> listenerDeleteList = oldListeners.stream()
            .filter(o -> !listenerNames.contains(o.getName()))
            .map(Listener::getName)
            .collect(Collectors.toList());

        for (String listenerName : listenerDeleteList) {
            update = update.withoutListener(listenerName);
        }

        Set<String> oldListenerNames = oldListeners.stream()
            .map(Listener::getName).collect(Collectors.toSet());

        List<Listener> listenerModificationList = getListener().stream()
            .filter(o -> oldListenerNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (Listener listener : listenerModificationList) {
            update = listener.updateListener(update);
        }

        List<Listener> listenerAdditionList = getListener().stream()
            .filter(o -> !oldListenerNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (Listener listener : listenerAdditionList) {
            update = listener.createListener(update);
        }

        return update;
    }

    private Update saveRedirectConfiguration(Set<RedirectConfiguration> oldRedirectConfigurations, Update update) {
        Set<String> redirectConfigurationNames = getRedirectConfiguration().stream()
            .map(RedirectConfiguration::getName).collect(Collectors.toSet());

        List<String> redirectConfigurationDeleteList = oldRedirectConfigurations.stream()
            .filter(o -> !redirectConfigurationNames.contains(o.getName()))
            .map(RedirectConfiguration::getName)
            .collect(Collectors.toList());

        for (String redirectConfigurationName : redirectConfigurationDeleteList) {
            update = update.withoutRedirectConfiguration(redirectConfigurationName);
        }

        Set<String> oldRedirectConfigurationNames = oldRedirectConfigurations.stream()
            .map(RedirectConfiguration::getName).collect(Collectors.toSet());

        List<RedirectConfiguration> redirectConfigurationModificationList = getRedirectConfiguration().stream()
            .filter(o -> oldRedirectConfigurationNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (RedirectConfiguration redirectConfiguration : redirectConfigurationModificationList) {
            update = redirectConfiguration.updateRedirectConfiguration(update);
        }

        List<RedirectConfiguration> redirectConfigurationAdditionList = getRedirectConfiguration().stream()
            .filter(o -> !oldRedirectConfigurationNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (RedirectConfiguration redirectConfiguration : redirectConfigurationAdditionList) {
            update = redirectConfiguration.createRedirectConfiguration(update);
        }

        return update;
    }

    private Update saveBackendHttpConfiguration(Set<BackendHttpConfiguration> oldBackendHttpConfigurations, Update update) {
        Set<String> backendHttpConfigurationNames = getBackendHttpConfiguration().stream()
            .map(BackendHttpConfiguration::getName).collect(Collectors.toSet());

        List<String> backendHttpConfigurationDeleteList = oldBackendHttpConfigurations.stream()
            .filter(o -> !backendHttpConfigurationNames.contains(o.getName()))
            .map(BackendHttpConfiguration::getName)
            .collect(Collectors.toList());

        for (String backendHttpConfigurationName : backendHttpConfigurationDeleteList) {
            update = update.withoutBackendHttpConfiguration(backendHttpConfigurationName);
        }

        Set<String> oldBackendHttpConfigurationNames = oldBackendHttpConfigurations.stream()
            .map(BackendHttpConfiguration::getName).collect(Collectors.toSet());

        List<BackendHttpConfiguration> backendHttpConfigurationModificationList = getBackendHttpConfiguration().stream()
            .filter(o -> oldBackendHttpConfigurationNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (BackendHttpConfiguration backendHttpConfiguration : backendHttpConfigurationModificationList) {
            update = backendHttpConfiguration.updateBackendHttpConfiguration(update);
        }

        List<BackendHttpConfiguration> backendHttpConfigurationAdditionList = getBackendHttpConfiguration().stream()
            .filter(o -> !oldBackendHttpConfigurationNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (BackendHttpConfiguration backendHttpConfiguration : backendHttpConfigurationAdditionList) {
            update = backendHttpConfiguration.createBackendHttpConfiguration(update);
        }

        return update;
    }

    private Update saveProbe(Set<Probe> oldProbes, Update update) {
        Set<String> probeNames = getProbe().stream()
            .map(Probe::getName).collect(Collectors.toSet());

        List<String> probeDeleteList = oldProbes.stream()
            .filter(o -> !probeNames.contains(o.getName()))
            .map(Probe::getName)
            .collect(Collectors.toList());

        for (String probeName : probeDeleteList) {
            update = update.withoutProbe(probeName);
        }

        Set<String> oldProbeNames = oldProbes.stream()
            .map(Probe::getName).collect(Collectors.toSet());

        List<Probe> probeModificationList = getProbe().stream()
            .filter(o -> oldProbeNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (Probe probe : probeModificationList) {
            update = probe.updateProbe(update);
        }

        List<Probe> probeAdditionList = getProbe().stream()
            .filter(o -> !oldProbeNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (Probe probe : probeAdditionList) {
            update = probe.createProbe(update);
        }

        return update;
    }

    private Update saveBackend(Set<Backend> oldBackends, Update update) {
        Set<String> backendNames = getBackend().stream()
            .map(Backend::getName).collect(Collectors.toSet());

        List<String> backendDeleteList = oldBackends.stream()
            .filter(o -> !backendNames.contains(o.getName()))
            .map(Backend::getName)
            .collect(Collectors.toList());

        for (String backendName : backendDeleteList) {
            update = update.withoutBackend(backendName);
        }

        Set<String> oldBackendNames = oldBackends.stream()
            .map(Backend::getName).collect(Collectors.toSet());

        Map<String, Backend> oldBackendMap = oldBackends.stream()
            .collect(Collectors.toMap(Backend::getName, o -> o));

        List<Backend> backendModificationList = getBackend().stream()
            .filter(o -> oldBackendNames.contains(o.getName()))
            .collect(Collectors.toList());

        for (Backend backend : backendModificationList) {
            Backend oldBackend = oldBackendMap.get(backend.getName());
            update = backend.updateBackend(update, oldBackend.getIpAddresses(), oldBackend.getFqdns());
        }

        List<Backend> backendAdditionList = getBackend().stream()
            .filter(o -> !oldBackendNames.contains(o.getName()))
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
