package gyro.azure.network;

import java.util.concurrent.Callable;

import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.ApplicationGateway;
import gyro.azure.AbstractAzureCommand;
import gyro.core.GyroException;
import gyro.core.command.GyroCommand;
import gyro.core.resource.Resource;
import gyro.core.scope.RootScope;

public abstract class AbstractApplicationGatewayCommand extends AbstractAzureCommand
    implements GyroCommand, Callable<Integer> {

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

    com.azure.resourcemanager.network.models.ApplicationGateway getApplicationGatewayResourceManager(String applicationGatewayResourceName) {
        RootScope scope = getScope();

        Resource resource = scope.findResource("azure::application-gateway::" + applicationGatewayResourceName);

        if (resource instanceof ApplicationGatewayResource) {
            AzureResourceManager client = getResourceManagerClient();

            com.azure.resourcemanager.network.models.ApplicationGateway applicationGateway = client.applicationGateways()
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

    @Override
    public Integer call() throws Exception {
        execute();
        return 0;
    }
}
