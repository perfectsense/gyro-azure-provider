package gyro.azure.keyvault;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.requests.UpdateSecretRequest;
import com.microsoft.azure.management.keyvault.Vault;
import com.psddev.dari.util.ObjectUtils;
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
public class KeyVaultSecretResource extends AzureResource implements Copyable<SecretBundle> {

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
    @Id
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
    public void copyFrom(SecretBundle secret) {
        setName(secret.secretIdentifier().name());
        setId(secret.id());
        setKid(secret.kid());
        setIdentifier(secret.secretIdentifier().identifier());
        setTags(secret.tags());
        setValue(secret.value());
        setContentType(secret.contentType());
        String vaultUri = secret.secretIdentifier().vault();
        vaultUri = vaultUri.endsWith("/") ? vaultUri : vaultUri + "/";
        setVault(findById(KeyVaultResource.class, vaultUri));

        KeyVaultSecretAttribute attribute = newSubresource(KeyVaultSecretAttribute.class);
        attribute.copyFrom(secret.attributes());
        setAttribute(attribute);
    }

    @Override
    public boolean refresh() {
        Vault vault = getVault().getKeyVault();
        SecretBundle secret;

        try {
            secret = vault.client().getSecret(vault.vaultUri(), getName());
        } catch (KeyVaultErrorException ex) {
            if (ex.body().error().message().equals("Operation get is not allowed on a disabled secret.")) {
                // secret is present but in disabled state
                return true;
            }

            throw ex;
        }

        if (secret == null) {
            return false;
        }

        copyFrom(secret);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Vault vault = getVault().getKeyVault();

        SetSecretRequest.Builder builder = new SetSecretRequest.Builder(vault.vaultUri(), getName(), getValue());

        builder.withAttributes(getAttribute().toSecretAttributes());

        if (!getTags().isEmpty()) {
            builder.withTags(getTags());
        }

        if (!ObjectUtils.isBlank(getContentType())) {
            builder.withContentType(getContentType());
        }

        SecretBundle secret = vault.client().setSecret(builder.build());

        copyFrom(secret);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Vault vault = getVault().getKeyVault();

        UpdateSecretRequest.Builder builder = new UpdateSecretRequest.Builder(getId());

        builder.withAttributes(getAttribute().toSecretAttributes());
        builder.withContentType(getContentType());
        builder.withTags(getTags());

        vault.client().updateSecret(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Vault vault = getVault().getKeyVault();

        vault.client().deleteSecret(vault.vaultUri(), getName());
    }
}
