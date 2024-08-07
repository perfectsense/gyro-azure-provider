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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.IpTag;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.psddev.dari.util.ObjectUtils;
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
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

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
 *
 *          tags: {
 *              Name: "public-ip-address-example"
 *          }
 *     end
 */
@Type("public-ip-address")
public class PublicIpAddressResource extends AzureResource implements Copyable<PublicIpAddress> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private SKU_TYPE skuType;
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
     * Name of the Public IP Address.
     */

    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which this would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Specify Sku type. Defaults to ``BASIC``.
     */
    @ValidStrings({ "BASIC", "STANDARD" })
    public SKU_TYPE getSkuType() {
        if (skuType == null) {
            skuType = SKU_TYPE.BASIC;
        }

        return skuType;
    }

    public void setSkuType(SKU_TYPE skuType) {
        this.skuType = skuType;
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
     * Specify the idle time in minutes before time out.
     */
    @Required
    @Range(min = 4, max = 30)
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
    public void copyFrom(PublicIpAddress publicIpAddress) {
        setIpAddress(publicIpAddress.ipAddress());
        setDomainLabel(publicIpAddress.leafDomainLabel());
        setIdleTimeoutInMinute(publicIpAddress.idleTimeoutInMinutes());
        setTags(publicIpAddress.tags());
        setId(publicIpAddress.id());
        setName(publicIpAddress.name());
        setIsDynamic(publicIpAddress.ipAllocationMethod().equals(IpAllocationMethod.DYNAMIC));
        setSkuType(publicIpAddress.sku().equals(PublicIPSkuType.BASIC) ? SKU_TYPE.BASIC : SKU_TYPE.STANDARD);
        setAvailabilityZoneIds(publicIpAddress.availabilityZones()
            .stream()
            .map(ExpandableStringEnum::toString)
            .collect(Collectors.toSet()));
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
        AzureResourceManager client = createClient(AzureResourceManager.class);

        PublicIpAddress publicIpAddress = client.publicIpAddresses().getById(getId());

        if (publicIpAddress == null) {
            return false;
        }

        copyFrom(publicIpAddress);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        PublicIpAddress.DefinitionStages.WithCreate withCreate = client.publicIpAddresses()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withSku(getSkuType() == SKU_TYPE.BASIC ? PublicIPSkuType.BASIC : PublicIPSkuType.STANDARD);

        if (!ObjectUtils.isBlank(getReverseFqdn())) {
            withCreate = withCreate.withReverseFqdn(getReverseFqdn());
        }

        for (String key : getIpTags().keySet()) {
            withCreate = withCreate.withIpTag(key, getIpTags().get(key));
        }

        for (String availabilityZoneId : getAvailabilityZoneIds()) {
            withCreate = withCreate.withAvailabilityZone(AvailabilityZoneId.fromString(availabilityZoneId));
        }

        if (getSkuType() == SKU_TYPE.BASIC) {
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

        PublicIpAddress publicIpAddress = withCreate.withTags(getTags()).create();

        copyFrom(publicIpAddress);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        PublicIpAddress publicIpAddress = client.publicIpAddresses().getById(getId());

        PublicIpAddress.Update update = publicIpAddress.update();

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
            PublicIpAddress response = update.apply();
            copyFrom(response);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);
        client.publicIpAddresses().deleteById(getId());
    }

    public enum SKU_TYPE {
        BASIC,
        STANDARD
    }
}
