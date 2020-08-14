package gyro.azure.network;

import java.util.List;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "remove-certificate", description = "Remove a certificate from an Azure application gateway.", mixinStandardHelpOptions = true)
public class RemoveApplicationGatewayCertificateCommand extends AbstractApplicationGatewayCommand {

    @Parameters(description = "The command requires two arguments. <application-gateway-name>: the application gateway resource name used in the config from which the certificate would be removed. <cert-name>: name of the certificate to be removed.", arity = "1")
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 2) {
            String applicationGatewayResourceName = arguments.get(0);
            String certificateName = arguments.get(1);

            ApplicationGateway applicationGateway = getApplicationGateway(applicationGatewayResourceName);

            ApplicationGatewayListener listener = applicationGateway.listeners()
                .values()
                .stream()
                .filter(o -> o.sslCertificate() != null && o.sslCertificate().name().equals(certificateName))
                .findFirst()
                .orElse(null);

            if (listener == null) {
                applicationGateway.update().withoutSslCertificate(certificateName).apply();
                GyroCore.ui().write("\nCertificate removed.");
            } else {
                throw new GyroException(String.format(
                    "Certificate '%s' cannot be removed as it is being used by listener '%s'.",
                    certificateName,
                    listener.name()));
            }

        } else {
            throw new GyroException(
                "'remove-certificate' needs exactly two arguments, <application-gateway-name> and <cert-name>");
        }
    }
}
