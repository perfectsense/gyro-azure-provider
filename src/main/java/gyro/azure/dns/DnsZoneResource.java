package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.ZoneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a DNS Zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::dns-zone dns-zone-example-zones
 *         name: "zones.example.com"
 *         public-access: false
 *         resource-group-name: $(azure::resource-group resource-group-dns-zone-example | resource-group-name)
 *         tags: {
 *            Name: "resource-group-dns-zone-example"
 *         }
 *     end
 */
@ResourceType("dns-zone")
public class DnsZoneResource extends AzureResource {

    private String id;
    private Boolean publicAccess;
    private String name;
    private List<String> registrationVirtualNetworkIds;
    private List<String> resolutionVirtualNetworkIds;
    private String resourceGroupName;
    private Map<String, String> tags;

    /**
     * The id of the dns zone.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Determines if the dns zone is public or private. Defaults to public (true). (Optional)
     */
    public Boolean getPublicAccess() {
        if (publicAccess == null) {
            publicAccess = true;
        }

        return publicAccess;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * The name of the dns zone. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of virtual network id's that register hostnames in a private dns zone.
     * Can be used when the access is private. (Optional)
     */
    @Updatable
    public List<String> getRegistrationVirtualNetworkIds() {
        if (registrationVirtualNetworkIds == null) {
            registrationVirtualNetworkIds = new ArrayList<>();
        }

        return registrationVirtualNetworkIds;
    }

    public void setRegistrationVirtualNetworkIds(List<String> registrationVirtualNetworkIds) {
        this.registrationVirtualNetworkIds = registrationVirtualNetworkIds;
    }

    /**
     * A list of virtual network id's that resolve records in a private dns zone.
     * Can be used when the access is private. (Optional)
     */
    @Updatable
    public List<String> getResolutionVirtualNetworkIds() {
        if (resolutionVirtualNetworkIds == null) {
            resolutionVirtualNetworkIds = new ArrayList<>();
        }

        return resolutionVirtualNetworkIds;
    }

    public void setResolutionVirtualNetworkIds(List<String> resolutionVirtualNetworkIds) {
        this.resolutionVirtualNetworkIds = resolutionVirtualNetworkIds;
    }

    /**
     * The name of the resource group where the dns zone is found. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The tags associated with the dns zone. (Optional)
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

    @Override
    public boolean refresh() {
        Azure client = createClient();

        DnsZone dnsZone = client.dnsZones().getById(getId());

        if (dnsZone == null) {
            return false;
        }

        setId(dnsZone.id());
        setPublicAccess(dnsZone.accessType() == ZoneType.PUBLIC);
        setName(dnsZone.name());
        setRegistrationVirtualNetworkIds(dnsZone.registrationVirtualNetworkIds());
        setResolutionVirtualNetworkIds(dnsZone.resolutionVirtualNetworkIds());
        setResourceGroupName(dnsZone.resourceGroupName());
        setTags(dnsZone.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        DnsZone.DefinitionStages.WithCreate withCreate;

        withCreate = client.dnsZones().define(getName()).withExistingResourceGroup(getResourceGroupName());

        if (getPublicAccess() != null && !getPublicAccess()) {
            if (getRegistrationVirtualNetworkIds().isEmpty() && getResolutionVirtualNetworkIds().isEmpty()) {
                withCreate.withPrivateAccess(getRegistrationVirtualNetworkIds(), getResolutionVirtualNetworkIds());
            } else {
                withCreate.withPrivateAccess();
            }
        } else {
            withCreate.withPublicAccess();
        }

        withCreate.withTags(getTags());

        DnsZone dnsZone = withCreate.create();

        setId(dnsZone.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsZone.Update update = client.dnsZones().getById(getId()).update();

        update.withTags(getTags());
        update.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.dnsZones().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "dns zone " + getName();
    }
}
