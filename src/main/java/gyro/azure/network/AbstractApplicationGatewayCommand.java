package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import gyro.azure.AbstractAzureCommand;
import gyro.core.GyroException;
import gyro.core.command.GyroCommand;
import gyro.core.resource.Resource;
import gyro.core.scope.RootScope;

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
            throw new GyroException(String.format(
                "No 'application-gateway' resource found with name - %s",
                applicationGatewayResourceName));
        }
    }
}
