package gyro.azure.keyvault;

import gyro.core.command.GyroCommandGroup;
import gyro.core.command.VersionCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "key-vault",
    description = "Manage azure key-vault secrets, keys and certificates.",
    synopsisHeading = "%n",
    header = "Add, remove, or list certificates and secrets of key-vault.",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    commandListHeading = "%nCommands:%n",
    usageHelpWidth = 100,
    mixinStandardHelpOptions = true,
    versionProvider = VersionCommand.class,
    subcommands = {
        AddVaultCertificateCommand.class,
        AddVaultSecretCommand.class,
        ListVaultCertificateCommand.class,
        ListVaultSecretCommand.class,
        RemoveVaultCertificateCommand.class,
        RemoveVaultSecretCommand.class
    }
)
public class AzureKeyVaultCommand implements GyroCommandGroup {

}
