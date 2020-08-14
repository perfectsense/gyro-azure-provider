package gyro.azure.keyvault;

import java.util.List;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "list-secret", description = "List all secrets present in an Azure key vault.", mixinStandardHelpOptions = true)
public class ListVaultSecretCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires one argument. <vault-name>: the key-vault resource name used in the config whose secrets would be listed.", arity = "1")
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 1) {
            String vaultResourceName = arguments.get(0);

            Vault vault = getVault(vaultResourceName);

            PagedList<SecretItem> secretItemPagedList = vault.client().listSecrets(vault.vaultUri());
            if (!secretItemPagedList.isEmpty()) {
                secretItemPagedList.loadAll();

                for (SecretItem secret : secretItemPagedList) {
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
