package gyro.azure.keyvault;

import java.util.List;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "list-secret", description = "List all secrets present in an Azure key vault")
public class ListVaultSecretCommand extends AbstractVaultCommand {

    @Arguments(description = "The command requires one argument. <vault-name>: the key-vault resource name used in the config whose secrets would be listed", required = true)
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 1) {
            String vaultResourceName = arguments.get(0);

            Vault vault = getVault(vaultResourceName);

            PagedList<SecretItem> secretItemPagedList = vault.client().listSecrets(vault.vaultUri());
            if (!secretItemPagedList.isEmpty()) {
                secretItemPagedList.loadAll();

                for (SecretItem secret: secretItemPagedList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n***********************");
                    sb.append(String.format("\nName: %s", secret.identifier().name()));
                    sb.append(String.format("\nVersion: %s", secret.identifier().version()));
                    sb.append(String.format("\nContent Type: %s", secret.contentType()));

                    GyroCore.ui().write(sb.toString());
                }
            } else {
                GyroCore.ui().write("No secrets found!");
            }

        } else {
            throw new GyroException("'List-secrets' needs exactly one argument, <vault-name>");
        }
    }
}
