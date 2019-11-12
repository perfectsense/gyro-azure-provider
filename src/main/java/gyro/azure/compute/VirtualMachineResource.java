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

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxCreateManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxCreateManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxCreateUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootUsernameManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootUsernameManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootUsernameUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithManagedCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithUnmanagedCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithFromImageCreateOptionsManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithFromImageCreateOptionsManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithFromImageCreateOptionsUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithNetwork;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithOS;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithPrivateIP;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithPublicIPAddress;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsAdminUsernameManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsAdminUsernameManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsAdminUsernameUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsCreateManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsCreateManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsCreateUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.identity.IdentityResource;
import gyro.azure.network.NetworkInterfaceResource;
import gyro.azure.network.NetworkResource;
import gyro.azure.network.PublicIpAddressResource;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroInstance;
import gyro.core.GyroUI;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a virtual machine.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::virtual-machine virtual-machine-example
 *         name: "virtual-machine-example"
 *         resource-group: $(azure::resource-group resource-group-example-VM)
 *         network: $(azure::network network-example-VM)
 *         subnet: "subnet1"
 *         os-type: "linux"
 *         os-disk: $(azure::disk os-disk-example-VM)
 *         delete-os-disk-on-terminate: true
 *         data-disks: [ $(azure::disk data-disk-example-VM) ]
 *         network-interface: $(azure::network-interface network-interface-example-VM)
 *         vm-image-type: "popular"
 *         known-virtual-image: "UBUNTU_SERVER_14_04_LTS"
 *         admin-user-name: "qwerty@123"
 *         admin-password: "qwerty@123"
 *         caching-type: "NONE"
 *         vm-size-type: "STANDARD_G1"
 *         storage-account-type-data-disk: "STANDARD_LRS"
 *         storage-account-type-os-disk: "STANDARD_LRS"
 *
 *         tags: {
 *             Name: "virtual-machine-example"
 *         }
 *    end
 */
@Type("virtual-machine")
public class VirtualMachineResource extends AzureResource implements GyroInstance, Copyable<VirtualMachine> {
    private String name;
    private ResourceGroupResource resourceGroup;
    private NetworkResource network;
    private NetworkInterfaceResource networkInterface;
    private String adminUserName;
    private String adminPassword;
    private AvailabilitySetResource availabilitySet;
    private String id;
    private String vmId;
    private PublicIpAddressResource publicIpAddress;
    private String privateIpAddress;
    private String osType;
    private DiskResource osDisk;
    private Boolean deleteOsDiskOnTerminate;
    private Set<DiskResource> dataDisks;
    private String subnet;
    private String vmImageType;
    private String ssh;
    private String storedImage;
    private String customImage;
    private String galleryImageVersion;
    private String cachingType;
    private String storageAccountTypeDataDisk;
    private String storageAccountTypeOsDisk;
    private String vmSizeType;
    private String knownVirtualImage;
    private String timeZone;
    private String imagePublisher;
    private String imageOffer;
    private String imageSku;
    private String imageRegion;
    private String imageVersion;
    private Set<NetworkInterfaceResource> secondaryNetworkInterface;
    private Map<String, String> tags;
    private String customData;
    private Boolean enableSystemManagedServiceIdentity;
    private String SystemManagedServiceIdentityPrincipalId;
    private Set<IdentityResource> identities;
    private String state;
    private String location;
    private String computerName;
    private String publicIpAddressIp;
    private Date launchDate;

    /**
     * Name of the Virtual Machine. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the Virtual Machine would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The virtual network which would be associated with this. (Required)
     */
    @Required
    public NetworkResource getNetwork() {
        return network;
    }

    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The network interface that the Virtual Machine would use.
     */
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * Login user name for the Virtual Machine.
     */
    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    /**
     * The availability set of the Virtual Machine.
     */
    public AvailabilitySetResource getAvailabilitySet() {
        return availabilitySet;
    }

    public void setAvailabilitySet(AvailabilitySetResource availabilitySet) {
        this.availabilitySet = availabilitySet;
    }

