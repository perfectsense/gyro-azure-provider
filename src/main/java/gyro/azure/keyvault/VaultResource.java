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

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
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
 * The vault is managed using a combination of ``gyro`` configuration and commands. Currently ``certificate`` is the only entity that can be managed using ``gyro``.
 *
 * The vault resource by itself is managed using ``gyro`` configuration which allows you to ``create``, ``update`` and ``remove`` a vault, including managing the access policies of the vault that handles the permissions to manage certificates, keys and secrets.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::vault vault-example
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
 * A set of commands that lets you manage certificates (``add``, ``remove`` or ``list``) of a vault that you are managing using ``gyro``. In order to use these commands, make sure to add proper access policy to the vault for the service principal you are using for the ``azure-provider``.
 *
 * The following commands are supported:
 *
 * **Add Certificate**
 *
 * Adds a certificate to a vault using your certificate file (.pfx). Access policy needed (Upload, Insert).
 *
 * .. code::
 *
 *     gyro azure vault add-certificate <vault-name> <cert-name> <path> --password <password>
 *
 * - ``vault-name`` - The name of the vault resource defined in your config where you want to create your certificate.
 * - ``cert-name`` - The name of the certificate that you want to create when you import the certificate file.
 * - ``cert-path`` - The path pointing to the certificate file to be uploaded. Only ``.pfx`` files are supported.
 * - ``password`` - An optional password if the certificate file was encrypted with one.
 *
 * **Remove Certificate**
 *
 * Remove a certificate from the vault. Access policy needed (delete).
 *
 * .. code::
 *
 *     gyro azure vault remove-certificate <vault-name> <cert-name>
 *
 * - ``vault-name`` - The name of the vault resource defined in your config from which to remove the certificate.
 * - ``cert-name`` - The name of the certificate that you want to remove.
 *
 * **List Certificate**
 *
 * List certificates of a vault. Access policy needed (List).
 *
 * .. code::
 *
 *     gyro azure vault list-certificate <vault-name>
 *
 * - ``vault-name`` - The name of the vault resource defined in your config that you want to list certificates from.
 * - ``show-thumbprint`` - An option that shows the x509 thumbprint of the certificate.
 *
 *

 */
@Type("vault")
public class VaultResource extends AzureResource implements Copyable<Vault> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;
    private Set<VaultAccessPolicy> accessPolicy;
    private Boolean enableDeployment;
    private Boolean enableTemplateDeployment;
    private Boolean enableDiskEncryption;
    private Boolean enablePurgeVault;
    private Boolean enableSoftDelete;
    private String id;
    private String url;
    private String location;

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

    /**
     * A set of access policy configs for the vault.
     *
     * @subresource gyro.azure.keyvault.VaultAccessPolicy
     */
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

    /**
     *  When ``true`` virtual machines are permitted to retrieve certificates stored as secrets from the vault.
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
     * When ``true`` resource managers are permitted to retrieve certificates stored as secrets from the vault.
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
     * When ``true`` disk managers are permitted to retrieve certificates stored as secrets from the vault and unwrap keys.
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
     * When set to `true` purges the vault upon deletion to remove the vault beyond recovery. If set to `false` vault would be deleted but could be recovered until `90` days. During this time no other vault with the same name can be created.
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
     * Enables soft delete for the vault.
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

    /**
     * The location of the vault.
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
        setLocation(vault.inner().location());
        setEnableSoftDelete(vault.softDeleteEnabled());
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

        if (getEnableSoftDelete()) {
            withCreate = withCreate.withSoftDeleteEnabled();
        }

        Vault vault = withCreate.create();

        setId(vault.id());
        setUrl(vault.vaultUri());
        setLocation(vault.inner().location());
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
        Azure client = createClient();

        client.vaults().deleteById(getId());

        try {
            if (getEnablePurgeVault()) {
                client.vaults().purgeDeleted(getName(), getLocation());
            }
        } catch (CloudException ex) {
            if (ex.body() == null || ex.body().code() == null || !ex.body().code().equals("ResourceNotFound")) {
                throw ex;
            }
        }
    }
}
