package gyro.azure.keyvault;

import java.util.List;

import com.microsoft.azure.management.keyvault.Vault;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "remove-certificate", description = "Remove a certificate from an Azure vault")
public class RemoveVaultCertificate extends AbstractVaultCommand {

    @Arguments(description = "The command requires two arguments. <vault-name>: the vault resource name used in the config from which the certificate would be removed. <cert-name>: name of the certificate to be removed.", required = true)
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 2) {
            String vaultResourceName = arguments.get(0);
            String certificateName = arguments.get(1);

            Vault vault = getVault(vaultResourceName);

            vault.client().deleteCertificate(vault.vaultUri(), certificateName);
            System.out.println("\nCertificate removed.");

        } else {
            throw new GyroException("'remove-certificate' needs exactly two arguments, <vault-name> and <cert-name>");
        }
    }
}
