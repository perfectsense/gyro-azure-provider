package gyro.azure.keyvault;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
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
public class KeyVaultKeyResource extends AzureResource implements Copyable<KeyVaultKey> {

    private String name;
    private KeyVaultResource vault;
    private String type;
    private KeyVaultKeyAttribute attribute;
    private Integer size;
    private List<String> operations;
    private KeyCurveName curveName;
    private String version;
    private String id;
    private Map<String, String> tags;

    /**
     * The name of the key.
     */
    @Id
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
     * The elliptic curve name
     */
    @Updatable
    @ValidStrings({"P-256", "P-384", "P-521", "P-256K"})
    public KeyCurveName getCurveName() {
        return curveName;
    }

    public void setCurveName(KeyCurveName curveName) {
        this.curveName = curveName;
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
    public void copyFrom(KeyVaultKey key) {
        KeyVaultKeyAttribute attribute = newSubresource(KeyVaultKeyAttribute.class);
        attribute.copyFrom(key.getProperties());
        setAttribute(attribute);
        setTags(key.getProperties().getTags());
        setVersion(key.getProperties().getVersion());

        setOperations(key.getKeyOperations().stream().map(KeyOperation::toString).collect(Collectors.toList()));
        setId(key.getId());
        setName(key.getName());

        String vaultName = getId().split(".vault.azure.net")[0].split("://")[1];
        setVault(findById(KeyVaultResource.class, vaultName));
    }

    @Override
    public boolean refresh() {
        KeyClient keyClient = getKeyClient();

        KeyVaultKey key = keyClient.getKey(getName());

        if (key == null) {
            return false;
        }

        copyFrom(key);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        KeyClient keyClient = getKeyClient();
        KeyVaultKey key = null;

        if (getType().equals("EC") || getType().equals("EC-HSM")) {
            CreateEcKeyOptions createKeyOptions = new CreateEcKeyOptions(getName());

            if (getAttribute() != null) {
                createKeyOptions = createKeyOptions.setEnabled(getAttribute().getEnabled());
                createKeyOptions = createKeyOptions.setExpiresOn(
                    getAttribute().getExpires() != null ? OffsetDateTime.parse(getAttribute().getExpires()) : null);
                createKeyOptions = createKeyOptions.setNotBefore(
                    getAttribute().getNotBefore() != null ? OffsetDateTime.parse(getAttribute().getNotBefore()) : null);
            }

            if (getOperations() != null) {
                createKeyOptions = createKeyOptions.setKeyOperations(
                    getOperations().stream().map(KeyOperation::fromString).toArray(KeyOperation[]::new));
            }

            if (getTags() != null) {
                createKeyOptions = createKeyOptions.setTags(getTags());
            }

            if (getType().equals("EC-HSM")) {
                createKeyOptions = createKeyOptions.setHardwareProtected(true);
            }

            if (getCurveName() != null) {
                createKeyOptions = createKeyOptions.setCurveName(getCurveName());
            }

            key = keyClient.createEcKey(createKeyOptions);

        } else if (getType().equals("RSA") || getType().equals("RSA-HSM")) {
            CreateRsaKeyOptions createKeyOptions = new CreateRsaKeyOptions(getName());

            if (getAttribute() != null) {
                createKeyOptions = createKeyOptions.setEnabled(getAttribute().getEnabled());
                createKeyOptions = createKeyOptions.setExpiresOn(
                    getAttribute().getExpires() != null ? OffsetDateTime.parse(getAttribute().getExpires()) : null);
                createKeyOptions = createKeyOptions.setNotBefore(
                    getAttribute().getNotBefore() != null ? OffsetDateTime.parse(getAttribute().getNotBefore()) : null);
            }

            if (getOperations() != null) {
                createKeyOptions = createKeyOptions.setKeyOperations(
                    getOperations().stream().map(KeyOperation::fromString).toArray(KeyOperation[]::new));
            }

            if (getTags() != null) {
                createKeyOptions = createKeyOptions.setTags(getTags());
            }

            if (getType().equals("RSA-HSM")) {
                createKeyOptions = createKeyOptions.setHardwareProtected(true);
            }

            if (getSize() != null) {
                createKeyOptions = createKeyOptions.setKeySize(getSize());
            }

            key = keyClient.createRsaKey(createKeyOptions);

        } else {
            CreateOctKeyOptions createKeyOptions = new CreateOctKeyOptions(getName());

            if (getAttribute() != null) {
                createKeyOptions = createKeyOptions.setEnabled(getAttribute().getEnabled());
                createKeyOptions = createKeyOptions.setExpiresOn(
                    getAttribute().getExpires() != null ? OffsetDateTime.parse(getAttribute().getExpires()) : null);
                createKeyOptions = createKeyOptions.setNotBefore(
                    getAttribute().getNotBefore() != null ? OffsetDateTime.parse(getAttribute().getNotBefore()) : null);
            }

            if (getOperations() != null) {
                createKeyOptions = createKeyOptions.setKeyOperations(
                    getOperations().stream().map(KeyOperation::fromString).toArray(KeyOperation[]::new));
            }

            if (getTags() != null) {
                createKeyOptions = createKeyOptions.setTags(getTags());
            }

            if (getType().equals("OCT-HSM")) {
                createKeyOptions = createKeyOptions.setHardwareProtected(true);
            }

            key = keyClient.createOctKey(createKeyOptions);
        }

        copyFrom(key);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        KeyClient keyClient = getKeyClient();

        KeyVaultKey key = keyClient.getKey(getName());

        if (changedFieldNames.contains("attribute")) {
            key.getProperties().setEnabled(getAttribute().getEnabled());
            key.getProperties().setExpiresOn(
                getAttribute().getExpires() != null ? OffsetDateTime.parse(getAttribute().getExpires()) : null);
            key.getProperties().setNotBefore(
                getAttribute().getNotBefore() != null ? OffsetDateTime.parse(getAttribute().getNotBefore()) : null);
        }

        if (changedFieldNames.contains("tags")) {
            key.getProperties().setTags(getTags());
        }

        keyClient.updateKeyProperties(key.getProperties());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        KeyClient keyClient = getKeyClient();

        SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey(getName());
        deletedKeyPoller.poll();
        deletedKeyPoller.waitForCompletion();
    }

    public KeyClient getKeyClient() {
        return new KeyClientBuilder().credential(getTokenCredential()).vaultUrl(getVault().getUrl()).buildClient();
    }
}
