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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.CertificateItem;
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
    private Boolean enablePurgeVault;
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
