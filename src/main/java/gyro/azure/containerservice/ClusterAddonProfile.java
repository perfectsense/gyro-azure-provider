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
