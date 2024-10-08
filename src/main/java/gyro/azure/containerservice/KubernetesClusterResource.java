/*
 * Copyright 2022, Brightspot, Inc.
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

package gyro.azure.containerservice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubeletDiskType;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster.DefinitionStages.WithAgentPool;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster.DefinitionStages.WithCreate;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster.DefinitionStages.WithServicePrincipalClientId;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool.DefinitionStages.Blank;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool.DefinitionStages.WithAttach;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.containerservice.models.LoadBalancerSku;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAddonProfile;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.containerservice.models.NetworkPolicy;
import com.azure.resourcemanager.containerservice.models.OSDiskType;
import com.azure.resourcemanager.containerservice.models.OSType;
import com.azure.resourcemanager.containerservice.models.ScaleSetEvictionPolicy;
import com.azure.resourcemanager.containerservice.models.ScaleSetPriority;
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
import gyro.core.validation.CollectionMin;
import gyro.core.validation.Required;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates a Kubernetes Cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::kubernetes-cluster kubernetes-cluster-example
 *         name: "kubernetes-cluster-example"
 *         version: "1.22.4"
 *         enable-private-cluster: false
 *
 *         resource-group: $(azure::resource-group resource-group-cluster-example)
 *
 *         linux-root-username: "adminuser"
 *
 *         dns-prefix: "kubernetes-cluster-example-dns"
 *         enable-rbac: true
 *
 *         agent-pool
 *             name: "agentpool"
 *             size: "Standard_DS2_v2"
 *             count: 1
 *             availability-zones: [1,2,3]
 *             mode: "System"
 *             auto-scaling-enabled: true
 *             type: "VirtualMachineScaleSets"
 *             os-type: "Linux"
 *             os-disk-type: "Manged"
 *             os-disk-size-in-gb: 128
 *             node-size: 1
 *             network: $(azure::network network-cluster-example)
 *             subnet: "subnet1"
 *             maximum-pods-per-node: 110
 *             minimum-node-size: 1
 *             maximum-node-size: 5
 *             kubelet-disk-type: "OS"
 *
 *             tags: {
 *                 Name: "agentpool_primary"
 *             }
 *         end
 *
 *         network-profile
 *             dns-service-ip: "10.0.0.10"
 *             service-cidr: "10.0.0.0/16"
 *             load-balancer-sku: "Standard"
 *             outbound-type: "loadBalancer"
 *
 *             load-balancer-profile
 *                 outbound-ips
 *                     public-ips: $(azure::public-ip-address public-ip-address-example-cluster)
 *                 end
 *
 *             end
 *         end
 *
 *         tags: {
 *             Name: "kubernetes-cluster-example"
 *         }
 *
 *     end
 *
 */
@Type("kubernetes-cluster")
public class KubernetesClusterResource extends AzureResource implements Copyable<KubernetesCluster> {

    private String name;
    private String version;
    private Set<ClusterAddonProfile> addonProfile;
    private Set<ClusterAgentPool> agentPool;
    private NetworkProfile networkProfile;
    private String dnsPrefix;
    private Boolean enableRbac;
    private String fqdn;
    private String linuxRootUsername;
    private String nodeResourceGroup;
    private String powerState;
    private String provisioningState;
    private String servicePrincipalClientId;
    private String servicePrincipalSecret;
    private String sshKey;
    private String systemAssignedManagedServiceIdentityPrincipalId;
    private String id;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;
    private Boolean enablePrivateCluster;
    private ClusterPropertiesAutoScalerProfile autoScalerProfile;
    private ApiServerAccessProfile apiServerAccessProfile;

    /**
     * Name of the cluster.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Version of the AKS cluster to use.
     */
    @Updatable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Addon profile configuration.
     *
     * @subresource gyro.azure.containerservice.ClusterAddonProfile
     */
    @Updatable
    public Set<ClusterAddonProfile> getAddonProfile() {
        if (addonProfile == null) {
            addonProfile = new HashSet<>();
        }

        return addonProfile;
    }

