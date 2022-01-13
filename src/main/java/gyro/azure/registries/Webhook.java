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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.util.ExpandableStringEnum;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMin;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class Webhook extends Diffable implements Copyable<com.azure.resourcemanager.containerregistry.models.Webhook> {

    private String name;
    private Set<String> actions;
    private Map<String, String> customHeaders;
    private Boolean enabled;
    private String serviceUri;
    private String repositoryScope;
    private Map<String, String> tags;

    /**
     * The name of the webhook.
     */
    @Required
    @Regex(value = "[a-zA-Z0-9]{5,50}", message = "5-50 characters long an supporting only alpha numeric values")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Actions for the webhook.
     */
    @Required
    @CollectionMin(1)
    @Updatable
    @ValidStrings({"push", "delete", "quarantine", "chart_push", "chart_delete"})
    public Set<String> getActions() {
        if (actions == null) {
            actions = new HashSet<>();
        }

        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    /**
     * A list of custom headers for the webhook.
     */
    @Updatable
    public Map<String, String> getCustomHeaders() {
        if (customHeaders == null) {
            customHeaders = new HashMap<>();
        }

        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    /**
     * Enabled if set to ``true``.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The service url for the webhook.
     */
    @Required
    @Updatable
    public String getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }

    /**
     * The scope for the webhook.
     */
    @Updatable
    public String getRepositoryScope() {
        return repositoryScope;
    }

    public void setRepositoryScope(String repositoryScope) {
        this.repositoryScope = repositoryScope;
    }

    /**
     * A set of tags for the webhook.
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
    public void copyFrom(com.azure.resourcemanager.containerregistry.models.Webhook model) {
        setName(model.name());
        setActions(model.triggers().stream().map(ExpandableStringEnum::toString).collect(Collectors.toSet()));
        setCustomHeaders(model.customHeaders());
        setEnabled(model.isEnabled());
        setServiceUri(model.serviceUri());
        setRepositoryScope(model.scope());
        setTags(model.tags());
    }

    @Override
    public String primaryKey() {
        return getName();
    }
}
