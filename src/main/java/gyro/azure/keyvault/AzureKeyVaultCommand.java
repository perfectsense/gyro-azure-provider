package gyro.azure.keyvault;

import gyro.core.command.GyroCommandGroup;
import picocli.CommandLine;

@CommandLine.Command(name = "key-vault", description = "Manage azure key-vault secrets, keys and certificates.", mixinStandardHelpOptions = true, subcommands = {
    AddVaultCertificateCommand.class,
    AddVaultSecretCommand.class,
    ListVaultCertificateCommand.class,
    ListVaultSecretCommand.class,
    RemoveVaultCertificateCommand.class,
    RemoveVaultSecretCommand.class})
public class AzureKeyVaultCommand implements GyroCommandGroup {

}
