/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.azure.keyvault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.Sku;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

/**
 *
 * Azure Key Vaults are managed using a combination of resource configuration and commands. Currently
 * ``azure::key-vault-certificate`` and ``azure::key-vault-secret`` are the entities that support commands.
 *
 * Create an Azure Key Vault using the ``azure::key-vault`` resource. After the Key Vault is
 * created use the ``gyro azure key-vault`` command to manage certificates and secrets within the key vault.
 * See documentation below on how to create, add, or remove a certificate or secret from a key vault.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::key-vault vault-example
 *         resource-group: $(azure::resource-group resource-group-example-vault)
 *
 *         name: "key-vault-example"
 *         enable-deployment: true
 *         enable-template-deployment: true
 *         enable-disk-encryption: true
 *         enable-soft-delete: false
 *
 *         access-policy
 *             key-permissions: ["get", "import", "list", "delete"]
 *             certificate-permissions: ['get', 'update', 'create', 'list', 'delete', 'import', 'backup', 'recover', 'restore', 'setissuers', 'deleteissuers', 'purge', 'listissuers', 'getissuers', 'managecontacts', 'manageissuers']
 *             secret-permissions: ["get"]
 *             object-id: "e0afa33f-9882-4cdc-abb8-c613a8949f9e"
 *         end
 *
 *         access-policy
 *             key-permissions: ["get", "import", "list", "delete"]
 *             certificate-permissions: ['get', 'update', 'create', 'list', 'delete', 'import', 'backup', 'recover', 'restore', 'setissuers', 'deleteissuers', 'purge', 'listissuers', 'getissuers', 'managecontacts', 'manageissuers']
 *             secret-permissions: ["get"]
 *             object-id: "b7d674a2-7e88-47af-b772-5d90b4bf965a"
 *         end
 *
 *         tags: {
 *             Name: "key-vault-examples"
 *         }
 *     end
 *
 * Certificate Commands
 * --------------------
 *
 * The following set of commands allow you to manage certificates in a key vault. Before using these commands
 * you must have already created an ``azure::key-vault``. The key vault must be managed by Gyro. Ensure a proper
 * access policy is added to the key vault for the service principal you are using.
 *
 * **Add Certificate**
 *
 * Adds a certificate to a key vault using your certificate file (.pfx). Access policy needed (Upload, Insert).
 *
 * .. code::
 *
 *     gyro azure key-vault add-certificate <vault-name> <cert-name> <path> --password <password>
 *
 * - ``vault-name`` - The name of the key-vault resource defined in your config where you want to create your certificate.
 * - ``cert-name`` - The name of the certificate that you want to create when you import the certificate file.
 * - ``cert-path`` - The path pointing to the certificate file to be uploaded. Only ``.pfx`` files are supported.
 * - ``password`` - An optional password if the certificate file was encrypted with one.
 *
 * **Remove Certificate**
 *
 * Remove a certificate from the key vault. Access policy needed (delete).
 *
 * .. code::
 *
 *     gyro azure key-vault remove-certificate <vault-name> <cert-name>
 *
 * - ``vault-name`` - The name of the key-vault resource defined in your config from which to remove the certificate.
 * - ``cert-name`` - The name of the certificate that you want to remove.
 *
 * **List Certificate**
 *
 * List certificates of a vault. Access policy needed (List).
 *
 * .. code::
 *
 *     gyro azure key-vault list-certificate <vault-name>
 *
 * - ``vault-name`` - The name of the key-vault resource defined in your config that you want to list certificates from.
 * - ``show-thumbprint`` - An option that shows the x509 thumbprint of the certificate.
 *
 *
 * Secret Commands
 * --------------------
 *
 * The following set of commands allow you to manage secrets in a key vault. Before using these commands
 * you must have already created an ``azure::key-vault``. The key vault must be managed by Gyro. Ensure a proper
 * access policy is added to the key vault for the service principal you are using.
 *
 * **Add Secret**
 *
 * Adds a secret to a key vault. Access policy needed (Set, Get).
 *
 * .. code::
 *
 *     gyro azure key-vault add-secret <vault-name> <secret-name> <value> --content-type <content-type>
 *
 * - ``vault-name`` - The name of the key-vault resource defined in your config where you want to create your secret.
 * - ``secret-name`` - The name of the secret that you want to create.
 * - ``value`` - The secret value.
 * - ``content-type`` - An optional value specifying the content type of the secret.
 * - ``expires`` - An optional date time value value in UTC specifying the expiration time. Format 'YYYY-MM-DDTHH:MM:SS.sssZ'.
 * - ``not-before`` - An optional date time value value in UTC specifying the expiration not before a specific time. Format 'YYYY-MM-DDTHH:MM:SS.sssZ'.
 * - ``enabled`` - An optional value specifying if the secret is enable or disable the secret. Defaults to 'false'.
 *
 * **Remove Secret**
 *
 * Remove a secret from the key vault. Access policy needed (delete).
 *
 * .. code::
 *
 *     gyro azure key-vault remove-secret <vault-name> <secret-name>
 *
 * - ``vault-name`` - The name of the key-vault resource defined in your config from which to remove the secret.
 * - ``secret-name`` - The name of the secret that you want to remove.
 *
 * **List Secret**
 *
 * List secrets of a vault. Access policy needed (List).
 *
 * .. code::
 *
 *     gyro azure key-vault list-secret <vault-name>
 *
 * - ``vault-name`` - The name of the key-vault resource defined in your config that you want to list secrets from.
 *
 *
 */
