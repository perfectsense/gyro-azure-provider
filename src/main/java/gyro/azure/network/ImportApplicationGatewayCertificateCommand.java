package gyro.azure.network;

import java.util.List;

import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import gyro.azure.keyvault.AbstractVaultCommand;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "import-certificate",
    header = "Import a certificate from an Azure vault to an application gateway.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class ImportApplicationGatewayCertificateCommand extends AbstractApplicationGatewayCommand {

    @Parameters(description = "The command requires four arguments. <application-gateway-name>: the application gateway resource name used in the config to which the certificate would be imported to. <cert-name>: name of the certificate to be created on the application gateway. <vault-name>: the key-vault resource name used in the config from which to import the certificate from. <vault-cert-name>: name of the certificate in the vault to be imported.", arity = "1")
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 4) {
            String applicationGatewayResourceName = arguments.get(0);
            String certificateName = arguments.get(1);
            String vaultResourceName = arguments.get(2);
            String vaultCertificateName = arguments.get(3);

            ApplicationGateway applicationGateway = getApplicationGateway(applicationGatewayResourceName);

            Vault vault = AbstractVaultCommand.getVault(vaultResourceName, getScope(), getResourceManagerClient());

            CertificateClient client = new CertificateClientBuilder()
                .vaultUrl(vault.vaultUri())
                .credential(null)
                .buildClient();

            KeyVaultCertificateWithPolicy certificate = client.getCertificate(vaultCertificateName);


            applicationGateway.update()
                .defineSslCertificate(certificateName)
                .withKeyVaultSecretId(certificate.getSecretId())
                .attach()
                .apply();

            GyroCore.ui().write("\nCertificate imported.");

        } else {
            throw new GyroException(
                "'import-certificate' needs exactly four arguments, <application-gateway-name> <cert-name> <vault-name> <vault-cert-name>");
        }
    }
}
