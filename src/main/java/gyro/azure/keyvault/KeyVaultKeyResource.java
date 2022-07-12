package gyro.azure.keyvault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
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
public class KeyVaultKeyResource extends AzureResource implements Copyable<Key> {

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
    @ValidStrings({"EC", "EC-HSM", "RSA", "RSA-HSM", "OCT", "OCT-HSM"})
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
    @ValidStrings({"encrypt", "decrypt", "sign", "verify", "wrapKey", "unwrapKey", "import"})
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
    public void copyFrom(Key key) {
        setTags(key.tags());

        setOperations(key.getJsonWebKey().getKeyOps().stream().map(ExpandableStringEnum::toString).collect(Collectors.toList()));
        KeyVaultKeyAttribute attribute = newSubresource(KeyVaultKeyAttribute.class);
        attribute.copyFrom(key.attributes());
        setAttribute(attribute);

        setId(key.id());
        setName(key.name());
        setVersion(key.innerModel().getVersion());

        String vaultName = getId().split(".vault.azure.net")[0].split("://")[1];
        setVault(findById(KeyVaultResource.class, vaultName));
    }

    @Override
    public boolean refresh() {
        Vault vault = getVault().getKeyVault();

        Key key = vault.keys().getById(getId());

        if (key == null) {
            return false;
        }

        copyFrom(key);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Vault vault = getVault().getKeyVault();

        Key.DefinitionStages.WithCreate withCreate = vault.keys().define(getName())
            .withKeyTypeToCreate(KeyType.fromString(getType()));

        if (getSize() != null) {
            withCreate = withCreate.withKeySize(getSize());
        }

        if (getAttribute() != null) {
            withCreate = withCreate.withAttributes(getAttribute().toKeyProperties());
        }

        if (getOperations() != null) {
            withCreate = withCreate.withKeyOperations(getOperations().stream()
                .map(KeyOperation::fromString).collect(Collectors.toList()));
        }

        if (getTags() != null) {
            withCreate = withCreate.withTags(getTags());
        }

        Key key = withCreate.create();

        copyFrom(key);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Vault vault = getVault().getKeyVault();

        Key key = vault.keys().getById(getId());

        Key.Update update = key.update();

        if (changedFieldNames.contains("attribute")) {
            update = update.withAttributes(getAttribute().toKeyProperties());
        }

        if (changedFieldNames.contains("operations")) {
            update = update.withKeyOperations(getOperations().stream()
                .map(KeyOperation::fromString).collect(Collectors.toList()));
        }

        if (changedFieldNames.contains("tags")) {
            update = update.withTags(getTags());
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Vault vault = getVault().getKeyVault();

        vault.keys().deleteById(getId());
    }
}
