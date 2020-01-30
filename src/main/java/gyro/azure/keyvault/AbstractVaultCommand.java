package gyro.azure.keyvault;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.azure.AbstractAzureCommand;
import gyro.azure.AzureCommand;
import gyro.core.GyroException;
import gyro.core.command.GyroCommand;
import gyro.core.resource.Resource;
import gyro.core.scope.RootScope;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;

public abstract class AbstractVaultCommand extends AbstractAzureCommand implements GyroCommand {

    Vault getVault(String vaultResourceName) {
        RootScope scope = getScope();

        Resource resource = scope.findResource("azure::vault::" + vaultResourceName);

        if (resource instanceof VaultResource) {
            Azure client = getClient();

            Vault vault = client.vaults().getById(((VaultResource) resource).getId());

            if (vault == null) {
                throw new GyroException("The vault no longer exists!!");
            }

            return vault;

        } else {
            throw new GyroException(String.format("No 'Vault' resource found with name - %s", vaultResourceName));
        }
    }

    public static void setVaultCommand(Cli.CliBuilder<Object> builder) {
        List<Class<?>> subTypesOf = new ArrayList<>(AzureCommand.getReflections().getSubTypesOf(AbstractVaultCommand.class));

        builder.withGroup("vault")
            .withDescription("Manage azure vault secrets, keys and certificates")
            .withDefaultCommand(Help.class)
            .withCommands(subTypesOf);
    }
}
