package gyro.azure.keyvault;

import java.time.OffsetDateTime;

import com.azure.security.keyvault.keys.models.KeyProperties;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class KeyVaultKeyAttribute extends Diffable implements Copyable<KeyProperties> {

    private Boolean enabled;
    private String expires;
    private String notBefore;
    private String created;
    private String updated;

    /**
     * Enable or Disable the key for use.
     */
    @Required
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * A date time value value in UTC specifying when the key expires. Format ``YYYY-MM-DDTHH:MM:SS.sssZ``. Example ``2020-04-03T15:54:12.000Z``.
     */
    @Updatable
    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    /**
     * A date time value value in UTC specifying the not before time. Format ``YYYY-MM-DDTHH:MM:SS.sssZ``. Example ``2020-04-03T15:54:12.000Z``.
     */
    @Updatable
    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    /**
     * The date time value in UTC of when the key was created.
     */
    @Output
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * The date time value in UTC of when the key was last updated.
     */
    @Output
    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(KeyProperties properties) {
        setEnabled(properties.isEnabled());
        setExpires(properties.getExpiresOn() != null ? properties.getExpiresOn().toString() : null);
        setNotBefore(properties.getNotBefore() != null ? properties.getNotBefore().toString() : null);
        setCreated(properties.getCreatedOn() != null ? properties.getCreatedOn().toString() : null);
        setUpdated(properties.getUpdatedOn() != null ? properties.getUpdatedOn().toString() : null);
    }

    KeyProperties toKeyProperties() {
        return new KeyProperties()
            .setEnabled(getEnabled())
            .setExpiresOn(getExpires() != null ? OffsetDateTime.parse(getExpires()) : null)
            .setNotBefore(getNotBefore() != null ? OffsetDateTime.parse(getNotBefore()) : null);
    }
}
