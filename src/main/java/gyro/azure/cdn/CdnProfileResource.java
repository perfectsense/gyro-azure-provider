package gyro.azure.cdn;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CdnProfile.DefinitionStages.WithSku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a cdn profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         azure::cdn-profile cdn-profile-example
 *             name: "cdn-profile-example"
 *             resource-group-name: $(azure::resource-group resource-group-cdn-profile-example | resource-group-name)
 *             sku: "Standard_Akamai"
 *             tags: {
 *                 Name: "cdn-profile-example"
 *             }
 *         end
 */
@Type("cdn-profile")
public class CdnProfileResource extends AzureResource {

    private String id;
    private String name;
    private String resourceGroupName;
    private String sku;
    private Map<String, String> tags;

    /**
     * The id of the profile.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the profile. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group where the profile is found. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The sku of the profile. Valid values are ``Premium_Verizon``, ``Standard_Verizon``, ``Standard_Akamai``. (Required)
     */
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * The tags associated with the profile. (Optional)
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

        CdnProfile cdnProfile = client.cdnProfiles().getById(getId());

        if (cdnProfile == null) {
            return false;
        }

        setId(cdnProfile.id());
        setName(cdnProfile.name());
        setResourceGroupName(cdnProfile.resourceGroupName());
        setSku(cdnProfile.sku().name().toString());
        setTags(cdnProfile.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        WithSku withSku = client.cdnProfiles().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());

        CdnProfile cdnProfile = null;
        if ("Premium_Verizon".equalsIgnoreCase(getSku())) {
            cdnProfile = withSku.withPremiumVerizonSku().withTags(getTags()).create();
        } else if ("Standard_Verizon".equalsIgnoreCase(getSku())) {
            cdnProfile = withSku.withStandardVerizonSku().withTags(getTags()).create();
        } else if ("Standard_Akamai".equalsIgnoreCase(getSku())) {
            cdnProfile = withSku.withStandardAkamaiSku().withTags(getTags()).create();
        } else {
            throw new GyroException("Invalid sku. Valid values are Premium_Verizon, " +
                    "Standard_Verizon, Standard_Akamai.");
        }

        setId(cdnProfile.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        CdnProfile.Update update = client.cdnProfiles().getById(getId()).update().withTags(getTags());
        update.apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.cdnProfiles().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "cdn profile " + getName();
    }
}
