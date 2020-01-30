package gyro.azure.keyvault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
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
import gyro.core.validation.Required;

@Type("vault")
public class VaultResource extends AzureResource implements Copyable<Vault> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;
    private Set<VaultAccessPolicy> accessPolicy;
    private Boolean enableDeployment;
    private Boolean enableTemplateDeployment;
    private Boolean enableDiskEncryption;
    private String id;
    private String url;

    /**
     * The name of the vault. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the vault would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Tags for the vault.
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

    @Updatable
    public Set<VaultAccessPolicy> getAccessPolicy() {
        if (accessPolicy == null) {
            accessPolicy = new HashSet<>();
        }

        return accessPolicy;
    }

    public void setAccessPolicy(Set<VaultAccessPolicy> accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    @Updatable
    public Boolean getEnableDeployment() {
        if (enableDeployment == null) {
            enableDeployment = false;
        }

        return enableDeployment;
    }

    public void setEnableDeployment(Boolean enableDeployment) {
        this.enableDeployment = enableDeployment;
    }

    @Updatable
    public Boolean getEnableTemplateDeployment() {
        if (enableTemplateDeployment == null) {
            enableTemplateDeployment = false;
        }

        return enableTemplateDeployment;
    }

    public void setEnableTemplateDeployment(Boolean enableTemplateDeployment) {
        this.enableTemplateDeployment = enableTemplateDeployment;
    }

    @Updatable
    public Boolean getEnableDiskEncryption() {
        if (enableDiskEncryption == null) {
            enableDiskEncryption = false;
        }

        return enableDiskEncryption;
    }

    public void setEnableDiskEncryption(Boolean enableDiskEncryption) {
        this.enableDiskEncryption = enableDiskEncryption;
    }

    /**
     * The ID of the vault.
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
     * The URI of the vault.
     */
    @Output
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void copyFrom(Vault vault) {
        setName(vault.name());
        setId(vault.id());
        setUrl(vault.vaultUri());
        setResourceGroup(findById(ResourceGroupResource.class, vault.resourceGroupName()));
        setTags(vault.tags());

        getAccessPolicy().clear();
        if (vault.accessPolicies() != null) {
            setAccessPolicy(vault.accessPolicies().stream().map(o -> {
                VaultAccessPolicy accessPolicy = newSubresource(VaultAccessPolicy.class);
                accessPolicy.copyFrom(o);
                return accessPolicy;
            }).collect(Collectors.toSet()));
        }

        setEnableDeployment(vault.enabledForDeployment());
        setEnableDiskEncryption(vault.enabledForDiskEncryption());
        setEnableTemplateDeployment(vault.enabledForTemplateDeployment());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        Vault vault = client.vaults().getById(getId());

        if (vault == null) {
            return false;
        }

        copyFrom(vault);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        Vault.DefinitionStages.WithCreate withCreate = client.vaults().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName()).withEmptyAccessPolicy();

        if (!getAccessPolicy().isEmpty()) {
            for (VaultAccessPolicy accessPolicy : getAccessPolicy()) {
                withCreate = accessPolicy.createAccessPolicy(withCreate);
            }
        }

        if (!getTags().isEmpty()) {
            withCreate = withCreate.withTags(getTags());
        }

        if (getEnableDeployment()) {
            withCreate = withCreate.withDeploymentEnabled();
        }

        if (getEnableDiskEncryption()) {
            withCreate = withCreate.withDiskEncryptionEnabled();
        }

        if (getEnableTemplateDeployment()) {
            withCreate = withCreate.withTemplateDeploymentEnabled();
        }

        Vault vault = withCreate.create();

        setId(vault.id());
        setUrl(vault.vaultUri());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Azure client = createClient();

        Vault vault = client.vaults().getById(getId());

        Vault.Update update = vault.update();
        VaultResource currentVaultResource = (VaultResource) current;

        if (changedFieldNames.contains("access-policy")) {
            if (!currentVaultResource.getAccessPolicy().isEmpty()) {
                for (VaultAccessPolicy accessPolicy : currentVaultResource.getAccessPolicy()) {
                    update = update.withoutAccessPolicy(accessPolicy.getObjectId());
                }
            }

            if (!getAccessPolicy().isEmpty()) {
                for (VaultAccessPolicy accessPolicy : getAccessPolicy()) {
                    update = accessPolicy.createAccessPolicy(update);
                    update = accessPolicy.updateAccessPolicy(update);
                }
            }
        }

        if (changedFieldNames.contains("enable-deployment")) {
            update = getEnableDeployment() ? update.withDeploymentEnabled() : update.withDeploymentDisabled();
        }

        if (changedFieldNames.contains("enable-template-deployment")) {
            update = getEnableTemplateDeployment()
                ? update.withTemplateDeploymentEnabled()
                : update.withTemplateDeploymentDisabled();
        }

        if (changedFieldNames.contains("enable-disk-encryption")) {
            update = getEnableDiskEncryption()
                ? update.withDiskEncryptionEnabled()
                : update.withDiskEncryptionDisabled();
        }

        if (changedFieldNames.contains("tags")) {
            if (!currentVaultResource.getTags().isEmpty()) {
                for (String tag : currentVaultResource.getTags().keySet()) {
                    update.withoutTag(tag);
                }
            }

            if (!getTags().isEmpty()) {
                update = update.withTags(getTags());
            }
        }

        update.apply();

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        client.vaults().deleteById(getId());
    }
}
