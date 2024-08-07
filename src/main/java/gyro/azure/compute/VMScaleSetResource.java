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

package gyro.azure.compute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.identity.IdentityResource;
import gyro.azure.network.ApplicationSecurityGroupResource;
import gyro.azure.network.LoadBalancerResource;
import gyro.azure.network.NetworkResource;
import gyro.azure.network.NetworkSecurityGroupResource;
import gyro.azure.resources.ResourceGroupResource;
import gyro.azure.storage.CloudBlobResource;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.GyroException;
import gyro.core.GyroInstance;
import gyro.core.GyroInstances;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a scale set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::scale-set scale-set-example
 *         name: "scale-set-example"
 *         resource-group: $(azure::resource-group scale-set-resource-group-example)
 *         sku-name: "Standard_A0"
 *         sku-tier: "Standard"
 *
 *         os-type: "linux"
 *         image-type: "popular"
 *         known-virtual-image: "UBUNTU_SERVER_14_04_LTS"
 *         admin-user-name: "qwerty@123"
 *         admin-password: "qwerty@123"
 *         capacity: 2
 *
 *         proximity-placement-group
 *             name: "proximity-placement-group-example"
 *             type: "STANDARD"
 *         end
 *
 *         network: $(azure::network scale-set-network-example)
 *         subnet-name: "subnet2"
 *
 *         primary-internet-facing-load-balancer
 *             load-balancer: $(azure::load-balancer scale-set-load-balancer-example-internet-facing)
 *             backends: [
 *                 "backend-one",
 *                 "backend-two"
 *             ]
 *             inbound-nat-pools: [
 *                 "test-nat-pool-public"
 *             ]
 *         end
 *
 *         primary-internal-load-balancer
 *             load-balancer: $(azure::load-balancer scale-set-load-balancer-example-internal)
 *             backends: [
 *                 "backend-one",
 *                 "backend-two"
 *             ]
 *             inbound-nat-pools: [
 *                 "test-nat-pool"
 *             ]
 *         end
 *
 *         tags: {
 *             Name: "scale-set-example"
 *         }
 *     end
 */
