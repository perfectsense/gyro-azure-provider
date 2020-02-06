package gyro.azure.network;

import java.io.File;
import java.util.List;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "add-certificate", description = "Add a certificate to an Azure application gateway")
public class AddApplicationGatewayCertificate extends AbstractApplicationGatewayCommand {

    @Arguments(description = "The command requires two arguments. <application-gateway-name>: the application gateway resource name used in the config to which the certificate would be added. <cert-name>: name of the certificate to be added. <path>: the path to the certificate file (.pfx)", required = true)
    private List<String> arguments;

    @Option(name = { "--password" }, description = "Password used to encrypt the certificate file")
    private String password;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 3) {
            String applicationGatewayResourceName = arguments.get(0);
            String certificateName = arguments.get(1);
            String certificatePath = arguments.get(2);

            ApplicationGateway applicationGateway = getApplicationGateway(applicationGatewayResourceName);

            applicationGateway.update().defineSslCertificate(certificateName)
                .withPfxFromFile(new File(certificatePath))
                .withPfxPassword(!ObjectUtils.isBlank(password) ? password : "")
                .attach().apply();

            System.out.println("\nCertificate added.");

        } else {
            throw new GyroException("'add-certificate' needs exactly three arguments, <application-gateway-name> <cert-name> <path>");
        }
    }
}
