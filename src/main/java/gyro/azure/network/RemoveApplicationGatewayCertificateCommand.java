package gyro.azure.network;

import java.util.List;

import com.microsoft.azure.management.network.ApplicationGateway;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "remove-certificate", description = "Remove a certificate from an Azure application gateway")
public class RemoveApplicationGatewayCertificateCommand extends AbstractApplicationGatewayCommand {

    @Arguments(description = "The command requires two arguments. <application-gateway-name>: the application gateway resource name used in the config from which the certificate would be removed. <cert-name>: name of the certificate to be removed.", required = true)
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 2) {
            String applicationGatewayResourceName = arguments.get(0);
            String certificateName = arguments.get(1);

            ApplicationGateway applicationGateway = getApplicationGateway(applicationGatewayResourceName);

            applicationGateway.update().withoutSslCertificate(certificateName).apply();

            System.out.println("\nCertificate removed.");

        } else {
            throw new GyroException("'remove-certificate' needs exactly two arguments, <application-gateway-name> and <cert-name>");
        }
    }
}