    /**
     * Login password for the Virtual Machine.
     */
    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * The ID for the Virtual Machine.
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
     * Shortened ID for the Virtual Machine.
     */
    @Output
    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    /**
     * The Public ip address associated with the Virtual Machine.
     */
    public PublicIpAddressResource getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(PublicIpAddressResource publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    /**
     * Private ip address associated with the Virtual Machine.
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The os for the Virtual Machine. Valid values are ``Linux`` or ``Windows``. (Required)
     */
    @Required
    @ValidStrings({"linux", "windows"})
    public String getOsType() {
        return osType != null ? osType.toLowerCase() : null;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    /**
     * The OS Disk to be attached to the Virtual Machine.
     */
    public DiskResource getOsDisk() {
        return osDisk;
    }

    public void setOsDisk(DiskResource osDisk) {
        this.osDisk = osDisk;
    }

    /**
     * Determines if the OS Disk should be deleted when the VM is terminated.
     */
    @Updatable
    public Boolean getDeleteOsDiskOnTerminate() {
        return deleteOsDiskOnTerminate == null
                ? deleteOsDiskOnTerminate = Boolean.FALSE
                : deleteOsDiskOnTerminate;
    }

    public void setDeleteOsDiskOnTerminate(Boolean deleteOsDiskOnTerminate) {
        this.deleteOsDiskOnTerminate = deleteOsDiskOnTerminate;
    }

    /**
     * The Data Disks to be attached to the Virtual Machine.
     */
    @Updatable
    public Set<DiskResource> getDataDisks() {
        return dataDisks == null
                ? dataDisks = new LinkedHashSet<>()
                : dataDisks;
    }

    public void setDataDisks(Set<DiskResource> dataDisks) {
        this.dataDisks = dataDisks;
    }

    /**
     * One of the subnet name from the assigned virtual network.
     */
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Type of Virtual Machine image. Defaults to specialized. Valid values are ``popular`` or ``specialized`` or ``latest`` or ``specific`` or ``custom`` or ``gallery``. (Required)
     */
    @Required
    @ValidStrings({"popular", "specialized", "latest", "specific", "custom", "gallery"})
    public String getVmImageType() {
        if (vmImageType == null) {
            vmImageType = "specialized";
        }

        return vmImageType;
    }

    public void setVmImageType(String vmImageType) {
        this.vmImageType = vmImageType;
    }

    /**
     * The ssh public key to be associated to the Virtual Machine.
     */
    public String getSsh() {
        return ssh;
    }

    public void setSsh(String ssh) {
        this.ssh = ssh;
    }

    /**
     * The id of a stored image to create the Virtual Machine.
     */
    public String getStoredImage() {
        return storedImage;
    }

    public void setStoredImage(String storedImage) {
        this.storedImage = storedImage;
    }

    /**
     * The id of a custom image to create the Virtual Machine.
     */
    public String getCustomImage() {
        return customImage;
    }

    public void setCustomImage(String customImage) {
        this.customImage = customImage;
    }

    /**
     * The version of a gallery image to create the Virtual Machine.
     */
    public String getGalleryImageVersion() {
        return galleryImageVersion;
    }

    public void setGalleryImageVersion(String galleryImageVersion) {
        this.galleryImageVersion = galleryImageVersion;
    }

    /**
     * The caching type for the Virtual Machine.
     */
    @Updatable
    public String getCachingType() {
        return cachingType;
    }

    public void setCachingType(String cachingType) {
        this.cachingType = cachingType;
    }

    /**
     * The data disk storage account type for the Virtual Machine. Valid values are ``STANDARD_LRS`` or ``PREMIUM_LRS`` or ``STANDARDSSD_LRS``.
     */
    @ValidStrings({"STANDARD_LRS", "PREMIUM_LRS", "STANDARDSSD_LRS"})
    @Updatable
    public String getStorageAccountTypeDataDisk() {
        return storageAccountTypeDataDisk;
    }

    public void setStorageAccountTypeDataDisk(String storageAccountTypeDataDisk) {
        this.storageAccountTypeDataDisk = storageAccountTypeDataDisk;
    }

    /**
     * The os disk storage account type for the Virtual Machine. Valid values are ``STANDARD_LRS`` or ``PREMIUM_LRS`` or ``STANDARDSSD_LRS``.
     */
    @ValidStrings({"STANDARD_LRS", "PREMIUM_LRS", "STANDARDSSD_LRS"})
    public String getStorageAccountTypeOsDisk() {
        return storageAccountTypeOsDisk;
    }

    public void setStorageAccountTypeOsDisk(String storageAccountTypeOsDisk) {
        this.storageAccountTypeOsDisk = storageAccountTypeOsDisk;
    }

    /**
     * The size of the Virtual Machine.
     */
    @Updatable
    public String getVmSizeType() {
        return vmSizeType != null ? vmSizeType.toUpperCase() : null;
    }

    public void setVmSizeType(String vmSizeType) {
        this.vmSizeType = vmSizeType;
    }

    /**
     * The known Virtual Machine image type.
     */
    public String getKnownVirtualImage() {
        return knownVirtualImage;
    }

    public void setKnownVirtualImage(String knownVirtualImage) {
        this.knownVirtualImage = knownVirtualImage;
    }

    /**
     * The time zone for the Virtual Machine.
     */
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * The publisher of the image to be used for creating the Virtual Machine. Required if ``vm-image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImagePublisher() {
        return imagePublisher;
    }

    public void setImagePublisher(String imagePublisher) {
        this.imagePublisher = imagePublisher;
    }

    /**
     * The offer of the image to be used for creating the Virtual Machine. Required if ``vm-image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImageOffer() {
        return imageOffer;
    }

    public void setImageOffer(String imageOffer) {
        this.imageOffer = imageOffer;
    }

    /**
     * The SKU of the image to be used for creating the Virtual Machine. Required if ``vm-image-type`` selected as ``latest`` or ``specific``.
     */
    public String getImageSku() {
        return imageSku;
    }

    public void setImageSku(String imageSku) {
        this.imageSku = imageSku;
    }

    /**
     * The region where the image resides to be used for creating the Virtual Machine. Required if ``vm-image-type`` selected as ``specific``.
     */
    public String getImageRegion() {
        return imageRegion;
    }

    public void setImageRegion(String imageRegion) {
        this.imageRegion = imageRegion;
    }

    /**
     * The version of the image to be used for creating the Virtual Machine. Required if ``vm-image-type`` selected as ``specific``.
     */
    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    /**
     * A list of secondary network interface that the Virtual Machine would use.
     */
    public Set<NetworkInterfaceResource> getSecondaryNetworkInterface() {
        if (secondaryNetworkInterface == null) {
            secondaryNetworkInterface = new HashSet<>();
        }

        return secondaryNetworkInterface;
    }

    public void setSecondaryNetworkInterface(Set<NetworkInterfaceResource> secondaryNetworkInterface) {
        this.secondaryNetworkInterface = secondaryNetworkInterface;
    }

    /**
     * Tags for the Virtual Machine.
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
     * The custom data for the Virtual Machine. Not supported if ``vm-image-type`` selected as ``specialized``.
     */
    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    /**
     * Enable system managed service identity for the Virtual Machine. Defaults to ``false``.
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
     * The principal id for the system managed service identity of the Virtual Machine.
     */
    @Output
    public String getSystemManagedServiceIdentityPrincipalId() {
        return SystemManagedServiceIdentityPrincipalId;
    }

    public void setSystemManagedServiceIdentityPrincipalId(String systemManagedServiceIdentityPrincipalId) {
        SystemManagedServiceIdentityPrincipalId = systemManagedServiceIdentityPrincipalId;
    }

    /**
     * A list of identities associated with the virtual machine.
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
     * The state of the virtual machine.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The location of the virtual machine.
     */
    @Output
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * The computer name of the virtual machine.
     */
    @Output
    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    /**
     * The public ip address ip of the virtual machine.
     */
    @Output
    public String getPublicIpAddressIp() {
        return publicIpAddressIp;
    }

    public void setPublicIpAddressIp(String publicIpAddressIp) {
        this.publicIpAddressIp = publicIpAddressIp;
    }

    /**
     * The launch date of the virtual machine.
     */
    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    @Override
    public void copyFrom(VirtualMachine virtualMachine) {
        setName(virtualMachine.name());
        setVmId(virtualMachine.vmId());
        setTags(virtualMachine.tags());
        setId(virtualMachine.id());
        setVmId(virtualMachine.vmId());

        setAvailabilitySet(virtualMachine.availabilitySetId() != null ? findById(AvailabilitySetResource.class, virtualMachine.availabilitySetId()) : null);
        setPublicIpAddress(virtualMachine.getPrimaryPublicIPAddressId() != null ? findById(PublicIpAddressResource.class, virtualMachine.getPrimaryPublicIPAddressId()) : null);
        setOsType(virtualMachine.osType().name());

        setNetworkInterface(
            findById(NetworkInterfaceResource.class,
                virtualMachine.inner().networkProfile()
                    .networkInterfaces().stream()
                    .filter(NetworkInterfaceReference::primary).findFirst()
                    .map(SubResource::id).orElse(null)
            )
        );
        setSecondaryNetworkInterface(
            virtualMachine.inner().networkProfile()
                .networkInterfaces().stream()
                .filter(o -> !o.primary())
                .map(o -> findById(NetworkInterfaceResource.class, o.id()))
                .collect(Collectors.toSet())
        );

        setVmSizeType(virtualMachine.inner().hardwareProfile().vmSize().toString());

        Set<DiskResource> dataDisks = new LinkedHashSet<>();
        Map<Integer, VirtualMachineDataDisk> dataDiskMap = virtualMachine.dataDisks();
        for (Map.Entry<Integer, VirtualMachineDataDisk> dataDiskEntry : dataDiskMap.entrySet()) {
            VirtualMachineDataDisk dataDisk = dataDiskEntry.getValue();

            DiskResource dataDiskResource = findById(DiskResource.class, dataDisk.id());
            if (dataDiskResource != null) {
                dataDisks.add(dataDiskResource);
            }
        }

        setDataDisks(dataDisks);

        setEnableSystemManagedServiceIdentity(!ObjectUtils.isBlank(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId()));
        setSystemManagedServiceIdentityPrincipalId(virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());

        getIdentities().clear();
        if (virtualMachine.userAssignedManagedServiceIdentityIds() != null) {
            getIdentities().addAll(
                virtualMachine.userAssignedManagedServiceIdentityIds()
                    .stream()
                    .map(o -> findById(IdentityResource.class, o))
                    .collect(Collectors.toSet())
            );
        }

        setState(virtualMachine.powerState().toString());
        setLocation(virtualMachine.inner().location());
        setComputerName(virtualMachine.computerName());
        setPublicIpAddressIp(virtualMachine.getPrimaryPublicIPAddress()!= null ? virtualMachine.getPrimaryPublicIPAddress().ipAddress() : null);
        InstanceViewStatus instanceViewStatus = virtualMachine.instanceView().statuses().stream().filter(o -> o.code().equals("ProvisioningState/succeeded")).findFirst().orElse(null);
        setLaunchDate(instanceViewStatus != null ? instanceViewStatus.time().toDate() : null);

        Azure client = createClient();
        setPrivateIpAddress(client.networkInterfaces().getById(getNetworkInterface().getId()).primaryPrivateIP());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        VirtualMachine virtualMachine = client.virtualMachines().getById(getId());

        if (virtualMachine == null) {
            return false;
        }

        copyFrom(virtualMachine);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        VirtualMachine virtualMachine = doVMFluentWorkflow(createClient()).create();
        setId(virtualMachine.id());
        setVmId(virtualMachine.vmId());
        copyFrom(virtualMachine);
    }

    /**
     * Executes Fluent Virtual Machine workflow. The workflow executes in the following order:
     * Configure Azure Region and Resource Groups
     * Configure VM network. Including Private IP and Public IP
     * Configure OS Disk
     * Configure Default Admin or Root Account
     * Configure Data Disks
     * Configure Generic Host attributes
     * @return {@link WithCreate} VM Definition object ready for creation
     */
    private WithCreate doVMFluentWorkflow(Azure client) {
        WithNetwork initialVMBuilder = configureRegionAndResourceGroups(client.virtualMachines().define(getName()));
        WithOS networkConfigured = configureNetwork(client, initialVMBuilder);
        WithCreate osConfiguredVMBuilder = configureOS(client, networkConfigured);

        if (osConfiguredVMBuilder == null) {
            throw new GyroException("Invalid config.");
        }

        if (!getSecondaryNetworkInterface().isEmpty()) {
            for (NetworkInterfaceResource nic : getSecondaryNetworkInterface()) {
                osConfiguredVMBuilder = osConfiguredVMBuilder.withExistingSecondaryNetworkInterface(
                    client.networkInterfaces().getByResourceGroup(getResourceGroup().getName(), nic.getName())
                );
            }
        }

        if (getAvailabilitySet() != null) {
            osConfiguredVMBuilder = osConfiguredVMBuilder.withExistingAvailabilitySet(client.availabilitySets().getByResourceGroup(getResourceGroup().getName(), getAvailabilitySet().getId()));
        }

        if (getEnableSystemManagedServiceIdentity()) {
            osConfiguredVMBuilder = osConfiguredVMBuilder.withSystemAssignedManagedServiceIdentity();
        }

        for (IdentityResource identity : getIdentities()) {
            osConfiguredVMBuilder = osConfiguredVMBuilder.withExistingUserAssignedManagedServiceIdentity(client.identities().getById(identity.getId()));
        }

        return osConfiguredVMBuilder
                .withSize(getVmSizeType())
                .withTags(getTags());
    }

    /**
     * First step in Fluent Virtual Machine workflow.
     * Configures Azure Region and Resource Groups
     * @return {@link WithNetwork} VM Definition object ready for Network configurations
     */
    private WithNetwork configureRegionAndResourceGroups(VirtualMachine.DefinitionStages.Blank initialVMBuilder) {
        return initialVMBuilder.withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroup().getName());
    }

    /**
     * Second step in Virtual Machine Fluent workflow.
     * Configures Network. Uses existing network interface if defined or
     * creates one with either a defined or generated private and public IP.
     * @return {@link WithOS} VM Definition object ready for OS configurations
     */
    private WithOS configureNetwork(Azure client, WithNetwork initialVMBuilder) {

        WithOS networkConfigured;

        if (!ObjectUtils.isBlank(getNetworkInterface())) {
            networkConfigured = initialVMBuilder.withExistingPrimaryNetworkInterface(
                    client.networkInterfaces().getByResourceGroup(
                            getResourceGroup().getName(), getNetworkInterface().getName()
                    ));
        } else {

            WithPrivateIP withPrivateIP = initialVMBuilder
                    .withExistingPrimaryNetwork(client.networks().getById(getNetwork().getId()))
                    .withSubnet(getSubnet());

            WithPublicIPAddress withPublicIpAddress;
            if (!ObjectUtils.isBlank(getPrivateIpAddress())) {
                withPublicIpAddress = withPrivateIP.withPrimaryPrivateIPAddressStatic(getPrivateIpAddress());
            } else {
                withPublicIpAddress = withPrivateIP.withPrimaryPrivateIPAddressDynamic();
            }

            if (!ObjectUtils.isBlank(getPublicIpAddress())) {
                networkConfigured = withPublicIpAddress.withExistingPrimaryPublicIPAddress(
                        client.publicIPAddresses().getByResourceGroup(getResourceGroup().getName(), getPublicIpAddress().getName())
                );
            } else {
                networkConfigured = withPublicIpAddress.withoutPrimaryPublicIPAddress();
            }
        }

        return networkConfigured;
    }

    /**
     * Entry point into Third through Fifth step in Virtual Machine Fluent workflow.
     * Splits workflow by OS.
     * Configures OS Disk, Admin User, and Data Disks
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureOS(Azure client, WithOS withOS) {
        switch(getOsType()) {
            case "linux":
                return configureLinux(client, withOS);
            case "windows":
                return configureWindows(client, withOS);
            default:
                throw new GyroException(String.format("OS Type [%s] is unsupported!", getOsType()));
        }
    }

    /**
     * Third step in Virtual Machine Fluent workflow. Splits workflow path by Image Type (OS Disk)
     * Configures OS Disk, Admin User, and Data Disks
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureLinux(Azure client, WithOS withOS) {
        switch (getVmImageType()) {
            case "custom":
                return configureLinuxManaged(
                        client,
                        withOS.withLinuxCustomImage(getCustomImage()));
            case "gallery":
                return configureLinuxManaged(
                        client,
                        withOS.withLinuxGalleryImageVersion(getGalleryImageVersion()));
            case "latest":
                return configureLinuxManagedOrUnmanaged(
                        client,
                        withOS.withLatestLinuxImage(getImagePublisher(), getImageOffer(), getImageSku()));
            case "popular":
                return configureLinuxManagedOrUnmanaged(
                        client,
                        withOS.withPopularLinuxImage(KnownLinuxVirtualMachineImage.valueOf(getKnownVirtualImage())));
            case "specific":
                return configureLinuxManagedOrUnmanaged(
                        client,
                        withOS.withSpecificLinuxImageVersion(
                                client.virtualMachineImages()
                                .getImage(getImageRegion(), getImagePublisher(), getImageOffer(), getImageSku(), getImageVersion())
                                .imageReference()));
            case "stored":
                return configureLinuxUnmanaged(
                        client,
                        withOS.withStoredLinuxImage(getStoredImage()));
            case "specialized":
                // Only Managed Disks are supported by Gyro currently
                WithManagedCreate specializedOsManagedConfigured = withOS.withSpecializedOSDisk(
                        client.disks().getById(getOsDisk().getId()), OperatingSystemTypes.LINUX);
                return configureManagedDataDisks(client, specializedOsManagedConfigured);
            default:
                throw new GyroException(String.format("Linux VM Image Type [%s] is Unsupported!", getVmImageType()));
        }
    }

    /**
     * Helper method in Virtual Machine workflow. Handles Managed Disk types.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureLinuxManaged(Azure client, WithLinuxRootUsernameManaged vmImageTypeConfigured) {
        return configureManagedDataDisks(client, configureLinuxAdmin(vmImageTypeConfigured).withCustomData(getEncodedCustomData()));
    }

    /**
     * Helper method in Virtual Machine workflow. Handles ManagedOrUnmanaged Disk types.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureLinuxManagedOrUnmanaged(Azure client, WithLinuxRootUsernameManagedOrUnmanaged vmImageTypeConfigured) {
        WithFromImageCreateOptionsManagedOrUnmanaged adminConfigured = configureLinuxAdmin(vmImageTypeConfigured);
        // Only managed disks are supported by Gyro currently.
        return configureManagedDataDisks(client, adminConfigured.withCustomData(getEncodedCustomData()));
    }

    /**
     * Helper method in Virtual Machine workflow. Handles Unmanaged Disk types.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureLinuxUnmanaged(Azure client, WithLinuxRootUsernameUnmanaged vmImageTypeConfigured) {
        return configureUnmanagedDataDisks(client, configureLinuxAdmin(vmImageTypeConfigured).withCustomData(getEncodedCustomData()));
    }

    /**
     * Fourth step in Virtual Machine Fluent workflow. Configures Admin User for Managed Disk types
     * @return {@link WithFromImageCreateOptionsManaged} VM Definition object ready for data disk configurations
     */
    private WithFromImageCreateOptionsManaged configureLinuxAdmin(WithLinuxRootUsernameManaged vmImageTypeConfigured) {
        WithLinuxCreateManaged adminConfigured = null;
        WithLinuxRootPasswordOrPublicKeyManaged rootUserConfigured = vmImageTypeConfigured.withRootUsername(getAdminUserName());
        if (!StringUtils.isBlank(getAdminPassword())) {
            adminConfigured = rootUserConfigured.withRootPassword(getAdminPassword());
        }

        if (!StringUtils.isBlank(getSsh())) {
            if (adminConfigured == null) {
                adminConfigured = rootUserConfigured.withSsh(getSsh());
            } else {
                adminConfigured = adminConfigured.withSsh(getSsh());
            }
        }

        return adminConfigured;
    }

    /**
     * Fourth step in Virtual Machine Fluent workflow. Configures Admin User for ManagedOrUnmanaged Disk types
     * @return {@link WithFromImageCreateOptionsManagedOrUnmanaged} VM Definition object ready for data disk configurations
     */
    private WithFromImageCreateOptionsManagedOrUnmanaged configureLinuxAdmin(WithLinuxRootUsernameManagedOrUnmanaged vmImageTypeConfigured) {
        WithLinuxCreateManagedOrUnmanaged adminConfigured = null;
        WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged rootUserConfigured = vmImageTypeConfigured.withRootUsername(getAdminUserName());
        if (!StringUtils.isBlank(getAdminPassword())) {
            adminConfigured = rootUserConfigured.withRootPassword(getAdminPassword());
        }

        if (!StringUtils.isBlank(getSsh())) {
            if (adminConfigured == null) {
                adminConfigured = rootUserConfigured.withSsh(getSsh());
            } else {
                adminConfigured = adminConfigured.withSsh(getSsh());
            }
        }

        return adminConfigured;
    }

    /**
     * Fourth step in Virtual Machine Fluent workflow. Configures Admin User for Unmanaged Disk types
     * @return {@link WithFromImageCreateOptionsUnmanaged} VM Definition object ready for data disk configurations
     */
    private WithFromImageCreateOptionsUnmanaged configureLinuxAdmin(WithLinuxRootUsernameUnmanaged vmImageTypeConfigured) {
        WithLinuxCreateUnmanaged adminConfigured = null;
        WithLinuxRootPasswordOrPublicKeyUnmanaged rootUserConfigured = vmImageTypeConfigured.withRootUsername(getAdminUserName());
        if (!StringUtils.isBlank(getAdminPassword())) {
            adminConfigured = rootUserConfigured.withRootPassword(getAdminPassword());
        }

        if (!StringUtils.isBlank(getSsh())) {
            if (adminConfigured == null) {
                adminConfigured = rootUserConfigured.withSsh(getSsh());
            } else {
                adminConfigured = adminConfigured.withSsh(getSsh());
            }
        }

        return adminConfigured;
    }

    /**
     * Third step in Virtual Machine Fluent workflow. Splits workflow path by Image Type (OS Disk)
     * Configures OS Disk, Admin User, and Data Disks
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureWindows(Azure client, WithOS withOS) {

        switch (getVmImageType()) {
            case "custom":
                return configureWindowsManaged(
                        client,
                        withOS.withWindowsCustomImage(getCustomImage()));
            case "gallery":
                return configureWindowsManaged(
                        client,
                        withOS.withWindowsGalleryImageVersion(getGalleryImageVersion()));
            case "latest":
                return configureWindowsManagedOrUnmanaged(
                        client,
                        withOS.withLatestWindowsImage(getImagePublisher(), getImageOffer(), getImageSku()));
            case "popular":
                return configureWindowsManagedOrUnmanaged(
                        client,
                        withOS.withPopularWindowsImage(KnownWindowsVirtualMachineImage.valueOf(getKnownVirtualImage())));
            case "specific":
                return configureWindowsManagedOrUnmanaged(
                        client,
                        withOS.withSpecificWindowsImageVersion(
                                client.virtualMachineImages()
                                .getImage(getImageRegion(), getImagePublisher(), getImageOffer(), getImageSku(), getImageVersion())
                                .imageReference()));
            case "stored":
                return configureWindowsUnmanaged(
                        client,
                        withOS.withStoredWindowsImage(getStoredImage()));
            case "specialized":
                // Only Managed Disks are supported by Gyro currently
                WithManagedCreate specializedOsManagedConfigured = withOS.withSpecializedOSDisk(
                        client.disks().getById(getOsDisk().getId()), OperatingSystemTypes.WINDOWS);
                return configureManagedDataDisks(client, specializedOsManagedConfigured);
            default:
                throw new GyroException(String.format("Windows VM Image Type [%s] is Unsupported!", getVmImageType()));
        }
    }

    /**
     * Helper method in Virtual Machine Fluent workflow. Handles Managed Disk types.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureWindowsManaged(Azure client, WithWindowsAdminUsernameManaged vmImageTypeConfigured) {
        WithWindowsCreateManaged adminConfigured = configureWindowsAdmin(vmImageTypeConfigured);
        return configureManagedDataDisks(
                client,
                adminConfigured.withoutAutoUpdate()
                        .withoutVMAgent()
                        .withTimeZone(getTimeZone())
                        .withCustomData(getEncodedCustomData()));
    }

    /**
     * Helper method in Virtual Machine Fluent workflow. Handles ManagedOrUnmanaged Disk types.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureWindowsManagedOrUnmanaged(Azure client, WithWindowsAdminUsernameManagedOrUnmanaged vmImageTypeConfigured) {
        WithWindowsCreateManagedOrUnmanaged adminConfigured = configureWindowsAdmin(vmImageTypeConfigured);

        // Only managed disks are supported by Gyro currently.
        return configureManagedDataDisks(
                client,
                adminConfigured.withoutAutoUpdate()
                        .withoutVMAgent()
                        .withTimeZone(getTimeZone())
                        .withCustomData(getEncodedCustomData()));
    }

    /**
     * Helper method in Virtual Machine Fluent workflow. Handles Unmanaged Disk types.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureWindowsUnmanaged(Azure client, WithWindowsAdminUsernameUnmanaged vmImageTypeConfigured) {
        return configureUnmanagedDataDisks(
                client,
                configureWindowsAdmin(vmImageTypeConfigured).withCustomData(getEncodedCustomData()));
    }

    /**
     * Fourth step in Virtual Machine Fluent workflow. Configures Admin User for Managed Disk types
     * @return {@link WithWindowsCreateManaged} VM Definition object ready for data disk configurations
     */
    private WithWindowsCreateManaged configureWindowsAdmin(WithWindowsAdminUsernameManaged vmImageTypeConfigured) {
        return vmImageTypeConfigured.withAdminUsername(getAdminUserName())
                .withAdminPassword(getAdminPassword());
    }

    /**
     * Fourth step in Virtual Machine Fluent workflow. Configures Admin User for ManagedOrUnmanaged Disk types
     * @return {@link WithWindowsCreateManagedOrUnmanaged} VM Definition object ready for data disk configurations
     */
    private WithWindowsCreateManagedOrUnmanaged configureWindowsAdmin(WithWindowsAdminUsernameManagedOrUnmanaged vmImageTypeConfigured) {
        return vmImageTypeConfigured.withAdminUsername(getAdminUserName())
                .withAdminPassword(getAdminPassword());
    }

    /**
     * Fourth step in Virtual Machine Fluent workflow. Configures Admin User for Unmanaged Disk types
     * @return {@link WithWindowsCreateUnmanaged} VM Definition object ready for data disk configurations
     */
    private WithWindowsCreateUnmanaged configureWindowsAdmin(WithWindowsAdminUsernameUnmanaged vmImageTypeConfigured) {
        return vmImageTypeConfigured.withAdminUsername(getAdminUserName())
                .withAdminPassword(getAdminPassword());
    }

    /**
     * Fifth step in Virtual Machine Fluent workflow.
     * Configures Managed Data Disks and Managed Data Disk defaults.
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureManagedDataDisks(Azure client, WithManagedCreate adminConfigured) {
        WithManagedCreate diskDefaultsConfigured = adminConfigured
                .withDataDiskDefaultCachingType(CachingTypes.fromString(getCachingType()))
                .withDataDiskDefaultStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeDataDisk()))
                .withOSDiskStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeOsDisk()));

        for (DiskResource diskResource : getDataDisks()) {
            Disk disk = client.disks().getById(diskResource.getId());
            if (disk != null) {
                diskDefaultsConfigured.withExistingDataDisk(disk);
            }
        }

        // Availability Zones?

        return diskDefaultsConfigured;
    }

    /**
     * Fifth step in Virtual Machine Fluent workflow. Configures Data disks
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private <T extends WithFromImageCreateOptionsManagedOrUnmanaged> WithCreate configureDataDisks(Azure client, T adminConfigured) {
        // Only managed disks are supported by Gyro currently.
        return configureManagedDataDisks(client, adminConfigured);
    }

    /**
     * Fifth step in Virtual Machine Fluent workflow. Configures Data disks
     * @return {@link WithCreate} VM Definition object ready for final generic configurations
     */
    private WithCreate configureUnmanagedDataDisks(Azure client, WithUnmanagedCreate adminConfigured) {
        // Only managed disks are supported by Gyro currently.
        if (!getDataDisks().isEmpty()) {
            throw new GyroException("Unmanaged Data Disks are currently not supported by Gyro");
        }

        return adminConfigured;
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        VirtualMachine virtualMachine = client.virtualMachines().getById(getId());

        VirtualMachine.Update update = virtualMachine.update()
            .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()))
            .withDataDiskDefaultCachingType(CachingTypes.fromString(getCachingType()))
            .withDataDiskDefaultStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeDataDisk()))
            .withTags(getTags());

        if (changedFieldNames.contains("data-disks")) {
            Map<String, Integer> currentDataDiskIdsToLun = new HashMap<>();
            for (Map.Entry<Integer, VirtualMachineDataDisk> dataDiskEntry : virtualMachine.dataDisks().entrySet()) {
                currentDataDiskIdsToLun.put(dataDiskEntry.getValue().id(), dataDiskEntry.getKey());
            }

            Set<String> wantedDataDiskIds = getDataDisks()
                    .stream()
                    .map(DiskResource::getId)
                    .filter(s -> client.disks().getById(s) != null)
                    .collect(Collectors.toSet());

            Set<String> diskIdsToRemove = new LinkedHashSet<>(currentDataDiskIdsToLun.keySet());
            diskIdsToRemove.removeAll(wantedDataDiskIds);
            for (String diskIdToRemove : diskIdsToRemove) {
                if (currentDataDiskIdsToLun.get(diskIdToRemove) != null) {
                    update.withoutDataDisk(currentDataDiskIdsToLun.get(diskIdToRemove));
                }
            }

            Set<String> disksIdsToAdd = new LinkedHashSet<>(wantedDataDiskIds);
            disksIdsToAdd.removeAll(currentDataDiskIdsToLun.keySet());
            for (String diskIdToAdd : disksIdsToAdd) {
                Disk diskToAdd = client.disks().getById(diskIdToAdd);
                if (diskIdToAdd != null) {
                    update.withExistingDataDisk(diskToAdd);
                }
            }
        }

        if (changedFieldNames.contains("enable-system-managed-service-identity")) {
            if (getEnableSystemManagedServiceIdentity()) {
                update = update.withSystemAssignedManagedServiceIdentity();
            } else {
                update = update.withoutSystemAssignedManagedServiceIdentity();
            }
        }

        if (changedFieldNames.contains("identities")) {
            for (IdentityResource identity : ((VirtualMachineResource) current).getIdentities()) {
                update = update.withoutUserAssignedManagedServiceIdentity(identity.getId());
            }

            for (IdentityResource identity : getIdentities()) {
                update = update.withExistingUserAssignedManagedServiceIdentity(client.identities().getById(identity.getId()));
            }
        }

        virtualMachine = update.apply();
        copyFrom(virtualMachine);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        VirtualMachine virtualMachine = client.virtualMachines().getById(getId());
        client.virtualMachines().deleteById(getId());

        if (getDeleteOsDiskOnTerminate()
                && virtualMachine != null
                && !"specialized".equals(getVmImageType())) {

            ui.write("\n Deleting Dynamically created OS Disk (%s)", virtualMachine.osDiskId());
            client.disks().deleteById(virtualMachine.osDiskId());
        } else {
            ui.write("\n OS Disk is controlled by Gyro. Skipping deletion of OS disk.");
        }
    }

    private String getEncodedCustomData() {
        return !ObjectUtils.isBlank(getCustomData()) ? Base64.getEncoder().encodeToString(getCustomData().getBytes()) : null;
    }

    private String getDecodedCustomData(String data) {
        return !ObjectUtils.isBlank(data) ? new String(Base64.getDecoder().decode(data.getBytes())) : null;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if ("custom".equals(getVmImageType())) {
            if (getCustomImage() == null) {
                errors.add(new ValidationError(this, "custom-image", "[custom-image] is required when using 'custom' [os-type]!"));
            }
        } else if ("gallery".equals(getVmImageType())) {
            if (getGalleryImageVersion() == null) {
                errors.add(new ValidationError(this, "gallery-image-version", "[gallery-image-version] is required when using 'gallery' [os-type]!"));
            }
        } else if ("latest".equals(getVmImageType())) {
            if (getImagePublisher() == null) {
                errors.add(new ValidationError(this, "image-publisher", "[image-publisher] is required when using 'latest' [os-type]!"));
            }
            if (getImageOffer() == null) {
                errors.add(new ValidationError(this, "image-offer", "[image-offer] is required when using 'latest' [os-type]!"));
            }
            if (getImageSku() == null) {
                errors.add(new ValidationError(this, "image-sku", "[image-sku] is required when using 'latest' [os-type]!"));
            }
        } else if ("popular".equals(getVmImageType())) {
            if (getKnownVirtualImage() == null) {
                errors.add(new ValidationError(this, "known-virtual-image", "[known-virtual-image] is required when using 'popular' [os-type]!"));
            }
        } else if ("specific".equals(getVmImageType())) {
            if (getImageRegion() == null) {
                errors.add(new ValidationError(this, "image-region", "[image-region] is required when using 'specific' [os-type]!"));
            }
            if (getImagePublisher() == null) {
                errors.add(new ValidationError(this, "image-publisher", "[image-publisher] is required when using 'specific' [os-type]!"));
            }
            if (getImageSku() == null) {
                errors.add(new ValidationError(this, "image-sku", "[image-sku] is required when using 'specific' [os-type]!"));
            }
            if (getImageVersion() == null) {
                errors.add(new ValidationError(this, "image-version", "[image-version] is required when using 'specific' [os-type]!"));
            }
        } else if ("stored".equals(getVmImageType())) {
            if (getStoredImage() == null) {
                errors.add(new ValidationError(this, "stored-image", "[stored-image] is required when using 'stored' [os-type]!"));
            }
        } else if ("specialized".equals(getVmImageType())) {
            if (getOsDisk() == null) {
                errors.add(new ValidationError(this, "os-disk", "[os-disk] is required when using 'specialized' [os-type]!"));
            }
        } else {
            errors.add(new ValidationError(this, "vm-image-type", "Unsupported [vm-image-type]!"));
        }

        return errors;
    }

    // -- GyroInstance Implementation

    @Override
    public String getGyroInstanceId() {
        return getId();
    }

    @Override
    public String getGyroInstanceState() {
        return getState();
    }

    @Override
    public String getGyroInstancePrivateIpAddress() {
        return getPrivateIpAddress();
    }

    @Override
    public String getGyroInstancePublicIpAddress() {
        return getPublicIpAddress() != null ? getPublicIpAddress().getIpAddress() : getPublicIpAddressIp();
    }

    @Override
    public String getGyroInstanceHostname() {
        return getGyroInstancePublicIpAddress() != null ? getGyroInstancePublicIpAddress() : getGyroInstancePrivateIpAddress();
    }

    @Override
    public String getGyroInstanceName() {
        return DiffableInternals.getName(this);
    }

    @Override
    public String getGyroInstanceLaunchDate() {
        return getLaunchDate() != null ? getLaunchDate().toString() : null;
    }

    @Override
    public String getGyroInstanceLocation() {
        return getLocation();
    }
}
