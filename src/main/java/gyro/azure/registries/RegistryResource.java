/*
 * Copyright 2022, Brightspot, Inc.
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

package gyro.azure.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerregistry.models.PublicNetworkAccess;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.Registry.Update;
import com.azure.resourcemanager.containerregistry.models.Webhook.UpdateResourceStages.WithAttach;
import com.azure.resourcemanager.containerregistry.models.WebhookAction;
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
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

/**
 * Creates a registry.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::registry registry-example
 *         name: "registry-example"
 *         sku: "Standard"
 *         resource-group: $(azure::resource-group resource-group-registry-example)
 *         public-network-access: false
 *         admin-user-enabled: false
 *
 *         tags: {
 *             Name: "registry-example"
 *         }
 *     end
 */
@Type("registry")
public class RegistryResource extends AzureResource implements Copyable<Registry> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private String sku;
    private Boolean adminUserEnabled;
    private Boolean publicNetworkAccess;
    private Map<String, String> tags;
    private Set<Webhook> webhook;

    private String id;
    private String creationDate;
    private String loginServerUrl;

    /**
     * The name of the registry.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The resource group under which the registry will reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The type of sku.
     */
    @Required
    @Updatable
    @ValidStrings({"Standard", "Premium", "Basic"})
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * If set to ``true`` enables admin user for the registry. Defaults to ``false``.
     */
    @Updatable
    public Boolean getAdminUserEnabled() {
        if (adminUserEnabled == null) {
            adminUserEnabled = false;
        }

        return adminUserEnabled;
    }

    public void setAdminUserEnabled(Boolean adminUserEnabled) {
        this.adminUserEnabled = adminUserEnabled;
    }

    /**
     * If set to ``true`` enables public network access to this registry. Defaults to ``true``.
     */
    @Updatable
    public Boolean getPublicNetworkAccess() {
        if (publicNetworkAccess == null) {
            publicNetworkAccess = true;
        }

        return publicNetworkAccess;
    }

    public void setPublicNetworkAccess(Boolean publicNetworkAccess) {
        this.publicNetworkAccess = publicNetworkAccess;
    }

    /**
     * The tags for the registry.
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
     * A set of webhooks for the registry.
     */
    @Updatable
    public Set<Webhook> getWebhook() {
        if (webhook == null) {
            webhook = new HashSet<>();
        }

        return webhook;
    }

    public void setWebhook(Set<Webhook> webhook) {
        this.webhook = webhook;
    }

    /**
     * The id of the registry.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The creation date of the registry.
     */
    @Output
    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * The login server url of the registry.
     */
    @Output
    public String getLoginServerUrl() {
        return loginServerUrl;
    }

    public void setLoginServerUrl(String loginServerUrl) {
        this.loginServerUrl = loginServerUrl;
    }

    @Override
    public void copyFrom(Registry registry) {
        setName(registry.name());
        setAdminUserEnabled(registry.adminUserEnabled());
        setCreationDate(registry.creationDate().toString());
        setId(registry.id());
        setSku(registry.sku().name().toString());
        setTags(registry.tags());
        setResourceGroup(findById(ResourceGroupResource.class, registry.resourceGroupName()));
        setPublicNetworkAccess(registry.publicNetworkAccess().equals(PublicNetworkAccess.ENABLED));
        setLoginServerUrl(registry.loginServerUrl());

        getWebhook().clear();
        if (registry.webhooks() != null) {
            registry.webhooks().list().forEach(hook -> {
                Webhook webhook = newSubresource(Webhook.class);
                webhook.copyFrom(hook);
                getWebhook().add(webhook);
            });
        }
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        Registry registry = client.containerRegistries()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        if (registry == null) {
            return false;
        }

        copyFrom(registry);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

        Registry.DefinitionStages.WithSku withSku = client.containerRegistries()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        Registry.DefinitionStages.WithCreate withCreate;

        if (getSku().equals("Standard")) {
            withCreate = withSku.withStandardSku();
        } else if (getSku().equals("Basic")) {
            withCreate = withSku.withBasicSku();
        } else if (getSku().equals("Premium")) {
            withCreate = withSku.withPremiumSku();
        } else {
            // Unreachable valid string on sku should take care of it.
            throw new GyroException("Invalid sku option");
        }

        if (!getTags().isEmpty()) {
            withCreate = withCreate.withTags(getTags());
        }

        if (getAdminUserEnabled()) {
            withCreate = withCreate.withRegistryNameAsAdminUser();
        }

        if (!getPublicNetworkAccess()) {
            withCreate = withCreate.disablePublicNetworkAccess();
        }

        Registry registry = withCreate.create();

        setId(registry.id());
        setCreationDate(registry.creationDate().toString());

        if (!getWebhook().isEmpty()) {
            HashSet<Webhook> currentWebhooks = new HashSet<>(getWebhook());
            getWebhook().clear();
            state.save();

            setWebhook(currentWebhooks);
            Update update = registry.update();

            update = updateWebhooks(update, null);
            update.apply();
        }

        copyFrom(registry);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

        Registry registry = client.containerRegistries()
            .getByResourceGroup(getResourceGroup().getName(), getName());

        Update update = registry.update();

        if (changedFieldNames.contains("tags")) {
            update = update.withTags(getTags());
        }

        if (changedFieldNames.contains("public-network-access")) {
            if (getPublicNetworkAccess()) {
                update = update.enablePublicNetworkAccess();
            } else {
                update = update.disablePublicNetworkAccess();
            }
        }

        if (changedFieldNames.contains("sku")) {
            if (getSku().equals("Standard")) {
                update = update.withStandardSku();
            } else if (getSku().equals("Basic")) {
                update = update.withBasicSku();
            } else if (getSku().equals("Premium")) {
                update = update.withPremiumSku();
            } else {
                // Unreachable valid string on sku should take care of it.
                throw new GyroException("Invalid sku option");
            }
        }

        if (changedFieldNames.contains("admin-user-enabled")) {
            if (getAdminUserEnabled()) {
                update = update.withRegistryNameAsAdminUser();
            } else {
                update = update.withoutRegistryNameAsAdminUser();
            }
        }

        if (changedFieldNames.contains("webhook")) {
            RegistryResource oldResource = (RegistryResource) current;

            update = updateWebhooks(update, oldResource.getWebhook());
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createResourceManagerClient();

        client.containerRegistries().deleteByResourceGroup(getResourceGroup().getName(), getName());
    }

    private Update updateWebhooks(Update update, Set<Webhook> oldWebhooks) {
        if (oldWebhooks == null) {
            oldWebhooks = new HashSet<>();
        }

        Set<String> oldWebhookNames = oldWebhooks.stream().map(Webhook::getName).collect(Collectors.toSet());
        Set<String> newWebhookNames = getWebhook().stream().map(Webhook::getName).collect(Collectors.toSet());

        Set<Webhook> addWebhooks = getWebhook().stream()
            .filter(o -> !oldWebhookNames.contains(o.getName()))
            .collect(Collectors.toSet());

        Set<Webhook> removeWebhooks = oldWebhooks.stream()
            .filter(o -> !newWebhookNames.contains(o.getName()))
            .collect(Collectors.toSet());

        Set<Webhook> updateWebhooks = getWebhook().stream()
            .filter(o -> oldWebhookNames.contains(o.getName()))
            .collect(Collectors.toSet());

        if (!removeWebhooks.isEmpty()) {
            for (Webhook webhook : removeWebhooks) {
                update = update.withoutWebhook(webhook.getName());
            }
        }

        if (!updateWebhooks.isEmpty()) {
            for (Webhook webhook : updateWebhooks) {
                WithAttach<Update> updateWithAttach = update
                    .updateWebhook(webhook.getName())
                    .withTriggerWhen(webhook.getActions()
                        .stream()
                        .map(WebhookAction::fromString)
                        .toArray(WebhookAction[]::new))
                    .withServiceUri(webhook.getServiceUri())
                    .enabled(webhook.getEnabled())
                    .withRepositoriesScope(webhook.getRepositoryScope())
                    .withTags(null)
                    .withCustomHeaders(null);

                if (!webhook.getTags().isEmpty()) {
                    for (String key : webhook.getTags().keySet()) {
                        updateWithAttach = updateWithAttach.withTag(key, webhook.getTags().get(key));
                    }
                }

                if (!webhook.getCustomHeaders().isEmpty()) {
                    for (String key : webhook.getCustomHeaders().keySet()) {
                        updateWithAttach = updateWithAttach.withCustomHeader(key, webhook.getCustomHeaders().get(key));
                    }
                }

                update = updateWithAttach.parent();
            }
        }

        if (!addWebhooks.isEmpty()) {
            for (Webhook webhook : addWebhooks) {
                com.azure.resourcemanager.containerregistry.models.Webhook.UpdateDefinitionStages.WithAttach<Update> updateWithAttach = update
                    .defineWebhook(webhook.getName())
                    .withTriggerWhen(webhook.getActions()
                        .stream()
                        .map(WebhookAction::fromString)
                        .toArray(WebhookAction[]::new))
                    .withServiceUri(webhook.getServiceUri())
                    .enabled(webhook.getEnabled())
                    .withRepositoriesScope(webhook.getRepositoryScope());

                if (!webhook.getTags().isEmpty()) {
                    for (String key : webhook.getTags().keySet()) {
                        updateWithAttach = updateWithAttach.withTag(key, webhook.getTags().get(key));
                    }
                }

                if (!webhook.getCustomHeaders().isEmpty()) {
                    for (String key : webhook.getCustomHeaders().keySet()) {
                        updateWithAttach = updateWithAttach.withCustomHeader(key, webhook.getCustomHeaders().get(key));
                    }
                }

                update = updateWithAttach.attach();
            }
        }

        return update;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (!getPublicNetworkAccess() && !"Premium".equals(getSku())) {
            validationErrors.add(new ValidationError(this, "public-network-access", "cannot be set to `false` when 'sku' is not set to `Premium`."));
        }

        return validationErrors;
    }
}