    public void setAddonProfile(Set<ClusterAddonProfile> addonProfile) {
        this.addonProfile = addonProfile;
    }

    /**
     * Agent pool configuration.
     *
     * @subresource gyro.azure.containerservice.ClusterAgentPool
     */
    @Required
    @Updatable
    @CollectionMin(1)
    public Set<ClusterAgentPool> getAgentPool() {
        if (agentPool == null) {
            agentPool = new HashSet<>();
        }

        return agentPool;
    }

    public void setAgentPool(Set<ClusterAgentPool> agentPool) {
        this.agentPool = agentPool;
    }

    /**
     * Network Profile configuration.
     *
     * @subresource gyro.azure.containerservice.NetworkProfile
     */
    @Updatable
    public NetworkProfile getNetworkProfile() {
        return networkProfile;
    }

    public void setNetworkProfile(NetworkProfile networkProfile) {
        this.networkProfile = networkProfile;
    }

    /**
     * The dns prefix for the cluster.
     */
    public String getDnsPrefix() {
        return dnsPrefix;
    }

    public void setDnsPrefix(String dnsPrefix) {
        this.dnsPrefix = dnsPrefix;
    }

    /**
     * When set to ``true`` enables rbac for the cluster. Defaults to ``true``.
     */
    @Updatable
    public Boolean getEnableRbac() {
        if (enableRbac == null) {
            enableRbac = true;
        }

        return enableRbac;
    }

    public void setEnableRbac(Boolean enableRbac) {
        this.enableRbac = enableRbac;
    }

    /**
     * The fqdn for the cluster.
     */
    @Output
    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    /**
     * The root user name.
     */
    @Required
    public String getLinuxRootUsername() {
        return linuxRootUsername;
    }

    public void setLinuxRootUsername(String linuxRootUsername) {
        this.linuxRootUsername = linuxRootUsername;
    }

    public String getNodeResourceGroup() {
        return nodeResourceGroup;
    }

    public void setNodeResourceGroup(String nodeResourceGroup) {
        this.nodeResourceGroup = nodeResourceGroup;
    }

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * The service principal client id for the cluster.
     */
    public String getServicePrincipalClientId() {
        return servicePrincipalClientId;
    }

    public void setServicePrincipalClientId(String servicePrincipalClientId) {
        this.servicePrincipalClientId = servicePrincipalClientId;
    }

    /**
     * The service principal secret for the cluster.
     */
    public String getServicePrincipalSecret() {
        return servicePrincipalSecret;
    }

    public void setServicePrincipalSecret(String servicePrincipalSecret) {
        this.servicePrincipalSecret = servicePrincipalSecret;
    }

    /**
     * The ssh key for the cluster.
     */
    @Required
    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    /**
     * The system assigned service principal id for the cluster.
     */
    @Output
    public String getSystemAssignedManagedServiceIdentityPrincipalId() {
        return systemAssignedManagedServiceIdentityPrincipalId;
    }

    public void setSystemAssignedManagedServiceIdentityPrincipalId(String systemAssignedManagedServiceIdentityPrincipalId) {
        this.systemAssignedManagedServiceIdentityPrincipalId = systemAssignedManagedServiceIdentityPrincipalId;
    }

    /**
     * The id of the cluster.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The resource group where the cluster will belong.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The tags for the cluster.
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
     * If set to ``true`` makes the cluster private. Defaults to ``false``.
     */
    public Boolean getEnablePrivateCluster() {
        if (enablePrivateCluster == null) {
            enablePrivateCluster = false;
        }

        return enablePrivateCluster;
    }

    public void setEnablePrivateCluster(Boolean enablePrivateCluster) {
        this.enablePrivateCluster = enablePrivateCluster;
    }

    /**
     * Autoscaler profile config.
     *
     * @subresource gyro.azure.containerservice.ClusterPropertiesAutoScalerProfile
     */
    @Updatable
    public ClusterPropertiesAutoScalerProfile getAutoScalerProfile() {
        return autoScalerProfile;
    }

