package gyro.azure.compute;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxCreateManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxCreateUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithManagedCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithFromImageCreateOptionsManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithNetwork;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithOS;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithPrivateIP;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithPublicIPAddress;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsAdminPasswordManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsCreateManaged;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithWindowsCreateUnmanaged;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.network.NetworkInterfaceResource;
import gyro.azure.network.NetworkResource;
import gyro.azure.network.PublicIpAddressResource;
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

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
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
public class VirtualMachineResource extends AzureResource implements Copyable<VirtualMachine> {
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
     * The Data Disks to be attached to the Virtual Machine.
     */
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
     * The data disk storage account type for the Virtual Machine. Valid values are ``STANDARD_LRS`` or ``PREMIUM_LRS`` or ``STANDARD_SSD_LRS``.
     */
    @ValidStrings({"STANDARD_LRS", "PREMIUM_LRS", "STANDARD_SSD_LRS"})
    @Updatable
    public String getStorageAccountTypeDataDisk() {
        return storageAccountTypeDataDisk;
    }

    public void setStorageAccountTypeDataDisk(String storageAccountTypeDataDisk) {
        this.storageAccountTypeDataDisk = storageAccountTypeDataDisk;
    }

    /**
     * The os disk storage account type for the Virtual Machine. Valid values are ``STANDARD_LRS`` or ``PREMIUM_LRS`` or ``STANDARD_SSD_LRS``.
     */
    @ValidStrings({"STANDARD_LRS", "PREMIUM_LRS", "STANDARD_SSD_LRS"})
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
        Azure client = createClient();

        WithNetwork withNetwork = client.virtualMachines().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        WithOS withOS;

        if (!ObjectUtils.isBlank(getNetworkInterface())) {
            withOS = withNetwork.withExistingPrimaryNetworkInterface(
                    client.networkInterfaces().getByResourceGroup(
                        getResourceGroup().getName(), getNetworkInterface().getName()
                    ));
        } else {

            WithPrivateIP withPrivateIP = withNetwork
                .withExistingPrimaryNetwork(client.networks().getById(getNetwork().getId()))
                .withSubnet(getSubnet());

            WithPublicIPAddress withPublicIpAddress;
            if (!ObjectUtils.isBlank(getPrivateIpAddress())) {
                withPublicIpAddress = withPrivateIP.withPrimaryPrivateIPAddressStatic(getPrivateIpAddress());
            } else {
                withPublicIpAddress = withPrivateIP.withPrimaryPrivateIPAddressDynamic();
            }

            if (!ObjectUtils.isBlank(getPublicIpAddress())) {
                withOS = withPublicIpAddress.withExistingPrimaryPublicIPAddress(
                    client.publicIPAddresses().getByResourceGroup(getResourceGroup().getName(), getPublicIpAddress().getName())
                );
            } else {
                withOS = withPublicIpAddress.withoutPrimaryPublicIPAddress();
            }
        }

        WithCreate create = null;
        WithManagedCreate managedCreate = null;
        WithFromImageCreateOptionsManaged withFromImageCreateOptionsManaged = null;

        boolean isLatestPopularOrSpecific = getVmImageType().equals("latest")
            || getVmImageType().equals("popular")
            || getVmImageType().equals("specific");

