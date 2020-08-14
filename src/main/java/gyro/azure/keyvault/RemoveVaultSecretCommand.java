package gyro.azure.keyvault;

import java.util.List;

import com.microsoft.azure.management.keyvault.Vault;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "remove-secret", description = "Remove a secret from an Azure key vault.", mixinStandardHelpOptions = true)
public class RemoveVaultSecretCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires two arguments. <vault-name>: the key-vault resource name used in the config from which the secret would be removed. <secret-name>: name of the secret to be removed.", arity = "1")
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 2) {
            String vaultResourceName = arguments.get(0);
            String secretName = arguments.get(1);

            Vault vault = getVault(vaultResourceName);

            vault.client().deleteSecret(vault.vaultUri(), secretName);
            GyroCore.ui().write("\nSecret removed.");

        } else {
            throw new GyroException("'remove-secret' needs exactly two arguments, <vault-name> <secret-name>");
        }
    }
}