package gyro.azure.network;

import gyro.core.command.GyroCommandGroup;
import picocli.CommandLine;

@CommandLine.Command(name = "application-gateway",
    description = "Manage azure application gateway certificate.",
    subcommands = {
        AddApplicationGatewayCertificateCommand.class,
        ImportApplicationGatewayCertificateCommand.class,
        ListApplicationGatewayCertificateCommand.class,
        RemoveApplicationGatewayCertificateCommand.class
    }
)
public class AzureApplicationGatewayCommand implements GyroCommandGroup {

}