    public void setAutoScalerProfile(ClusterPropertiesAutoScalerProfile autoScalerProfile) {
        this.autoScalerProfile = autoScalerProfile;
    }

    /**
     * Api server access profile config.
     *
     * @subresource gyro.azure.containerservice.ApiServerAccessProfile
     */
    @Updatable
    public ApiServerAccessProfile getApiServerAccessProfile() {
        return apiServerAccessProfile;
    }

    public void setApiServerAccessProfile(ApiServerAccessProfile apiServerAccessProfile) {
        this.apiServerAccessProfile = apiServerAccessProfile;
    }

    @Override
    public void copyFrom(KubernetesCluster cluster) {
        setName(cluster.name());
        setVersion(cluster.version());
        setDnsPrefix(cluster.dnsPrefix());
        setEnableRbac(cluster.enableRBAC());
        setFqdn(cluster.fqdn());
        setLinuxRootUsername(cluster.linuxRootUsername());
        setNodeResourceGroup(cluster.nodeResourceGroup());
        setPowerState(cluster.powerState().toString());
        setProvisioningState(cluster.provisioningState());
        setServicePrincipalClientId(cluster.servicePrincipalClientId());
        setServicePrincipalSecret(cluster.servicePrincipalSecret());
        setSshKey(cluster.sshKey());
        setSystemAssignedManagedServiceIdentityPrincipalId(cluster.systemAssignedManagedServiceIdentityPrincipalId());
        setId(cluster.id());
        setResourceGroup(findById(ResourceGroupResource.class, cluster.resourceGroupName()));
        setTags(cluster.tags());

        Set<ClusterAddonProfile> addonProfiles = new HashSet<>();
        try {
            if (cluster.addonProfiles() != null) {
                ClusterAddonProfile addonProfile;
                for (ManagedClusterAddonProfile addon : cluster.addonProfiles().values()) {
                    addonProfile = newSubresource(ClusterAddonProfile.class);
                    addonProfile.copyFrom(addon);
                    addonProfiles.add(addonProfile);
                }
            }
        } catch (NullPointerException ex) {
            // ignore
            // TODO cluster.addonProfiles() throwing npe
        }
        setAddonProfile(addonProfiles);

        Set<ClusterAgentPool> agentPools = new HashSet<>();
        if (cluster.agentPools() != null) {
            ClusterAgentPool agentPool;
            for (KubernetesClusterAgentPool agent : cluster.agentPools().values()) {
                agentPool = newSubresource(ClusterAgentPool.class);
                agentPool.copyFrom(agent);
                agentPools.add(agentPool);
            }
        }
        setAgentPool(agentPools);

        NetworkProfile networkProfile = null;
        if (cluster.networkProfile() != null) {
            networkProfile = newSubresource(NetworkProfile.class);
            networkProfile.copyFrom(cluster.networkProfile());
        }
        setNetworkProfile(networkProfile);

        ClusterPropertiesAutoScalerProfile autoScalerProfile = null;
        if (cluster.innerModel().autoScalerProfile() != null) {
            autoScalerProfile = newSubresource(ClusterPropertiesAutoScalerProfile.class);
            autoScalerProfile.copyFrom(cluster.innerModel().autoScalerProfile());
        }
        setAutoScalerProfile(autoScalerProfile);

        ApiServerAccessProfile apiServerAccessProfile = null;
        if (cluster.innerModel().apiServerAccessProfile() != null) {
            apiServerAccessProfile = newSubresource(ApiServerAccessProfile.class);
            apiServerAccessProfile.copyFrom(cluster.innerModel().apiServerAccessProfile());
        }
        setApiServerAccessProfile(apiServerAccessProfile);
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);
        KubernetesClusters kubernetesClusters = client.kubernetesClusters();
        KubernetesCluster cluster = kubernetesClusters.list().stream()
            .filter(o -> o.name().equals(getName()))
            .filter(o -> o.resourceGroupName().equals(getResourceGroup().getName()))
            .findFirst().orElse(null);

