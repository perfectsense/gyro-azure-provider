package gyro.azure.network;

import java.util.List;

import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.network.ApplicationGateway;
import gyro.azure.keyvault.AbstractVaultCommand;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "import-certificate", description = "Import a certificate from an Azure vault to an application gateway")
public class ImportApplicationGatewayCertificate extends AbstractApplicationGatewayCommand {

    @Arguments(description = "The command requires four arguments. <application-gateway-name>: the application gateway resource name used in the config to which the certificate would be imported to. <cert-name>: name of the certificate to be created on the application gateway. <vault-name>: the vault resource name used in the config from which to import the certificate from. <vault-cert-name>: name of the certificate in the vault to be imported.", required = true)
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 4) {
            String applicationGatewayResourceName = arguments.get(0);
            String certificateName = arguments.get(1);
            String vaultResourceName = arguments.get(2);
            String vaultCertificateName = arguments.get(3);

            ApplicationGateway applicationGateway = getApplicationGateway(applicationGatewayResourceName);

            Vault vault = AbstractVaultCommand.getVault(vaultResourceName, getScope(), getClient());

            CertificateBundle certificate = vault.client().getCertificate(vault.vaultUri(), vaultCertificateName);

            System.out.println("\n\n -> " + certificate.sid());

            applicationGateway.update()
                .defineSslCertificate(certificateName)
                .withKeyVaultSecretId(certificate.sid())
                .attach()
                .apply();

            System.out.println("\nCertificate imported.");

        } else {
            throw new GyroException(
                "'import-certificate' needs exactly four arguments, <application-gateway-name> <cert-name> <vault-name> <vault-cert-name>");
        }
    }
}
