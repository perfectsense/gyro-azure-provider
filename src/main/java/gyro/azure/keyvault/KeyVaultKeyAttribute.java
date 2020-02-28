package gyro.azure.keyvault;

import com.microsoft.azure.keyvault.models.KeyAttributes;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import org.joda.time.DateTime;

public class KeyVaultKeyAttribute extends Diffable implements Copyable<KeyAttributes> {

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
    public void copyFrom(KeyAttributes attributes) {
        setEnabled(attributes.enabled());
        setExpires(attributes.expires() != null ? attributes.expires().toString() : null);
        setNotBefore(attributes.notBefore() != null ? attributes.notBefore().toString() : null);
        setCreated(attributes.created() != null ? attributes.created().toString() : null);
        setUpdated(attributes.updated() != null ? attributes.updated().toString() : null);
    }

    KeyAttributes toKeyAttributes() {
        return (KeyAttributes) new KeyAttributes()
            .withEnabled(getEnabled())
            .withExpires(getExpires() != null ? DateTime.parse(getExpires()) : null)
            .withNotBefore(getNotBefore() != null ? DateTime.parse(getNotBefore()) : null);
    }
}