        if (cluster != null) {
            copyFrom(cluster);

            return true;
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        WithServicePrincipalClientId withServicePrincipalClientId = client.kubernetesClusters()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withVersion(getVersion())
            .withRootUsername(getLinuxRootUsername())
            .withSshKey(getSshKey());

        WithAgentPool withAgentPool = null;
        WithCreate withCreate = null;
        if (StringUtils.isBlank(getServicePrincipalClientId())) {
            withCreate = withServicePrincipalClientId.withSystemAssignedManagedServiceIdentity();
        } else {
            withAgentPool = withServicePrincipalClientId
                .withServicePrincipalClientId(getServicePrincipalClientId())
                .withServicePrincipalSecret(getServicePrincipalSecret());
        }

        Blank<? extends WithCreate> createStage;
        for (ClusterAgentPool agentPool : getAgentPool()) {
            if (withCreate == null) {
                createStage = withAgentPool.defineAgentPool(agentPool.getName());
            } else {
                createStage = withCreate.defineAgentPool(agentPool.getName());
            }

            WithAttach<? extends WithCreate> withAttach =   createStage
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.fromString(agentPool.getSize()))
                .withAgentPoolVirtualMachineCount(agentPool.getMinimumNodeSize())

                .withTags(agentPool.getTags())
                .withAgentPoolMode(AgentPoolMode.fromString(agentPool.getMode()))
                .withAgentPoolType(AgentPoolType.fromString(agentPool.getType()))
                .withAvailabilityZones(agentPool.getAvailabilityZones().toArray(Integer[]::new))
                .withKubeletDiskType(KubeletDiskType.fromString(agentPool.getKubeletDiskType()))
                .withNodeLabels(agentPool.getNodeLabels())
                .withNodeTaints(agentPool.getNodeTaints())
                .withOSType(OSType.fromString(agentPool.getOsType()))
                .withOSDiskType(OSDiskType.fromString(agentPool.getOsDiskType()))
                .withOSDiskSizeInGB(agentPool.getOsDiskSizeInGb())
                .withVirtualNetwork(agentPool.getNetwork().getId(), agentPool.getSubnet())
                .withMaxPodsCount(agentPool.getMaximumPodsPerNode());

            if (agentPool.getAutoScalingEnabled()) {
                withAttach = withAttach.withAutoScaling(agentPool.getMinimumNodeSize(),
                    agentPool.getMaximumNodeSize());
            }

            if (!StringUtils.isBlank(agentPool.getVirtualMachineEvictionPolicy())) {
                withCreate = withAttach.withSpotPriorityVirtualMachine()
                    .withSpotPriorityVirtualMachine(ScaleSetEvictionPolicy.fromString(agentPool.getVirtualMachineEvictionPolicy()))
                    .withVirtualMachineMaximumPrice(agentPool.getVirtualMachineMaximumPrice())
                    .withVirtualMachinePriority(ScaleSetPriority.fromString(agentPool.getVirtualMachinePriority()))
                    .attach();
            } else {
                withCreate = withAttach.attach();
            }
        }

        if (getNetworkProfile() != null) {
            NetworkProfile network = getNetworkProfile();
            KubernetesCluster.DefinitionStages.NetworkProfileDefinitionStages.WithAttach<WithCreate> withAttach = withCreate.defineNetworkProfile()
                .withNetworkPlugin(NetworkPlugin.fromString(network.getNetworkPlugin()));

            if (!StringUtils.isBlank(network.getNetworkPolicy())) {
                withAttach = withAttach.withNetworkPolicy(NetworkPolicy.fromString(network.getNetworkPolicy()));
            }

            if (!StringUtils.isBlank(network.getPodCidr())) {
                withAttach = withAttach.withPodCidr(network.getPodCidr());
            }

            if (!StringUtils.isBlank(network.getServiceCidr())) {
                withAttach = withAttach.withServiceCidr(network.getServiceCidr());
            }

            if (!StringUtils.isBlank(network.getLoadBalancerSku())) {
                withAttach = withAttach.withLoadBalancerSku(LoadBalancerSku.fromString(network.getLoadBalancerSku()));
            }

            withCreate = withAttach.attach();
        }

