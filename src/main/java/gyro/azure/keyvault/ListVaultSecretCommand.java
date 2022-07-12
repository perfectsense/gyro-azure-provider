package gyro.azure.keyvault;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.azure.resourcemanager.keyvault.models.Vault;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "list-secret",
    header = "List all secrets present in an Azure key vault.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class ListVaultSecretCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires one argument. <vault-name>: the key-vault resource name used in the config whose secrets would be listed.", arity = "1")
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 1) {
            String vaultResourceName = arguments.get(0);

            Vault vault = getVault(vaultResourceName);

            AtomicBoolean found = new AtomicBoolean(false);
            vault.secrets().list().forEach(secret -> {
                StringBuilder sb = new StringBuilder();
                sb.append("\n***********************");
                sb.append(String.format("\nName: %s", secret.name()));
                sb.append(String.format("\nContent Type: %s", secret.contentType()));

                GyroCore.ui().write(sb.toString());
                found.set(true);
            });

            if (!found.get()) {
                GyroCore.ui().write("No secrets found!");
            }

        } else {
            throw new GyroException("'List-secrets' needs exactly one argument, <vault-name>");
        }
    }
}
