package gyro.azure.keyvault;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates a key vault secret.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::key-vault-secret vault-secret-example
 *         name: "secret-example"
 *         value: "secret-value"
 *         vault: $(azure::key-vault vault-example-secret)
 *
 *         attribute
 *             enabled : true
 *             expires : "2020-04-04T15:54:12.000Z"
 *             not-before : "2020-04-02T15:54:12.000Z"
 *         end
 *
 *         tags: {
 *             Name: "vault-secret-examples"
 *         }
 *     end
 */
@Type("key-vault-secret")
public class KeyVaultSecretResource extends AzureResource implements Copyable<KeyVaultSecret> {

    private String name;
    private KeyVaultResource vault;
    private String value;
    private KeyVaultSecretAttribute attribute;
    private String contentType;
    private String id;
    private String kid;
    private String identifier;
    private Map<String, String> tags;

    /**
     * The name of the secret.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The key vault under which the secret is going to be created.
     */
    @Required
    public KeyVaultResource getVault() {
        return vault;
    }

    public void setVault(KeyVaultResource vault) {
        this.vault = vault;
    }

    /**
     * The value of the secret.
     */
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The attribute config for the secret.
     *
     * @subresource gyro.azure.keyvault.KeyVaultSecretAttribute
     */
    @Required
    @Updatable
    public KeyVaultSecretAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(KeyVaultSecretAttribute attribute) {
        this.attribute = attribute;
    }

    /**
     * The content type for the secret.
     */
    @Updatable
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * The Id of the secret.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The KID of the secret.
     */
    @Output
    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * The identifier of the secret.
     */
    @Output
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Tags for the secret.
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
    public void copyFrom(KeyVaultSecret secret) {
        setName(secret.getName());
        setId(secret.getId());
        setKid(secret.getProperties().getKeyId());
        setTags(secret.getProperties().getTags());
        setValue(secret.getValue());
        setContentType(secret.getProperties().getContentType());

        KeyVaultSecretAttribute attribute = newSubresource(KeyVaultSecretAttribute.class);
        attribute.copyFrom(secret.getProperties());
        setAttribute(attribute);

        String vaultName = getId().split(".vault.azure.net")[0].split("://")[1];
        setVault(findById(KeyVaultResource.class, vaultName));
    }

    @Override
    public boolean refresh() {
        SecretClient client = getSecretClient();

        KeyVaultSecret secret = client.getSecret(getName());

        if (secret == null) {
            return false;
        }

        copyFrom(secret);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        SecretClient client = getSecretClient();

        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(getName(), getValue());

        SecretProperties secretProperties = getAttribute().toSecretProperties();

        if (!StringUtils.isBlank(getContentType())) {
            secretProperties = secretProperties.setContentType(getContentType());
        }

        if (!getTags().isEmpty()) {
            secretProperties = secretProperties.setTags(getTags());
        }

        keyVaultSecret = keyVaultSecret.setProperties(secretProperties);

        copyFrom(client.setSecret(keyVaultSecret));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        SecretClient client = getSecretClient();

        KeyVaultSecret secret = client.getSecret(getName());

        if (changedFieldNames.contains("attribute")) {
            secret.getProperties().setEnabled(getAttribute().getEnabled());
            secret.getProperties().setExpiresOn(
                getAttribute().getExpires() != null ? OffsetDateTime.parse(getAttribute().getExpires()) : null);
            secret.getProperties().setNotBefore(
                getAttribute().getNotBefore() != null ? OffsetDateTime.parse(getAttribute().getNotBefore()) : null);
        }

        if (changedFieldNames.contains("content-type")) {
            secret.getProperties().setContentType(getContentType());
        }

        if (changedFieldNames.contains("tags")) {
            secret.getProperties().setTags(getTags());
        }

        client.updateSecretProperties(secret.getProperties());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        SecretClient client = getSecretClient();
        SyncPoller<DeletedSecret, Void> deletedSecretPoller = client.beginDeleteSecret(getName());
        deletedSecretPoller.poll();
        deletedSecretPoller.waitForCompletion();
    }

    public SecretClient getSecretClient() {
        return new SecretClientBuilder().credential(getTokenCredential()).vaultUrl(getVault().getUrl()).buildClient();
    }
}
