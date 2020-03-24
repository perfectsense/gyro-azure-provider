package gyro.azure.keyvault;

import com.microsoft.azure.keyvault.models.SecretAttributes;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import org.joda.time.DateTime;

public class KeyVaultSecretAttribute extends Diffable implements Copyable<SecretAttributes> {
    private Boolean enabled;
    private String expires;
    private String notBefore;
    private String created;
    private String updated;

    /**
     * Enable or Disable the secret for use.
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
     * A date time value value in UTC specifying when the secret expires. Format ``YYYY-MM-DDTHH:MM:SS.sssZ``. Example ``2020-04-03T15:54:12.000Z``.
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
     * The date time value in UTC of when the secret was created.
     */
    @Output
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * The date time value in UTC of when the secret was last updated.
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
    public void copyFrom(SecretAttributes attributes) {
        setEnabled(attributes.enabled());
        setExpires(attributes.expires() != null ? attributes.expires().toString() : null);
        setNotBefore(attributes.notBefore() != null ? attributes.notBefore().toString() : null);
        setCreated(attributes.created() != null ? attributes.created().toString() : null);
        setUpdated(attributes.updated() != null ? attributes.updated().toString() : null);
    }

    SecretAttributes toSecretAttributes() {
        return (SecretAttributes) new SecretAttributes()
            .withEnabled(getEnabled())
            .withExpires(getExpires() != null ? DateTime.parse(getExpires()) : null)
            .withNotBefore(getNotBefore() != null ? DateTime.parse(getNotBefore()) : null);
    }
}
