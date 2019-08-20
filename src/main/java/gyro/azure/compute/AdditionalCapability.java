package gyro.azure.compute;

import com.microsoft.azure.management.compute.AdditionalCapabilities;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;

public class AdditionalCapability extends Diffable implements Copyable<AdditionalCapabilities> {
    private Boolean ultraSSDEnabled;

    /**
     * Enable ultra SSD. Defaults to ``false``.
     */
    public Boolean getUltraSSDEnabled() {
        if (ultraSSDEnabled == null) {
            ultraSSDEnabled = false;
        }

        return ultraSSDEnabled;
    }

    public void setUltraSSDEnabled(Boolean ultraSSDEnabled) {
        this.ultraSSDEnabled = ultraSSDEnabled;
    }

    @Override
    public void copyFrom(AdditionalCapabilities additionalCapabilities) {
        setUltraSSDEnabled(additionalCapabilities.ultraSSDEnabled());
    }

    AdditionalCapabilities toAdditionalCapabilities() {
        AdditionalCapabilities capabilities = new AdditionalCapabilities();
        capabilities.withUltraSSDEnabled(getUltraSSDEnabled());
        return capabilities;
    }

    @Override
    public String primaryKey() {
        return "capability";
    }
}
