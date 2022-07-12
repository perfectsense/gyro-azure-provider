package gyro.azure.network;

import java.io.File;
import java.util.List;

import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "add-certificate",
    header = "Add a certificate to an Azure application gateway.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class AddApplicationGatewayCertificateCommand extends AbstractApplicationGatewayCommand {

    @Parameters(description = "The command requires two arguments. <application-gateway-name>: the application gateway resource name used in the config to which the certificate would be added. <cert-name>: name of the certificate to be added. <path>: the path to the certificate file (.pfx)", arity = "3")
    private List<String> arguments;

    @Option(names = "--password", description = "Password used to encrypt the certificate file.", arity = "0..1", interactive = true)
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

            GyroCore.ui().write("\nCertificate added.");

        } else {
            throw new GyroException(
                "'add-certificate' needs exactly three arguments, <application-gateway-name> <cert-name> <path>");
        }
    }
}
