package gyro.azure.keyvault;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.azure.AzureResource;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public abstract class KeyVaultInnerResource extends AzureResource {
    private KeyVaultResource vault;
    private Map<String, String> tags;

    /**
     * The key vault under which the key vault inner resource is going to be created. (Required)
     */
    @Required
    public KeyVaultResource getVault() {
        return vault;
    }

    public void setVault(KeyVaultResource vault) {
        this.vault = vault;
    }

    /**
     * Tags for the key vault inner resource.
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

    Vault getKeyVault() {
        Azure client = createClient();

       return client.vaults().getById(getVault().getId());
    }
}