        if (!getTags().isEmpty()) {
            withCreate = withCreate.withTags(getTags());
        }

        if (getEnablePrivateCluster()) {
            withCreate = withCreate.enablePrivateCluster();
        }

        if (!getAddonProfile().isEmpty()) {
            withCreate = withCreate.withAddOnProfiles(getAddonProfile()
                .stream()
                .collect(Collectors.toMap(o -> o.getIdentity().getId(),
                    ClusterAddonProfile::toAddonProfile)));
        }

        if (getAutoScalerProfile() != null) {
            withCreate = withCreate.withAutoScalerProfile(getAutoScalerProfile().toAutoScalerProfile());
        }

        if (!StringUtils.isBlank(getDnsPrefix())) {
            withCreate = withCreate.withDnsPrefix(getDnsPrefix());
        }

        KubernetesCluster cluster = withCreate.create();

        setId(cluster.id());
        state.save();

        KubernetesCluster.Update update;
        if (getApiServerAccessProfile() != null) {
            cluster.innerModel()
                .withApiServerAccessProfile(getApiServerAccessProfile().toManagedClusterApiServerAccessProfile());
        }

        update = cluster.update();

        if (getNetworkProfile() != null) {
            NetworkProfile networkProfile = getNetworkProfile();
            setNetworkProfile(null);
            setNetworkProfile(networkProfile);
            update = update.withNetworkProfile(getNetworkProfile().toNetworkProfile());
        }

        if (getEnableRbac()) {
            update = update.withRBACEnabled();
        } else {
            update = update.withRBACDisabled();
        }

        update.apply();

        copyFrom(cluster);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        AzureResourceManager client = createClient(AzureResourceManager.class);

        KubernetesCluster cluster = client.kubernetesClusters()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        KubernetesCluster.Update update = null;
        if (changedFieldNames.contains("api-server-access-profile")) {
            if (getApiServerAccessProfile() != null) {
                cluster.innerModel()
                    .withApiServerAccessProfile(getApiServerAccessProfile().toManagedClusterApiServerAccessProfile());
            } else {
                if (getEnablePrivateCluster()) {
                    cluster.innerModel()
                        .withApiServerAccessProfile(ApiServerAccessProfile.defaultPrivate());
                } else {
                    cluster.innerModel()
                        .withApiServerAccessProfile(ApiServerAccessProfile.defaultPublic());
                }
            }
        }

        update = cluster.update();

        if (changedFieldNames.contains("version")) {
            update = update.withVersion(getVersion());
        }

        if (changedFieldNames.contains("enable-rbac")) {
            if (getEnableRbac()) {
                update = update.withRBACEnabled();
            } else {
                update = update.withRBACDisabled();
            }
        }

        if (changedFieldNames.contains("addon-profile")) {
            update = update.withAddOnProfiles(getAddonProfile()
                .stream()
                .collect(Collectors.toMap(o -> o.getIdentity().getId(),
                    ClusterAddonProfile::toAddonProfile)));
        }

        if (changedFieldNames.contains("network-profile")) {
            update = update.withNetworkProfile(getNetworkProfile().toNetworkProfile());
        }

        if (changedFieldNames.contains("auto-scaler-profile")) {
            update = update.withAutoScalerProfile(getAutoScalerProfile().toAutoScalerProfile());
        }

        if (changedFieldNames.contains("tags")) {
            update = update.withTags(getTags());
        }

