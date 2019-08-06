package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IpTag;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddress.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.PublicIPSkuType;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a public ip address.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::public-ip-address public-ip-address-example
 *          name: "public-ip-address-example"
 *          resource-group: $(azure::resource-group resource-group-public-ip-address-example)
 *          idle-timeout-in-minute: 4
 *          is-sku-basic: false
 *
 *          tags: {
 *              Name: "public-ip-address-example"
 *          }
 *     end
 */
@Type("public-ip-address")
public class PublicIpAddressResource extends AzureResource implements Copyable<PublicIPAddress> {
    private String name;
    private ResourceGroupResource resourceGroup;
    private Boolean isSkuBasic;
    private Boolean isDynamic;
    private Integer idleTimeoutInMinute;
    private String id;
    private String ipAddress;
    private Set<String> availabilityZoneIds;
    private String domainLabel;
    private Map<String, String> tags;
    private Map<String, String> ipTags;
    private String reverseFqdn;
    private String fqdn;
    private Boolean hasAssignedLoadBalancer;
    private Boolean hasAssignedNetworkInterface;
    private String version;

    /**
     * Name of the Public IP Address. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which this would reside. (Required)
     */
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Specify if Sku type is basic or standard. Defaults to ``true``.
     */
    public Boolean getIsSkuBasic() {
        if (isSkuBasic == null) {
            isSkuBasic = true;
        }

        return isSkuBasic;
    }

    public void setIsSkuBasic(Boolean isSkuBasic) {
        this.isSkuBasic = isSkuBasic;
    }

    /**
     * Specifies if the Public IP Address is using Dynamic IP or Static IP.
     */
    @Output
    public Boolean getIsDynamic() {
        return isDynamic;
    }

