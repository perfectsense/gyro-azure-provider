package gyro.azure.keyvault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query key vault key.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    certificate: $(external-query azure::key-vault-key {resource-group: "resource-group-example", vault: "vault-example", name: "key-example"})
 */
@Type("key-vault-key")
public class KeyVaultKeyFinder extends AzureFinder<AzureResourceManager, Key, KeyVaultKeyResource> {

    private String resourceGroup;
    private String vault;
    private String name;

    /**
     * The resource group for the vault where the key resides.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The vault where the key resides.
     */
    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    /**
     * The name of the key.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    protected List<Key> findAllAzure(AzureResourceManager client) {
        throw new UnsupportedOperationException("Finding all keys without any filter is not supported!!");
    }

    @Override
    protected List<Key> findAzure(AzureResourceManager client, Map<String, String> filters) {
        List<Key> keys = new ArrayList<>();
        Vault vault = client.vaults().getByResourceGroup(filters.get("resource-group"), filters.get("vault"));
        if (vault != null) {
            keys.add(vault.keys().getByName(filters.get("name")));
        }

        return keys;
    }
}