@Type("key-vault")
public class KeyVaultResource extends AzureResource implements Copyable<Vault> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;
    private Set<KeyVaultAccessPolicy> accessPolicy;
    private Boolean enableDeployment;
    private Boolean enableTemplateDeployment;
    private Boolean enableDiskEncryption;
    private Boolean enablePurgeVault;
    private Boolean enableSoftDelete;
    private SkuName skuTier;
    private String id;
    private String url;
    private String location;

    /**
     * The name of the key vault.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the key vault would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Tags for the key vault.
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
     * A set of access policy configs for the key vault. See `Access Policy <https://docs.microsoft.com/en-us/azure/key-vault/key-vault-group-permissions-for-apps>`_.
     *
     * @subresource gyro.azure.keyvault.KeyVaultAccessPolicy
     */
    @Updatable
    public Set<KeyVaultAccessPolicy> getAccessPolicy() {
        if (accessPolicy == null) {
            accessPolicy = new HashSet<>();
        }

        return accessPolicy;
    }

    public void setAccessPolicy(Set<KeyVaultAccessPolicy> accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    /**
     *  When set to``true`` virtual machines are permitted to retrieve certificates stored as secrets from the key vault.
     */
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

    /**
     * When set to ``true`` resource managers are permitted to retrieve certificates stored as secrets from the key vault.
     */
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

    /**
     * When set to ``true`` disk managers are permitted to retrieve certificates stored as secrets from the key vault and unwrap keys.
     */
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
     * When set to `true` purges the vault upon deletion to remove the key vault beyond recovery. If set to `false` vault would be deleted but could be recovered until `90` days. During this time no other vault with the same name can be created.
     */
    @Updatable
    public Boolean getEnablePurgeVault() {
        if (enablePurgeVault == null) {
            enablePurgeVault = true;
        }

        return enablePurgeVault;
    }

    public void setEnablePurgeVault(Boolean enablePurgeVault) {
        this.enablePurgeVault = enablePurgeVault;
    }

    /**
     * When set to ``true`` enables soft delete for the key vault.
     */
    @Updatable
    public Boolean getEnableSoftDelete() {
        if (enableSoftDelete == null) {
            enableSoftDelete = false;
        }

        return enableSoftDelete;
    }

    public void setEnableSoftDelete(Boolean enableSoftDelete) {
        this.enableSoftDelete = enableSoftDelete;
    }

    /**
     * The SKU tier for the key vault.
     */
    public SkuName getSkuTier() {
        if (skuTier == null) {
            skuTier = SkuName.STANDARD;
        }

        return skuTier;
    }

    public void setSkuTier(SkuName skuTier) {
        this.skuTier = skuTier;
    }

    /**
     * The ID of the key vault.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The URI of the key vault.
     */
    @Output
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The location of the key vault.
     */
    @Output
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void copyFrom(Vault vault) {
        setName(vault.name());
        setId(vault.id());
        setUrl(vault.vaultUri());
        setResourceGroup(findById(ResourceGroupResource.class, vault.resourceGroupName()));
        setTags(vault.tags());
        setSkuTier(vault.sku().name());

        getAccessPolicy().clear();
        if (vault.accessPolicies() != null) {
            setAccessPolicy(vault.accessPolicies().stream().map(o -> {
                KeyVaultAccessPolicy accessPolicy = newSubresource(KeyVaultAccessPolicy.class);
                accessPolicy.copyFrom(o);
                return accessPolicy;
            }).collect(Collectors.toSet()));
        }

        setEnableDeployment(vault.enabledForDeployment());
        setEnableDiskEncryption(vault.enabledForDiskEncryption());
        setEnableTemplateDeployment(vault.enabledForTemplateDeployment());
        setLocation(vault.innerModel().location());
        setEnableSoftDelete(vault.softDeleteEnabled());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Vault vault = client.vaults().getByResourceGroup(getResourceGroup().getName(), getName());

        if (vault == null) {
            return false;
        }

        copyFrom(vault);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Vault.DefinitionStages.WithCreate withCreate = client.vaults().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName()).withEmptyAccessPolicy()
            .withSku(getSkuTier());

        if (!getAccessPolicy().isEmpty()) {
            for (KeyVaultAccessPolicy accessPolicy : getAccessPolicy()) {
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

        if (getEnableSoftDelete()) {
            withCreate = withCreate.withSoftDeleteEnabled();
        }

        Vault vault = withCreate.create();

        setId(vault.id());
        setUrl(vault.vaultUri());
        setLocation(vault.innerModel().location());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Vault vault = client.vaults().getById(getId());

        Vault.Update update = vault.update();
        KeyVaultResource currentKeyVaultResource = (KeyVaultResource) current;

        if (changedFieldNames.contains("access-policy")) {
            if (!currentKeyVaultResource.getAccessPolicy().isEmpty()) {
                for (KeyVaultAccessPolicy accessPolicy : currentKeyVaultResource.getAccessPolicy()) {
                    update = update.withoutAccessPolicy(accessPolicy.getObjectId());
                }
            }

            if (!getAccessPolicy().isEmpty()) {
                for (KeyVaultAccessPolicy accessPolicy : getAccessPolicy()) {
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
            if (!currentKeyVaultResource.getTags().isEmpty()) {
                for (String tag : currentKeyVaultResource.getTags().keySet()) {
                    update = update.withoutTag(tag);
                }
            }

            if (!getTags().isEmpty()) {
                update = update.withTags(getTags());
            }
        }

        if (changedFieldNames.contains("enable-soft-delete")) {
            if (getEnableSoftDelete()) {
                update = update.withSoftDeleteEnabled();
            } else {
                throw new GyroException("'enable-soft-delete' cannot be disabled.");
            }
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.vaults().deleteById(getId());

        if (getEnablePurgeVault()) {
            client.vaults().purgeDeleted(getName(), getLocation());
        }
    }

    Vault getKeyVault() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        return client.vaults().getByResourceGroup(getResourceGroup().getName(), getName());
    }
}
