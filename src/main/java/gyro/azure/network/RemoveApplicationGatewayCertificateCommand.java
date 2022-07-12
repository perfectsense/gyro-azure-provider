package gyro.azure.network;

import java.util.List;

import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "remove-certificate",
    header = "Remove a certificate from an Azure application gateway.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
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