@Type("scale-set")
public class VMScaleSetResource extends AzureResource implements GyroInstances, Copyable<VirtualMachineScaleSet> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private String skuName;
    private String skuTier;
    private ProximityPlacementGroupResource proximityPlacementGroup;
    private Boolean doNotRunExtensionsOnOverprovisionedVMs;
    private AdditionalCapability additionalCapability;
    private NetworkResource network;
    private String subnetName;
    private LoadBalancerAttachment primaryInternetFacingLoadBalancer;
    private LoadBalancerAttachment primaryInternalLoadBalancer;
    private String osType;
    private String imageType;
    private String adminUserName;
    private String adminPassword;
    private String imagePublisher;
    private String imageOffer;
    private String imageSku;
    private String imageRegion;
    private String imageVersion;
    private String knownVirtualImage;
    private String storedImage;
    private String customImage;
    private String ssh;
    private Integer capacity;
    private Set<ApplicationSecurityGroupResource> applicationSecurityGroups;
    private Set<String> applicationGatewayBackendPoolIds;
    private NetworkSecurityGroupResource networkSecurityGroup;
    private StorageAccountResource storageAccount;
    private Map<String, String> tags;
    private Boolean enableAcceleratedNetworking;
    private Boolean enableBootDiagnostic;
    private StorageAccountResource bootDiagnosticStorage;
    private CloudBlobResource bootDiagnosticBlob;
    private String computerNamePrefix;
    private String customData;
    private Boolean enableIpForwarding;
    private String osDiskCaching;
    private String osDiskName;
    private Boolean enableOverProvision;
    private Boolean enableLowPriorityVm;
    private String lowPriorityVmPolicy;
    private String timeZone;
    private String id;
    private Boolean enableSystemManagedServiceIdentity;
    private String SystemManagedServiceIdentityPrincipalId;
    private Set<IdentityResource> identities;

    /**
     * The name of the Scale Set.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group under which the Scale Set would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The SKU name of the Scale Set.
     */
    @Required
    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    /**
     * The SKU tier of the Scale Set.
     */
    @Required
    public String getSkuTier() {
        return skuTier;
    }

    public void setSkuTier(String skuTier) {
        this.skuTier = skuTier;
    }

    /**
     * The Proximity Placement Group for the Scale Set.
     *
     * @subresource gyro.azure.compute.ProximityPlacementGroupResource
     */
    public ProximityPlacementGroupResource getProximityPlacementGroup() {
        return proximityPlacementGroup;
    }

    public void setProximityPlacementGroup(ProximityPlacementGroupResource proximityPlacementGroup) {
        this.proximityPlacementGroup = proximityPlacementGroup;
    }

    /**
     * Disable running extensions over provisioned VMs. Defaults to ``false``.
     */
    public Boolean getDoNotRunExtensionsOnOverprovisionedVMs() {
        if (doNotRunExtensionsOnOverprovisionedVMs == null) {
            doNotRunExtensionsOnOverprovisionedVMs = false;
        }

        return doNotRunExtensionsOnOverprovisionedVMs;
    }

    public void setDoNotRunExtensionsOnOverprovisionedVMs(Boolean doNotRunExtensionsOnOverprovisionedVMs) {
        this.doNotRunExtensionsOnOverprovisionedVMs = doNotRunExtensionsOnOverprovisionedVMs;
    }

    /**
     * Additional capability for the Scale set to enable Ultra SSD. Defaults to disabled.
     *
     * @subresource gyro.azure.compute.AdditionalCapability
     */
    @Updatable
    public AdditionalCapability getAdditionalCapability() {
        if (additionalCapability == null) {
            additionalCapability = newSubresource(AdditionalCapability.class);
        }

        return additionalCapability;
    }

    public void setAdditionalCapability(AdditionalCapability additionalCapability) {
        this.additionalCapability = additionalCapability;
    }

    /**
     * The Virtual Network to be associated with the Scale Set.
     */
    @Required
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The Subnet to be associated with the Scale Set.
     */
    @Required
    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    /**
     * The primary internet facing Load Balancer to be associated with the Scale Set.
     *
     * @subresource gyro.azure.compute.LoadBalancerAttachment
     */
    @Updatable
    public LoadBalancerAttachment getPrimaryInternetFacingLoadBalancer() {
        return primaryInternetFacingLoadBalancer;
    }

    public void setPrimaryInternetFacingLoadBalancer(LoadBalancerAttachment primaryInternetFacingLoadBalancer) {
        this.primaryInternetFacingLoadBalancer = primaryInternetFacingLoadBalancer;
    }

    /**
     * The primary internal Load Balancer to be associated with the Scale Set.
     *
     * @subresource gyro.azure.compute.LoadBalancerAttachment
     */
    @Updatable
    public LoadBalancerAttachment getPrimaryInternalLoadBalancer() {
        return primaryInternalLoadBalancer;
    }

    public void setPrimaryInternalLoadBalancer(LoadBalancerAttachment primaryInternalLoadBalancer) {
        this.primaryInternalLoadBalancer = primaryInternalLoadBalancer;
    }

    /**
     * The type of os for the VMs deployed by the Scale Set.
     */
    @Required
    @ValidStrings({ "linux", "windows" })
    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    /**
     * The type of image to be used for the VMs deployed by the Scale Set.
     */
    @Required
    @ValidStrings({ "latest", "popular", "specific", "custom", "stored" })
    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * The root/admin user name for the os for the VMs deployed by the Scale Set.
     */
    @Required
    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    /**
     * The root/admin password for the os for the VMs deployed by the Scale Set. Required if 'ssh' not set.
     */
    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * The root/admin ssh for the os for the VMs deployed by the Scale Set. Required if 'password' not set.
     */
    public String getSsh() {
        return ssh;
    }

    public void setSsh(String ssh) {
        this.ssh = ssh;
    }

    /**
     * The publisher of the image to be used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImagePublisher() {
        return imagePublisher;
    }

    public void setImagePublisher(String imagePublisher) {
        this.imagePublisher = imagePublisher;
    }

    /**
     * The offer of the image to be used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImageOffer() {
        return imageOffer;
    }

    public void setImageOffer(String imageOffer) {
        this.imageOffer = imageOffer;
    }

    /**
     * The SKU of the image to be used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImageSku() {
        return imageSku;
    }

    public void setImageSku(String imageSku) {
        this.imageSku = imageSku;
    }

    /**
     * The region where the image resides to be used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImageRegion() {
        return imageRegion;
    }

    public void setImageRegion(String imageRegion) {
        this.imageRegion = imageRegion;
    }

    /**
     * The version of the image to be used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    /**
     * The known Virtual Machine image type used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``popular``.
     */
    public String getKnownVirtualImage() {
        return knownVirtualImage;
    }

    public void setKnownVirtualImage(String knownVirtualImage) {
        this.knownVirtualImage = knownVirtualImage;
    }

    /**
     * The Stored Image Uri used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``stored``.
     */
    public String getStoredImage() {
        return storedImage;
    }

    public void setStoredImage(String storedImage) {
        this.storedImage = storedImage;
    }

    /**
     * The CustomImage ID used for creating the VMs deployed by the Scale Set. Required if ``image-type`` selected as ``custom``.
     */
    public String getCustomImage() {
        return customImage;
    }

    public void setCustomImage(String customImage) {
        this.customImage = customImage;
    }

    /**
     * The max supported VMs in a the Scale Set.
     */
    @Required
    @Updatable
    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * A set of associated Application Security Groups with the Scale Set.
     */
    @Updatable
    public Set<ApplicationSecurityGroupResource> getApplicationSecurityGroups() {
        if (applicationSecurityGroups == null) {
            applicationSecurityGroups = new HashSet<>();
        }

        return applicationSecurityGroups;
    }

    public void setApplicationSecurityGroups(Set<ApplicationSecurityGroupResource> applicationSecurityGroups) {
        this.applicationSecurityGroups = applicationSecurityGroups;
    }

    /**
     * A set of associated Application Gateway Backend Pool IDs with the Scale Set.
     */
    @Updatable
    public Set<String> getApplicationGatewayBackendPoolIds() {
        if (applicationGatewayBackendPoolIds == null) {
            applicationGatewayBackendPoolIds = new HashSet<>();
        }

        return applicationGatewayBackendPoolIds;
    }

    public void setApplicationGatewayBackendPoolIds(Set<String> applicationGatewayBackendPoolIds) {
        this.applicationGatewayBackendPoolIds = applicationGatewayBackendPoolIds;
    }

    /**
     * Associated Network Security Group with the Scale Set.
     */
    @Updatable
    public NetworkSecurityGroupResource getNetworkSecurityGroup() {
        return networkSecurityGroup;
    }

    public void setNetworkSecurityGroup(NetworkSecurityGroupResource networkSecurityGroup) {
        this.networkSecurityGroup = networkSecurityGroup;
    }

    /**
     * Associated Storage Account with the Scale Set.
     */
    public StorageAccountResource getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccountResource storageAccount) {
        this.storageAccount = storageAccount;
    }

    /**
     * A set of tags for the Scale Set.
     */
    @Updatable
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * Enable/Disable accelerated networking for the Scale Set. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableAcceleratedNetworking() {
        if (enableAcceleratedNetworking == null) {
            enableAcceleratedNetworking = false;
        }

        return enableAcceleratedNetworking;
    }

    public void setEnableAcceleratedNetworking(Boolean enableAcceleratedNetworking) {
        this.enableAcceleratedNetworking = enableAcceleratedNetworking;
    }

    /**
     * Enable/Disable boot diagnostic for the Scale Set. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableBootDiagnostic() {
        if (enableBootDiagnostic == null) {
            enableBootDiagnostic = false;
        }

        return enableBootDiagnostic;
    }

    public void setEnableBootDiagnostic(Boolean enableBootDiagnostic) {
        this.enableBootDiagnostic = enableBootDiagnostic;
    }

    /**
     * Set the Storage Account to store the boot diagnostic for the Scale Set. Allowed only if 'enable-boot-diagnostic' is set to ``true``.
     */
    @Updatable
    public StorageAccountResource getBootDiagnosticStorage() {
        return bootDiagnosticStorage;
    }

    public void setBootDiagnosticStorage(StorageAccountResource bootDiagnosticStorage) {
        this.bootDiagnosticStorage = bootDiagnosticStorage;
    }

    /**
     * Set the Storage Account Blob to store the boot diagnostic for the Scale Set. Allowed only if 'enable-boot-diagnostic' is set to ``true``.
     */
    @Updatable
    public CloudBlobResource getBootDiagnosticBlob() {
        return bootDiagnosticBlob;
    }

    public void setBootDiagnosticBlob(CloudBlobResource bootDiagnosticBlob) {
        this.bootDiagnosticBlob = bootDiagnosticBlob;
    }

    /**
     * Set the prefix of the VMs launched by this Scale Set.
     */
    public String getComputerNamePrefix() {
        return computerNamePrefix;
    }

    public void setComputerNamePrefix(String computerNamePrefix) {
        this.computerNamePrefix = computerNamePrefix;
    }

    /**
     * Set the custom data of the VMs launched by this Scale Set.
     */
    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    /**
     * Enable/Disable IP forwarding for the VMs launched by this Scale Set. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableIpForwarding() {
        if (enableIpForwarding == null) {
            enableIpForwarding = false;
        }

        return enableIpForwarding;
    }

    public void setEnableIpForwarding(Boolean enableIpForwarding) {
        this.enableIpForwarding = enableIpForwarding;
    }

    /**
     * Set the OS Disk caching type for the VMs launched by this Scale Set.
     */
    @ValidStrings({ "NONE", "READ_ONLY", "READ_WRITE" })
    public String getOsDiskCaching() {
        if (osDiskCaching != null) {
            osDiskCaching = osDiskCaching.toUpperCase();
        }

        return osDiskCaching;
    }

    public void setOsDiskCaching(String osDiskCaching) {
        this.osDiskCaching = osDiskCaching;
    }

    /**
     * Set the OS Disk name for the VMs launched by this Scale Set.
     */
    public String getOsDiskName() {
        return osDiskName;
    }

    public void setOsDiskName(String osDiskName) {
        this.osDiskName = osDiskName;
    }

    /**
     * Enable/Disable over provisioning for the VMs launched by this Scale Set. Defaults to ``false``.
     */
    public Boolean getEnableOverProvision() {
        if (enableOverProvision == null) {
            enableOverProvision = false;
        }

        return enableOverProvision;
    }

    public void setEnableOverProvision(Boolean enableOverProvision) {
        this.enableOverProvision = enableOverProvision;
    }

    /**
     * Enable/Disable flagging low priority for the VMs launched by this Scale Set. Defaults to ``false``.
     */
    public Boolean getEnableLowPriorityVm() {
        if (enableLowPriorityVm == null) {
            enableLowPriorityVm = false;
        }

        return enableLowPriorityVm;
    }

    public void setEnableLowPriorityVm(Boolean enableLowPriorityVm) {
        this.enableLowPriorityVm = enableLowPriorityVm;
    }

    /**
     * Set the policy for eviction of the flagged low priority VMs launched by this Scale Set. Allowed only of 'enable-low-priority-vm' is set to ``true``.
     */
    @ValidStrings({ "DEALLOCATE", "DELETE" })
    public String getLowPriorityVmPolicy() {
        return lowPriorityVmPolicy;
    }

    public void setLowPriorityVmPolicy(String lowPriorityVmPolicy) {
        this.lowPriorityVmPolicy = lowPriorityVmPolicy;
    }

    /**
     * The time zone for VMs launched by this Scale Set. Required when 'os-type' is set to ``windows``.
     */
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Enable system managed service identity for scale sets. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableSystemManagedServiceIdentity() {
        if (enableSystemManagedServiceIdentity == null) {
            enableSystemManagedServiceIdentity = false;
        }

        return enableSystemManagedServiceIdentity;
    }

    public void setEnableSystemManagedServiceIdentity(Boolean enableSystemManagedServiceIdentity) {
        this.enableSystemManagedServiceIdentity = enableSystemManagedServiceIdentity;
    }

    /**
     * The principal id for the system managed service identity of the Scale Set.
     */
    @Output
    public String getSystemManagedServiceIdentityPrincipalId() {
        return SystemManagedServiceIdentityPrincipalId;
    }

    public void setSystemManagedServiceIdentityPrincipalId(String systemManagedServiceIdentityPrincipalId) {
        SystemManagedServiceIdentityPrincipalId = systemManagedServiceIdentityPrincipalId;
    }

    /**
     * A list of identities associated with the Scale Set.
     */
    @Updatable
    public Set<IdentityResource> getIdentities() {
        if (identities == null) {
            identities = new HashSet<>();
        }

        return identities;
    }

    public void setIdentities(Set<IdentityResource> identities) {
        this.identities = identities;
    }

    /**
     * The ID of the Scale Set.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String getEncodedCustomData() {
        return !ObjectUtils.isBlank(getCustomData())
            ? Base64.getEncoder().encodeToString(getCustomData().getBytes())
            : null;
    }

    private String getDecodedCustomData(String data) {
        return !ObjectUtils.isBlank(data) ? new String(Base64.getDecoder().decode(data.getBytes())) : null;
    }

    @Override
    public void copyFrom(VirtualMachineScaleSet scaleSet) {
        try {
            setName(scaleSet.name());
            setId(scaleSet.id());
            setResourceGroup(findById(ResourceGroupResource.class, scaleSet.resourceGroupName()));
            setSkuName(scaleSet.sku().sku().name());
            setSkuTier(scaleSet.sku().sku().tier());
            setDoNotRunExtensionsOnOverprovisionedVMs(scaleSet.doNotRunExtensionsOnOverprovisionedVMs());
            setApplicationGatewayBackendPoolIds(new HashSet<>(scaleSet.applicationGatewayBackendAddressPoolsIds()));
            setApplicationSecurityGroups(scaleSet.applicationSecurityGroupIds()
                .stream()
                .map(o -> findById(ApplicationSecurityGroupResource.class, o))
                .collect(Collectors.toSet()));
            setNetworkSecurityGroup(findById(NetworkSecurityGroupResource.class, scaleSet.networkSecurityGroupId()));
            setImagePublisher(scaleSet.storageProfile().imageReference().publisher());

            if (scaleSet.innerModel().proximityPlacementGroup() != null) {
                setProximityPlacementGroup(newSubresource(ProximityPlacementGroupResource.class));
                getProximityPlacementGroup().copyFrom(scaleSet.proximityPlacementGroup());
            } else {
                setProximityPlacementGroup(null);
            }

            if (scaleSet.additionalCapabilities() != null) {
                AdditionalCapability capability = newSubresource(AdditionalCapability.class);
                capability.copyFrom(scaleSet.additionalCapabilities());
                setAdditionalCapability(capability);
            } else {
                setAdditionalCapability(null);
            }

            if (scaleSet.getPrimaryInternalLoadBalancer() != null) {
                LoadBalancerAttachment attachment = newSubresource(LoadBalancerAttachment.class);
                attachment.setLoadBalancer(findById(
                    LoadBalancerResource.class,
                    scaleSet.getPrimaryInternalLoadBalancer().id()));
                attachment.setBackends(scaleSet.listPrimaryInternalLoadBalancerBackends().keySet());
                attachment.setInboundNatPools(scaleSet.listPrimaryInternalLoadBalancerInboundNatPools().keySet());
                setPrimaryInternalLoadBalancer(attachment);
            } else {
                setPrimaryInternalLoadBalancer(null);
            }

            if (scaleSet.getPrimaryInternetFacingLoadBalancer() != null) {
                LoadBalancerAttachment attachment = newSubresource(LoadBalancerAttachment.class);
                attachment.setLoadBalancer(findById(
                    LoadBalancerResource.class,
                    scaleSet.getPrimaryInternetFacingLoadBalancer().id()));
                attachment.setBackends(scaleSet.listPrimaryInternetFacingLoadBalancerBackends().keySet());
                attachment.setInboundNatPools(scaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().keySet());
                setPrimaryInternetFacingLoadBalancer(attachment);
            } else {
                setPrimaryInternetFacingLoadBalancer(null);
            }

            setEnableSystemManagedServiceIdentity(!ObjectUtils.isBlank(scaleSet.systemAssignedManagedServiceIdentityPrincipalId()));
            setSystemManagedServiceIdentityPrincipalId(scaleSet.systemAssignedManagedServiceIdentityPrincipalId());

            getIdentities().clear();
            if (scaleSet.userAssignedManagedServiceIdentityIds() != null) {
                getIdentities().addAll(
                    scaleSet.userAssignedManagedServiceIdentityIds()
                        .stream()
                        .map(o -> findById(IdentityResource.class, o))
                        .collect(Collectors.toSet())
                );

            }
        } catch (IOException ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        VirtualMachineScaleSet scaleSet = client.virtualMachineScaleSets().getById(getId());

        if (scaleSet == null) {
            return false;
        }

        copyFrom(scaleSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        VirtualMachineScaleSet.DefinitionStages.WithProximityPlacementGroup withProximityPlacementGroup = client.virtualMachineScaleSets()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withSku(VirtualMachineScaleSetSkuTypes.fromSkuNameAndTier(getSkuName(), getSkuTier()));

        VirtualMachineScaleSet.DefinitionStages.WithPrimaryInternetFacingLoadBalancer primaryStage;

        if (getProximityPlacementGroup() != null) {
            primaryStage = withProximityPlacementGroup
                .withNewProximityPlacementGroup(
                    getProximityPlacementGroup().getName(),
                    ProximityPlacementGroupType.fromString(getProximityPlacementGroup().getType()))
                .withDoNotRunExtensionsOnOverprovisionedVMs(getDoNotRunExtensionsOnOverprovisionedVMs())
                .withAdditionalCapabilities(getAdditionalCapability().toAdditionalCapabilities())
                .withExistingPrimaryNetworkSubnet(client.networks().getById(getNetwork().getId()), getSubnetName());
        } else {
            primaryStage = withProximityPlacementGroup
                .withDoNotRunExtensionsOnOverprovisionedVMs(getDoNotRunExtensionsOnOverprovisionedVMs())
                .withAdditionalCapabilities(getAdditionalCapability().toAdditionalCapabilities())
                .withExistingPrimaryNetworkSubnet(client.networks().getById(getNetwork().getId()), getSubnetName());
        }

        VirtualMachineScaleSet.DefinitionStages.WithPrimaryInternalLoadBalancer internetFacingLbStage;

        if (getPrimaryInternetFacingLoadBalancer() == null) {
            internetFacingLbStage = primaryStage.withoutPrimaryInternetFacingLoadBalancer();
        } else {
            internetFacingLbStage = primaryStage.withExistingPrimaryInternetFacingLoadBalancer(client.loadBalancers()
                    .getById(getPrimaryInternetFacingLoadBalancer().getLoadBalancer().getId()))
                .withPrimaryInternetFacingLoadBalancerBackends(getPrimaryInternetFacingLoadBalancer().getBackends()
                    .toArray(new String[0]))
                .withPrimaryInternetFacingLoadBalancerInboundNatPools(getPrimaryInternetFacingLoadBalancer().getInboundNatPools()
                    .toArray(new String[0]));
        }

        VirtualMachineScaleSet.DefinitionStages.WithOS internalLbStage;

        if (getPrimaryInternalLoadBalancer() == null) {
            internalLbStage = internetFacingLbStage.withoutPrimaryInternalLoadBalancer();
        } else {
            internalLbStage = internetFacingLbStage.withExistingPrimaryInternalLoadBalancer(client.loadBalancers()
                    .getById(getPrimaryInternalLoadBalancer().getLoadBalancer().getId()))
                .withPrimaryInternalLoadBalancerBackends(getPrimaryInternalLoadBalancer().getBackends()
                    .toArray(new String[0]))
                .withPrimaryInternalLoadBalancerInboundNatPools(getPrimaryInternalLoadBalancer().getInboundNatPools()
                    .toArray(new String[0]));
        }

        VirtualMachineScaleSet.DefinitionStages.WithCreate finalStage = null;

        if (getOsType().equals("linux")) {
            VirtualMachineScaleSet.DefinitionStages.WithLinuxRootUsernameManagedOrUnmanaged linuxStageA = null;
            VirtualMachineScaleSet.DefinitionStages.WithLinuxRootUsernameManaged linuxStageB = null;
            VirtualMachineScaleSet.DefinitionStages.WithLinuxRootUsernameUnmanaged linuxStageC = null;

            if (getImageType().equals("latest")) {
                linuxStageA = internalLbStage.withLatestLinuxImage(getImagePublisher(), getImageOffer(), getImageSku());
            } else if (getImageType().equals("popular")) {
                linuxStageA = internalLbStage.withPopularLinuxImage(KnownLinuxVirtualMachineImage.valueOf(
                    getKnownVirtualImage()));
            } else if (getImageType().equals("specific")) {
                linuxStageA = internalLbStage.withSpecificLinuxImageVersion(client.virtualMachineImages()
                    .getImage(getImageRegion(), getImagePublisher(), getImageOffer(), getImageSku(), getImageVersion())
                    .imageReference());
            } else if (getImageType().equals("custom")) {
                linuxStageB = internalLbStage.withGeneralizedLinuxCustomImage(getCustomImage());
            } else { // stored
                linuxStageC = internalLbStage.withStoredLinuxImage(getStoredImage());
            }

            // Todo Data Dsk and Os Disk
            if (linuxStageA != null) {
                if (!ObjectUtils.isBlank(getAdminPassword()) && !ObjectUtils.isBlank(getSsh())) {
                    finalStage = linuxStageA.withRootUsername(getAdminUserName())
                        .withRootPassword(getAdminPassword())
                        .withSsh(getSsh());
                } else if (!ObjectUtils.isBlank(getAdminPassword())) {
                    finalStage = linuxStageA.withRootUsername(getAdminUserName()).withRootPassword(getAdminPassword());
                } else {
                    finalStage = linuxStageA.withRootUsername(getAdminUserName()).withSsh(getSsh());
                }
            } else if (linuxStageB != null) {
                if (!ObjectUtils.isBlank(getAdminPassword()) && !ObjectUtils.isBlank(getSsh())) {
                    finalStage = linuxStageB.withRootUsername(getAdminUserName())
                        .withRootPassword(getAdminPassword())
                        .withSsh(getSsh());
                } else if (!ObjectUtils.isBlank(getAdminPassword())) {
                    finalStage = linuxStageB.withRootUsername(getAdminUserName()).withRootPassword(getAdminPassword());
                } else {
                    finalStage = linuxStageB.withRootUsername(getAdminUserName()).withSsh(getSsh());
                }
            } else {
                if (!ObjectUtils.isBlank(getAdminPassword()) && !ObjectUtils.isBlank(getSsh())) {
                    finalStage = linuxStageC.withRootUsername(getAdminUserName())
                        .withRootPassword(getAdminPassword())
                        .withSsh(getSsh());
                } else if (!ObjectUtils.isBlank(getAdminPassword())) {
                    finalStage = linuxStageC.withRootUsername(getAdminUserName()).withRootPassword(getAdminPassword());
                } else {
                    finalStage = linuxStageC.withRootUsername(getAdminUserName()).withSsh(getSsh());
                }
            }
        } else {
            //windows
            VirtualMachineScaleSet.DefinitionStages.WithWindowsAdminUsernameManagedOrUnmanaged windowsStageA = null;
            VirtualMachineScaleSet.DefinitionStages.WithWindowsAdminUsernameManaged windowsStageB = null;
            VirtualMachineScaleSet.DefinitionStages.WithWindowsAdminUsernameUnmanaged windowsStageC = null;

            if (getImageType().equals("latest")) {
                windowsStageA = internalLbStage.withLatestWindowsImage(
                    getImagePublisher(),
                    getImageOffer(),
                    getImageSku());
            } else if (getImageType().equals("popular")) {
                windowsStageA = internalLbStage.withPopularWindowsImage(KnownWindowsVirtualMachineImage.valueOf(
                    getKnownVirtualImage()));
            } else if (getImageType().equals("specific")) {
                windowsStageA = internalLbStage.withSpecificWindowsImageVersion(client.virtualMachineImages()
                    .getImage(getImageRegion(), getImagePublisher(), getImageOffer(), getImageSku(), getImageVersion())
                    .imageReference());
            } else if (getImageType().equals("custom")) {
                windowsStageB = internalLbStage.withGeneralizedWindowsCustomImage(getCustomImage());
            } else { // stored
                windowsStageC = internalLbStage.withStoredWindowsImage(getStoredImage());
            }

            VirtualMachineScaleSet.DefinitionStages.WithWindowsCreateUnmanaged windowsStageUnmanaged = null;
            VirtualMachineScaleSet.DefinitionStages.WithWindowsCreateManaged windowsStageManaged = null;

            if (windowsStageA != null) {
                windowsStageUnmanaged = windowsStageA.withAdminUsername(getAdminUserName())
                    .withAdminPassword(getAdminPassword())
                    .withUnmanagedDisks();
            } else if (windowsStageB != null) {
                windowsStageManaged = windowsStageB.withAdminUsername(getAdminUserName())
                    .withAdminPassword(getAdminPassword());
            } else {
                windowsStageUnmanaged = windowsStageC.withAdminUsername(getAdminUserName())
                    .withAdminPassword(getAdminPassword());
            }

            if (windowsStageUnmanaged != null) {
                finalStage = windowsStageUnmanaged
                    .withoutAutoUpdate()
                    .withoutVMAgent()
                    .withTimeZone(getTimeZone())
                    .withCapacity(getCapacity());
            } else {
                finalStage = windowsStageManaged
                    .withoutAutoUpdate()
                    .withoutVMAgent()
                    .withTimeZone(getTimeZone())
                    .withCapacity(getCapacity());
            }
        }

        finalStage = finalStage.withCapacity(getCapacity());

        for (String backendPoolId : getApplicationGatewayBackendPoolIds()) {
            finalStage = finalStage.withExistingApplicationGatewayBackendPool(backendPoolId);
        }

        for (ApplicationSecurityGroupResource securityGroup : getApplicationSecurityGroups()) {
            finalStage = finalStage.withExistingApplicationSecurityGroupId(securityGroup.getId());
        }

        if (getNetworkSecurityGroup() != null) {
            finalStage = finalStage.withExistingNetworkSecurityGroupId(getNetworkSecurityGroup().getId());
        }

        if (getStorageAccount() != null) {
            finalStage = finalStage.withExistingStorageAccount(client.storageAccounts()
                .getById(getStorageAccount().getId()));
        }

        finalStage = finalStage.withOverProvision(getEnableOverProvision());

        if (getEnableAcceleratedNetworking()) {
            finalStage = finalStage.withAcceleratedNetworking();
        }

        if (getEnableBootDiagnostic()) {
            if (getBootDiagnosticBlob() != null) {
                finalStage = finalStage.withBootDiagnostics(getBootDiagnosticBlob().getUri());
            } else if (getBootDiagnosticStorage() != null) {
                finalStage = finalStage.withBootDiagnostics(client.storageAccounts()
                    .getById(getBootDiagnosticStorage().getId()));
            } else {
                finalStage = finalStage.withBootDiagnostics();
            }
        }

        if (!getTags().isEmpty()) {
            finalStage = finalStage.withTags(getTags());
        }

        if (!ObjectUtils.isBlank(getComputerNamePrefix())) {
            finalStage = finalStage.withComputerNamePrefix(getComputerNamePrefix());
        }

        finalStage = finalStage.withCustomData(getEncodedCustomData());

        if (getEnableIpForwarding()) {
            finalStage = finalStage.withIpForwarding();
        }

        if (!ObjectUtils.isBlank(getOsDiskCaching())) {
            finalStage = finalStage.withOSDiskCaching(CachingTypes.valueOf(getOsDiskCaching()));
        }

        if (!ObjectUtils.isBlank(getOsDiskName())) {
            finalStage = finalStage.withOSDiskName(getOsDiskName());
        }

        if (getEnableLowPriorityVm()) {
            if (!ObjectUtils.isBlank(getLowPriorityVmPolicy())) {
                finalStage = finalStage.withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes.fromString(
                    getLowPriorityVmPolicy()));
            } else {
                finalStage = finalStage.withLowPriorityVirtualMachine();
            }
        }

        if (getEnableSystemManagedServiceIdentity()) {
            finalStage = finalStage.withSystemAssignedManagedServiceIdentity();
        }

        for (IdentityResource identity : getIdentities()) {
            finalStage = finalStage.withExistingUserAssignedManagedServiceIdentity(client.identities()
                .getById(identity.getId()));
        }

        VirtualMachineScaleSet scaleSet = finalStage.create();
        setId(scaleSet.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        VirtualMachineScaleSet scaleSet = client.virtualMachineScaleSets().getById(getId());

        VirtualMachineScaleSet.UpdateStages.WithPrimaryInternalLoadBalancer a1 = null;
        VirtualMachineScaleSet.UpdateStages.WithApply update;

        if (getPrimaryInternetFacingLoadBalancer() != null) {
            a1 = scaleSet.update()
                .withExistingPrimaryInternetFacingLoadBalancer(client.loadBalancers()
                    .getById(getPrimaryInternetFacingLoadBalancer().getLoadBalancer().getId()))
                .withPrimaryInternetFacingLoadBalancerBackends(getPrimaryInternetFacingLoadBalancer().getBackends()
                    .toArray(new String[0]))
                .withPrimaryInternetFacingLoadBalancerInboundNatPools(getPrimaryInternetFacingLoadBalancer().getInboundNatPools()
                    .toArray(new String[0]));
        }

        if (getPrimaryInternalLoadBalancer() != null) {
            if (a1 != null) {
                update = a1.withExistingPrimaryInternalLoadBalancer(client.loadBalancers()
                        .getById(getPrimaryInternalLoadBalancer().getLoadBalancer().getId()))
                    .withPrimaryInternalLoadBalancerBackends(getPrimaryInternalLoadBalancer().getBackends()
                        .toArray(new String[0]))
                    .withPrimaryInternalLoadBalancerInboundNatPools(getPrimaryInternalLoadBalancer().getInboundNatPools()
                        .toArray(new String[0]));
            } else {
                update = scaleSet.update()
                    .withExistingPrimaryInternalLoadBalancer(client.loadBalancers()
                        .getById(getPrimaryInternalLoadBalancer().getLoadBalancer().getId()))
                    .withPrimaryInternalLoadBalancerBackends(getPrimaryInternalLoadBalancer().getBackends()
                        .toArray(new String[0]))
                    .withPrimaryInternalLoadBalancerInboundNatPools(getPrimaryInternalLoadBalancer().getInboundNatPools()
                        .toArray(new String[0]));
            }
        } else {
            if (a1 != null) {
                update = a1.withoutPrimaryInternalLoadBalancer();
            } else {
                update = scaleSet.update().withoutPrimaryInternalLoadBalancer();
            }
        }

        if (getPrimaryInternetFacingLoadBalancer() == null) {
            update = update.withoutPrimaryInternetFacingLoadBalancer();
        }

        update = update.withTags(getTags());

        update = update.withCapacity(getCapacity());

        VMScaleSetResource currentScaleSetResource = (VMScaleSetResource) current;

        if (changedFieldNames.contains("application-gateway-backend-pool-ids")) {
            for (String backendPoolId : currentScaleSetResource.getApplicationGatewayBackendPoolIds()) {
                update = update.withoutApplicationGatewayBackendPool(backendPoolId);
            }

            for (String backendPoolId : getApplicationGatewayBackendPoolIds()) {
                update = update.withExistingApplicationGatewayBackendPool(backendPoolId);
            }
        }

        if (changedFieldNames.contains("application-security-group")) {
            for (ApplicationSecurityGroupResource securityGroup : currentScaleSetResource.getApplicationSecurityGroups()) {
                update = update.withoutApplicationSecurityGroup(securityGroup.getId());
            }

            for (ApplicationSecurityGroupResource securityGroup : getApplicationSecurityGroups()) {
                update = update.withExistingApplicationSecurityGroupId(securityGroup.getId());
            }
        }

        if (getNetworkSecurityGroup() != null) {
            update = update.withExistingNetworkSecurityGroupId(getNetworkSecurityGroup().getId());
        } else {
            update = update.withoutNetworkSecurityGroup();
        }

        if (getEnableAcceleratedNetworking()) {
            update = update.withAcceleratedNetworking();
        } else {
            update = update.withoutAcceleratedNetworking();
        }

        if (getEnableBootDiagnostic()) {
            if (getBootDiagnosticBlob() != null) {
                update = update.withBootDiagnostics(getBootDiagnosticBlob().getUri());
            } else if (getBootDiagnosticStorage() != null) {
                update = update.withBootDiagnostics(client.storageAccounts()
                    .getById(getBootDiagnosticStorage().getId()));
            } else {
                update = update.withBootDiagnostics();
            }
        } else {
            update = update.withoutBootDiagnostics();
        }

        if (getEnableIpForwarding()) {
            update = update.withIpForwarding();
        } else {
            update = update.withoutIpForwarding();
        }

        if (changedFieldNames.contains("enable-system-managed-service-identity")) {
            if (getEnableSystemManagedServiceIdentity()) {
                update = update.withSystemAssignedManagedServiceIdentity();
            } else {
                update = update.withoutSystemAssignedManagedServiceIdentity();
            }
        }

        if (changedFieldNames.contains("identities")) {
            for (IdentityResource identity : ((VMScaleSetResource) current).getIdentities()) {
                update = update.withoutUserAssignedManagedServiceIdentity(identity.getId());
            }

            for (IdentityResource identity : getIdentities()) {
                update = update.withExistingUserAssignedManagedServiceIdentity(client.identities()
                    .getById(identity.getId()));
            }
        }

        update = update.withAdditionalCapabilities(getAdditionalCapability().toAdditionalCapabilities());

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.virtualMachineScaleSets().deleteById(getId());
    }

    @Override
    public List<GyroInstance> getInstances() {
        List<GyroInstance> instances = new ArrayList<>();
        AzureResourceManager client = createClient(AzureResourceManager.class);

        VirtualMachineScaleSet virtualMachineScaleSet = client.virtualMachineScaleSets().getById(getId());

        if (virtualMachineScaleSet != null) {
            PagedIterable<VirtualMachineScaleSetVM> list = virtualMachineScaleSet.virtualMachines().list();
            List<String> instanceIds = list.stream()
                .map(VirtualMachineScaleSetVM::instanceId)
                .collect(Collectors.toList());

            instances.addAll(list.stream()
                .map(o -> {
                    VMScaleSetVirtualMachine vmResource = newSubresource(VMScaleSetVirtualMachine.class);
                    vmResource.copyFrom(o);
                    return vmResource;
                }).collect(Collectors.toList()));
        }

        return instances;
    }
}
