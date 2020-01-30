package gyro.azure.keyvault;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import com.microsoft.azure.keyvault.requests.ImportCertificateRequest;
import com.microsoft.azure.management.keyvault.Vault;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "add-certificate", description = "Add a certificate to an Azure vault")
public class AddVaultCertificate extends AbstractVaultCommand {

    @Arguments(description = "The command requires three arguments. <vault-name>: the vault resource name used in the config to which the certificate would be added. <cert-name>: name of the certificate to be added. <path>: the path to the certificate file (.pfx)", required = true)
    private List<String> arguments;

    @Option(name = { "--password" }, description = "Password used to encrypt the certificate file")
    private String password;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 3) {
            String vaultResourceName = arguments.get(0);
            String certificateName = arguments.get(1);
            String certificatePath = arguments.get(2);

            Vault vault = getVault(vaultResourceName);

            ImportCertificateRequest.Builder builder = new ImportCertificateRequest.Builder(
                vault.vaultUri(),
                certificateName,
                Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(certificatePath))));

            if (!ObjectUtils.isBlank(password)) {
                builder = builder.withPassword(password);
            }

            vault.client().importCertificate(builder.build());
            System.out.println("\nCertificate added.");

        } else {
            throw new GyroException("'add-certificate' needs exactly three arguments, <vault-name> <cert-name> <path>");
        }
    }
}
