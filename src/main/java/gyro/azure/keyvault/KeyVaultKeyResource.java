package gyro.azure.keyvault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.requests.CreateKeyRequest;
import com.microsoft.azure.keyvault.requests.UpdateKeyRequest;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.azure.management.keyvault.Vault;
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
import gyro.core.validation.ValidStrings;

/**
 * Creates a key vault key.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::key-vault-key vault-key-example
 *         name: "key-example-gyro"
 *         vault: $(azure::key-vault vault-example-keys)
 *         type: "RSA"
 *
 *         operations: ["encrypt", "decrypt"]
 *
 *         attribute
 *             enabled : false
 *             expires : "2020-04-04T15:54:12.000Z"
 *             not-before : "2020-04-02T15:54:12.000Z"
 *         end
 *
 *         tags: {
 *             Name: "vault-key-examples"
 *         }
 *     end
 */
@Type("key-vault-key")
public class KeyVaultKeyResource extends AzureResource implements Copyable<KeyBundle> {

    private String name;
    private KeyVaultResource vault;
    private String type;
    private KeyVaultKeyAttribute attribute;
    private Integer size;
    private List<String> operations;
    private String version;
    private String id;
    private Map<String, String> tags;

    /**
     * The name of the key.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The key vault under which the key is going to be created.
     */
    @Required
    public KeyVaultResource getVault() {
        return vault;
    }

    public void setVault(KeyVaultResource vault) {
        this.vault = vault;
    }

    /**
     * The type of the key. Valid values are ``EC``, ``RSA``, ``RSA-HSM`` or ``oct``
     */
    @Required
    @ValidStrings({"EC", "RSA", "RSA-HSM", "oct"})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The attribute config for the key.
     *
     * @subresource gyro.azure.keyvault.KeyVaultKeyAttribute
     */
    @Required
    @Updatable
    public KeyVaultKeyAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(KeyVaultKeyAttribute attribute) {
        this.attribute = attribute;
    }

    /**
     * The size of the key.
     */
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * A set of key operations that you want to enable.
     */
    @Updatable
    @ValidStrings({"encrypt", "decrypt", "sign", "verify", "wrapKey", "unwrapKey"})
    public List<String> getOperations() {
        if (operations == null) {
            operations = new ArrayList<>();
        }

        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }

    /**
     * The current version of the key.
     */
    @Output
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The id of the key.
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
     * Tags for the key.
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
    public void copyFrom(KeyBundle key) {
        setTags(key.tags());
        setType(key.key().kty().toString());
        setOperations(key.key().keyOps().stream().map(JsonWebKeyOperation::toString).collect(Collectors.toList()));

        KeyVaultKeyAttribute attribute = newSubresource(KeyVaultKeyAttribute.class);
        attribute.copyFrom(key.attributes());
        setAttribute(attribute);

        KeyIdentifier keyIdentifier = key.keyIdentifier();
        setId(keyIdentifier.identifier());
        setName(keyIdentifier.name());
        setVersion(keyIdentifier.version());

        String vaultUri = keyIdentifier.vault();
        vaultUri = vaultUri.endsWith("/") ? vaultUri : vaultUri + "/";
        setVault(findById(KeyVaultResource.class, vaultUri));

    }

    @Override
    public boolean refresh() {
        Vault vault = getVault().getKeyVault();
        KeyBundle keyBundle = vault.client().getKey(vault.vaultUri(), getName());

        if (keyBundle == null) {
            return false;
        }

        copyFrom(keyBundle);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Vault vault = getVault().getKeyVault();

        CreateKeyRequest.Builder builder = new CreateKeyRequest.Builder(vault.vaultUri(), getName(), new JsonWebKeyType(getType()));
        builder.withAttributes(getAttribute().toKeyAttributes());
        builder.withKeyOperations(getOperations().stream().map(JsonWebKeyOperation::new).collect(Collectors.toList()));
        builder.withKeySize(getSize());

        if (getTags() != null) {
            builder.withTags(getTags());
        }

        KeyBundle keyBundle = vault.client().createKey(builder.build());

        copyFrom(keyBundle);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Vault vault = getVault().getKeyVault();

        UpdateKeyRequest.Builder builder = new UpdateKeyRequest.Builder(vault.vaultUri(), getName());
        builder.withAttributes(getAttribute().toKeyAttributes());
        builder.withKeyOperations(getOperations().stream().map(JsonWebKeyOperation::new).collect(Collectors.toList()));
        builder.withTags(getTags());

        vault.client().updateKey(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Vault vault = getVault().getKeyVault();

        vault.client().deleteKey(vault.vaultUri(), getName());
    }
}