        if (getOsType().equals("linux")) {
            //linux

            WithLinuxCreateUnmanaged createUnmanaged = null;
            WithLinuxCreateManaged createManaged = null;

            if (isLatestPopularOrSpecific) {
                WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged managedOrUnmanaged;

                if (getVmImageType().equals("latest")) {
                    managedOrUnmanaged = withOS.withLatestLinuxImage(getImagePublisher(),getImageOffer(),getImageSku())
                        .withRootUsername(getAdminUserName());
                } else if (getVmImageType().equals("popular")) {
                    managedOrUnmanaged = withOS.withPopularLinuxImage(
                        KnownLinuxVirtualMachineImage.valueOf(getKnownVirtualImage())
                    ).withRootUsername(getAdminUserName());
                } else {
                    managedOrUnmanaged = withOS.withSpecificLinuxImageVersion(
                        client.virtualMachineImages()
                            .getImage(getImageRegion(),getImagePublisher(),getImageOffer(),getImageSku(),getImageVersion())
                            .imageReference()
                    ).withRootUsername(getAdminUserName());
                }

                if (!ObjectUtils.isBlank(getAdminPassword()) && !ObjectUtils.isBlank(getSsh())) {
                    withFromImageCreateOptionsManaged = managedOrUnmanaged.withRootPassword(getAdminPassword()).withSsh(getSsh())
                        .withCustomData(getEncodedCustomData());
                } else if (!ObjectUtils.isBlank(getAdminPassword())) {
                    withFromImageCreateOptionsManaged = managedOrUnmanaged.withRootPassword(getAdminPassword())
                        .withCustomData(getEncodedCustomData());
                } else {
                    withFromImageCreateOptionsManaged = managedOrUnmanaged.withSsh(getSsh())
                        .withCustomData(getEncodedCustomData());
                }

            } else if (getVmImageType().equals("stored")) {
                WithLinuxRootPasswordOrPublicKeyUnmanaged publicKeyUnmanaged = withOS
                    .withStoredLinuxImage(getStoredImage())
                    .withRootUsername(getAdminUserName());

                if (!ObjectUtils.isBlank(getAdminPassword()) && !ObjectUtils.isBlank(getSsh())) {
                    createUnmanaged = publicKeyUnmanaged.withRootPassword(getAdminPassword()).withSsh(getSsh());
                } else if (!ObjectUtils.isBlank(getAdminPassword())) {
                    createUnmanaged = publicKeyUnmanaged.withRootPassword(getAdminPassword());
                } else {
                    createUnmanaged = publicKeyUnmanaged.withSsh(getSsh());
                }

            } else if (getVmImageType().equals("custom") || getVmImageType().equals("gallery")) {
                WithLinuxRootPasswordOrPublicKeyManaged publicKeyManaged;

                if (getVmImageType().equals("custom")) {
                    publicKeyManaged = withOS.withLinuxCustomImage(getCustomImage())
                        .withRootUsername(getAdminUserName());
                } else {
                    publicKeyManaged = withOS.withLinuxGalleryImageVersion(getGalleryImageVersion())
                        .withRootUsername(getAdminUserName());
                }

                if (!ObjectUtils.isBlank(getAdminPassword()) && !ObjectUtils.isBlank(getSsh())) {
                    createManaged = publicKeyManaged.withRootPassword(getAdminPassword()).withSsh(getSsh());
                } else if (!ObjectUtils.isBlank(getAdminPassword())) {
                    createManaged = publicKeyManaged.withRootPassword(getAdminPassword());
                } else {
                    createManaged = publicKeyManaged.withSsh(getSsh());
                }

            } else {
                managedCreate = withOS.withSpecializedOSDisk(
                    client.disks().getById(getOsDisk().getId()), OperatingSystemTypes.LINUX
                );
            }

            if (createUnmanaged != null) {
                create = createUnmanaged.withCustomData(getEncodedCustomData())
                    .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()));
            } else if (createManaged != null) {
                create = createManaged.withCustomData(getEncodedCustomData())
                    .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()));
            }
        } else {
            //windows
            WithWindowsCreateUnmanaged createUnmanaged = null;
            WithWindowsCreateManaged createManaged = null;
            if (isLatestPopularOrSpecific) {
                VirtualMachine.DefinitionStages.WithWindowsAdminPasswordManagedOrUnmanaged managedOrUnmanaged;

                if (getVmImageType().equals("latest")) {
                    managedOrUnmanaged = withOS.withLatestWindowsImage(getImagePublisher(),getImageOffer(),getImageSku())
                        .withAdminUsername(getAdminUserName());
                } else if (getVmImageType().equals("popular")) {
                    managedOrUnmanaged = withOS.withPopularWindowsImage(
                        KnownWindowsVirtualMachineImage.valueOf(getKnownVirtualImage())
                    ).withAdminUsername(getAdminUserName());
                } else {
                    managedOrUnmanaged = withOS.withSpecificWindowsImageVersion(
                        client.virtualMachineImages()
                            .getImage(getImageRegion(),getImagePublisher(),getImageOffer(),getImageSku(),getImageVersion())
                            .imageReference()
                    ).withAdminUsername(getAdminUserName());
                }

                managedCreate = managedOrUnmanaged.withAdminPassword(getAdminPassword())
                    .withCustomData(getEncodedCustomData())
                    .withExistingDataDisk(client.disks().getById(getOsDisk().getId()));

            } else if (getVmImageType().equals("stored")) {
                createUnmanaged = withOS.withStoredWindowsImage(getStoredImage())
                    .withAdminUsername(getAdminUserName()).withAdminPassword(getAdminPassword());

            } else if (getVmImageType().equals("custom") || getVmImageType().equals("gallery")) {
                WithWindowsAdminPasswordManaged passwordManaged;
                if (getVmImageType().equals("custom")) {
                    passwordManaged = withOS.withWindowsCustomImage(getCustomImage())
                        .withAdminUsername(getAdminUserName());
                } else {
                    passwordManaged = withOS.withWindowsGalleryImageVersion(getGalleryImageVersion())
                        .withAdminUsername(getAdminUserName());
                }

                createManaged = passwordManaged.withAdminPassword(getAdminPassword());
            } else {
                managedCreate = withOS.withSpecializedOSDisk(
                    client.disks().getById(getOsDisk().getId()), OperatingSystemTypes.WINDOWS
                );
            }

            if (createUnmanaged != null) {
                create = createUnmanaged
                    .withoutAutoUpdate()
                    .withoutVMAgent()
                    .withTimeZone(getTimeZone())
                    .withCustomData(getEncodedCustomData())
                    .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()));
            } else if (createManaged != null) {
                create = createManaged
                    .withoutAutoUpdate()
                    .withoutVMAgent()
                    .withTimeZone(getTimeZone())
                    .withCustomData(getEncodedCustomData())
                    .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()));
            }
        }

        if (managedCreate != null) {
            create = managedCreate.withDataDiskDefaultCachingType(CachingTypes.fromString(getCachingType()))
                .withDataDiskDefaultStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeDataDisk()))
                .withOSDiskStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeOsDisk()))
                .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()));
        } else if (withFromImageCreateOptionsManaged != null) {
            create = withFromImageCreateOptionsManaged.withDataDiskDefaultCachingType(CachingTypes.fromString(getCachingType()))
                .withDataDiskDefaultStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeDataDisk()))
                .withOSDiskStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeOsDisk()))
                .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()));
        }

        if (create == null) {
            throw new GyroException("Invalid config.");
        }

        if (!getSecondaryNetworkInterface().isEmpty()) {
            for (NetworkInterfaceResource nic : getSecondaryNetworkInterface()) {
                create = create.withExistingSecondaryNetworkInterface(
                    client.networkInterfaces().getByResourceGroup(getResourceGroup().getName(), nic.getName())
                );
            }
        }

        if (getAvailabilitySet() != null) {
            create.withExistingAvailabilitySet(client.availabilitySets().getByResourceGroup(getResourceGroup().getName(), getAvailabilitySet().getId()));
        }

        VirtualMachine virtualMachine = create.withTags(getTags()).create();

        setId(virtualMachine.id());
        setVmId(virtualMachine.vmId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        VirtualMachine virtualMachine = client.virtualMachines().getById(getId());

        virtualMachine.update()
            .withSize(VirtualMachineSizeTypes.fromString(getVmSizeType()))
            .withDataDiskDefaultCachingType(CachingTypes.fromString(getCachingType()))
            .withDataDiskDefaultStorageAccountType(StorageAccountTypes.fromString(getStorageAccountTypeDataDisk()))
            .withTags(getTags())
            .apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.virtualMachines().deleteById(getId());
    }

    private String getEncodedCustomData() {
        return !ObjectUtils.isBlank(getCustomData()) ? Base64.getEncoder().encodeToString(getCustomData().getBytes()) : null;
    }

    private String getDecodedCustomData(String data) {
        return !ObjectUtils.isBlank(data) ? new String(Base64.getDecoder().decode(data.getBytes())) : null;
    }
}