    public void setIsDynamic(Boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    /**
     * Specify the idle time in minutes before time out. Valid values are any Integer between ``4`` and ``30``. (Required)
     */
    @Updatable
    public Integer getIdleTimeoutInMinute() {
        return idleTimeoutInMinute;
    }

    public void setIdleTimeoutInMinute(Integer idleTimeoutInMinute) {
        this.idleTimeoutInMinute = idleTimeoutInMinute;
    }

    /**
     * The ID of the Public IP Address.
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
     * The IP of the Public IP Address.
     */
    @Output
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The availability zone of the Public IP Address.
     */
    public Set<String> getAvailabilityZoneIds() {
        if (availabilityZoneIds == null) {
            availabilityZoneIds = new HashSet<>();
        }

        return availabilityZoneIds;
    }

    public void setAvailabilityZoneIds(Set<String> availabilityZoneIds) {
        this.availabilityZoneIds = availabilityZoneIds;
    }

    /**
     * The domain prefix of the Public IP Address.
     */
    @Updatable
    public String getDomainLabel() {
        return domainLabel;
    }

    public void setDomainLabel(String domainLabel) {
        this.domainLabel = domainLabel;
    }

    /**
     * The tags for the Public IP Address.
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
     * A set of IP tags for the Public IP Address.
     */
    @Updatable
    public Map<String, String> getIpTags() {
        if (ipTags == null) {
            ipTags = new HashMap<>();
        }

        return ipTags;
    }

    public void setIpTags(Map<String, String> ipTags) {
        this.ipTags = ipTags;
    }

    /**
     * The reverse FQDN for the Public IP Address.
     */
    @Updatable
    public String getReverseFqdn() {
        return reverseFqdn;
    }

    public void setReverseFqdn(String reverseFqdn) {
        this.reverseFqdn = reverseFqdn;
    }

    /**
     * The FQDN for the Public IP Address.
     */
    @Output
    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    /**
     * Specifies if the Public IP Address is associated with any Load Balancer.
     */
    @Output
    public Boolean getHasAssignedLoadBalancer() {
        return hasAssignedLoadBalancer;
    }

    public void setHasAssignedLoadBalancer(Boolean hasAssignedLoadBalancer) {
        this.hasAssignedLoadBalancer = hasAssignedLoadBalancer;
    }

    /**
     * Specifies if the Public IP Address is associated with any Network Interface.
     */
    @Output
    public Boolean getHasAssignedNetworkInterface() {
        return hasAssignedNetworkInterface;
    }

    public void setHasAssignedNetworkInterface(Boolean hasAssignedNetworkInterface) {
        this.hasAssignedNetworkInterface = hasAssignedNetworkInterface;
    }

    /**
     * The Public IP Address version being IPV4 or IPV6.
     */
    @Output
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void copyFrom(PublicIPAddress publicIpAddress) {
        setIpAddress(publicIpAddress.ipAddress());
        setDomainLabel(publicIpAddress.leafDomainLabel());
        setIdleTimeoutInMinute(publicIpAddress.idleTimeoutInMinutes());
        setTags(publicIpAddress.tags());
        setId(publicIpAddress.id());
        setName(publicIpAddress.name());
        setIsDynamic(publicIpAddress.ipAllocationMethod().equals(IPAllocationMethod.DYNAMIC));
        setIsSkuBasic(publicIpAddress.sku().equals(PublicIPSkuType.BASIC));
        setAvailabilityZoneIds(publicIpAddress.availabilityZones().stream().map(ExpandableStringEnum::toString).collect(Collectors.toSet()));
        setResourceGroup(findById(ResourceGroupResource.class, publicIpAddress.resourceGroupName()));
        setFqdn(publicIpAddress.fqdn());
        setHasAssignedLoadBalancer(publicIpAddress.hasAssignedLoadBalancer());
        setHasAssignedNetworkInterface(publicIpAddress.hasAssignedNetworkInterface());
        setReverseFqdn(publicIpAddress.reverseFqdn());
        setVersion(publicIpAddress.version().toString());
        setIpTags(publicIpAddress.ipTags().stream().collect(Collectors.toMap(IpTag::tag, IpTag::ipTagType)));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        PublicIPAddress publicIpAddress = client.publicIPAddresses().getById(getId());

        if (publicIpAddress == null) {
            return false;
        }

        copyFrom(publicIpAddress);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        WithCreate withCreate = client.publicIPAddresses()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withSku(getIsSkuBasic() ? PublicIPSkuType.BASIC : PublicIPSkuType.STANDARD);

        if (!ObjectUtils.isBlank(getReverseFqdn())) {
            withCreate = withCreate.withReverseFqdn(getReverseFqdn());
        }

        for (String key : getIpTags().keySet()) {
            withCreate = withCreate.withIpTag(key, getIpTags().get(key));
        }

        for (String availabilityZoneId : getAvailabilityZoneIds()) {
            withCreate = withCreate.withAvailabilityZone(AvailabilityZoneId.fromString(availabilityZoneId));
        }

        if (getIsSkuBasic()) {
            //basic
            withCreate = withCreate.withIdleTimeoutInMinutes(getIdleTimeoutInMinute());

            if (!ObjectUtils.isBlank(getDomainLabel())) {
                withCreate = withCreate.withLeafDomainLabel(getDomainLabel());
            } else {
                withCreate = withCreate.withoutLeafDomainLabel();
            }
        } else {
            //standard
            withCreate = withCreate.withStaticIP()
                .withIdleTimeoutInMinutes(getIdleTimeoutInMinute());

            if (!ObjectUtils.isBlank(getDomainLabel())) {
                withCreate = withCreate.withLeafDomainLabel(getDomainLabel());
            } else {
                withCreate = withCreate.withoutLeafDomainLabel();
            }
        }

        PublicIPAddress publicIpAddress = withCreate.withTags(getTags()).create();

        copyFrom(publicIpAddress);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        PublicIPAddress publicIpAddress = client.publicIPAddresses().getById(getId());

        PublicIPAddress.Update update = publicIpAddress.update();

        if (changedFieldNames.contains("idle-timeout-in-minute")) {
            update = update.withIdleTimeoutInMinutes(getIdleTimeoutInMinute());
        }

        if (changedFieldNames.contains("tags")) {
            update = update.withTags(getTags());
        }

        if (changedFieldNames.contains("domain-label")) {
            update = ObjectUtils.isBlank(getDomainLabel())
                ? update.withoutLeafDomainLabel() : update.withLeafDomainLabel(getDomainLabel());
        }

        if (changedFieldNames.contains("reverse-fqdn")) {
            update = ObjectUtils.isBlank(getReverseFqdn())
                ? update.withoutReverseFqdn() : update.withReverseFqdn(getReverseFqdn());
        }

        if (changedFieldNames.contains("ip-tags")) {
            PublicIpAddressResource publicIpAddressResource = (PublicIpAddressResource) current;
            for (String ipTag : publicIpAddressResource.getIpTags().keySet()) {
                update = update.withoutIpTag(ipTag);
            }

            for (String key : getIpTags().keySet()) {
                update = update.withIpTag(key, getIpTags().get(key));
            }
        }

        if (!changedFieldNames.isEmpty()) {
            PublicIPAddress response = update.apply();
            copyFrom(response);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();
        client.publicIPAddresses().deleteById(getId());
    }
}
