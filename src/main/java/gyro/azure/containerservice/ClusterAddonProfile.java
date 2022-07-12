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

package gyro.azure.containerservice;

import java.util.HashMap;
import java.util.Map;

import com.azure.resourcemanager.containerservice.models.ManagedClusterAddonProfile;
import gyro.azure.Copyable;
import gyro.azure.identity.IdentityResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class ClusterAddonProfile extends Diffable implements Copyable<ManagedClusterAddonProfile> {

    private Map<String, String> config;
    private IdentityResource identity;
    private Boolean enabled;

    /**
     * The config for the addon profile.
     */
    @Required
    @Updatable
    public Map<String, String> getConfig() {
        if (config == null) {
            config = new HashMap<>();
        }

        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /**
     * The identity for the addon profile.
     */
    @Required
    @Updatable
    public IdentityResource getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityResource identity) {
        this.identity = identity;
    }

    /**
     * If set to ``true`` enables the addon profile. Defaults to``true``.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(ManagedClusterAddonProfile model) {
        setConfig(model.config());
        setIdentity(findById(IdentityResource.class, model.identity() != null ? model.identity().resourceId() : null));
        setEnabled(model.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterAddonProfile toAddonProfile() {
        return new ManagedClusterAddonProfile()
            .withConfig(getConfig())
            .withEnabled(getEnabled());
    }
}