        if (changedFieldNames.contains("agent-pool")) {
            KubernetesClusterResource currentClusterResource = (KubernetesClusterResource) current;

            Set<ClusterAgentPool> currentAgentPool = currentClusterResource.getAgentPool();
            Set<ClusterAgentPool> pendingAgentPool = getAgentPool();

            Set<String> currentAgentPoolNames = currentAgentPool.stream().map(ClusterAgentPool::getName).collect(Collectors.toSet());
            Set<String> pendingAgentPoolNames = currentAgentPool.stream().map(ClusterAgentPool::getName).collect(Collectors.toSet());

            List<String> deleteAgentPool = currentAgentPoolNames.stream()
                .filter(o -> !pendingAgentPoolNames.contains(o))
                .collect(Collectors.toList());

            List<ClusterAgentPool> modifyAgentPool = pendingAgentPool.stream()
                .filter(o -> currentAgentPoolNames.contains(o.getName()))
                .collect(Collectors.toList());

            List<ClusterAgentPool> addAgentPool = pendingAgentPool.stream()
                .filter(o -> !currentAgentPoolNames.contains(o.getName()))
                .collect(Collectors.toList());

            if (!deleteAgentPool.isEmpty()) {
                for (String poolName : deleteAgentPool) {
                    update = update.withoutAgentPool(poolName);
                }
            }

            if (!modifyAgentPool.isEmpty()) {
                for (ClusterAgentPool agentPool : modifyAgentPool) {
                    update = update.updateAgentPool(agentPool.getName())
                        .withAgentPoolVirtualMachineCount(agentPool.getCount())
                        .withTags(agentPool.getTags())
                        .withAgentPoolMode(AgentPoolMode.fromString(agentPool.getMode()))
                        .withAutoScaling(agentPool.getMinimumNodeSize(), agentPool.getMaximumNodeSize())
                        .withKubeletDiskType(KubeletDiskType.fromString(agentPool.getKubeletDiskType()))
                        .parent();
                }
            }

            if (!addAgentPool.isEmpty()) {
                for (ClusterAgentPool agentPool : addAgentPool) {
                    WithAttach<? extends KubernetesCluster.Update> withAttach = update.defineAgentPool(agentPool.getName())
                        .withVirtualMachineSize(ContainerServiceVMSizeTypes.fromString(agentPool.getSize()))
                        .withAgentPoolVirtualMachineCount(agentPool.getMinimumNodeSize())

                        .withTags(agentPool.getTags())
                        .withAgentPoolMode(AgentPoolMode.fromString(agentPool.getMode()))
                        .withAgentPoolType(AgentPoolType.fromString(agentPool.getType()))
                        .withAvailabilityZones(agentPool.getAvailabilityZones().toArray(Integer[]::new))
                        .withKubeletDiskType(KubeletDiskType.fromString(agentPool.getKubeletDiskType()))
                        .withNodeLabels(agentPool.getNodeLabels())
                        .withNodeTaints(agentPool.getNodeTaints())
                        .withOSType(OSType.fromString(agentPool.getOsType()))
                        .withOSDiskType(OSDiskType.fromString(agentPool.getOsDiskType()))
                        .withOSDiskSizeInGB(agentPool.getOsDiskSizeInGb())
                        .withVirtualNetwork(agentPool.getNetwork().getId(), agentPool.getSubnet())
                        .withMaxPodsCount(agentPool.getMaximumPodsPerNode());

                    if (agentPool.getAutoScalingEnabled()) {
                        withAttach = withAttach.withAutoScaling(agentPool.getMinimumNodeSize(),
                            agentPool.getMaximumNodeSize());
                    }

                    if (!StringUtils.isBlank(agentPool.getVirtualMachineEvictionPolicy())) {
                        update = withAttach.withSpotPriorityVirtualMachine()
                            .withSpotPriorityVirtualMachine(ScaleSetEvictionPolicy.fromString(agentPool.getVirtualMachineEvictionPolicy()))
                            .withVirtualMachineMaximumPrice(agentPool.getVirtualMachineMaximumPrice())
                            .withVirtualMachinePriority(ScaleSetPriority.fromString(agentPool.getVirtualMachinePriority()))
                            .attach();
                    } else {
                        update = withAttach.attach();
                    }
                }
            }
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.kubernetesClusters().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }
}
