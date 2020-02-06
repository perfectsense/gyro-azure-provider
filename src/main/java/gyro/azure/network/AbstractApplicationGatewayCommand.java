package gyro.azure.network;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import gyro.azure.AbstractAzureCommand;
import gyro.azure.AzureCommand;
import gyro.core.GyroException;
import gyro.core.command.GyroCommand;
import gyro.core.resource.Resource;
import gyro.core.scope.RootScope;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;

public abstract class AbstractApplicationGatewayCommand extends AbstractAzureCommand implements GyroCommand {

    ApplicationGateway getApplicationGateway(String applicationGatewayResourceName) {
        RootScope scope = getScope();

        Resource resource = scope.findResource("azure::application-gateway::" + applicationGatewayResourceName);

        if (resource instanceof ApplicationGatewayResource) {
            Azure client = getClient();

            ApplicationGateway applicationGateway = client.applicationGateways()
                .getById(((ApplicationGatewayResource) resource).getId());

            if (applicationGateway == null) {
                throw new GyroException("The application gateway no longer exists!!");
            }

            return applicationGateway;
        } else {
            throw new GyroException(String.format("No 'application-gateway' resource found with name - %s", applicationGatewayResourceName));
        }
    }

    public static void setApplicationGatewayCommand(Cli.CliBuilder<Object> builder) {
        List<Class<?>> subTypesOf = new ArrayList<>(AzureCommand.getReflections().getSubTypesOf(AbstractApplicationGatewayCommand.class));

        builder.withGroup("application-gateway")
            .withDescription("Manage azure application gateway certificates")
            .withDefaultCommand(Help.class)
            .withCommands(subTypesOf);
    }
}
