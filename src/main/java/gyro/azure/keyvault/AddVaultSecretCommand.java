package gyro.azure.keyvault;

import java.time.ZoneOffset;
import java.util.List;

import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import org.joda.time.DateTime;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "add-secret",
    header = "Add a secret to an Azure key vault.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class AddVaultSecretCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires three arguments. <vault-name>: the key-vault resource name used in the config to which the secret would be added. <secret-name>: name of the secret to be added. <value>: the secret value", arity = "1")
    private List<String> arguments;

    @Option(names = "--content-type", description = "Content type for the secret.")
    private String contentType;

    @Option(names = "--expires", description = "A date time value value in UTC specifying the expiration time. Format 'YYYY-MM-DDTHH:MM:SS.sssZ'.")
    private String expires;

    @Option(names = "--not-before", description = "A date time value value in UTC specifying the expiration not before a specific time. Format 'YYYY-MM-DDTHH:MM:SS.sssZ'.")
    private String notBefore;

    @Option(names = "--enabled", description = "Enable/Disable the secret. Defaults to 'false'.")
    private boolean enabled;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 3) {
            String vaultResourceName = arguments.get(0);
            String secretName = arguments.get(1);
            String secretValue = arguments.get(2);

            Vault vault = getVault(vaultResourceName);

            Secret.DefinitionStages.WithCreate withCreate = vault.secrets().define(secretName).withValue(secretValue);

            SecretProperties properties = new SecretProperties();

            if (!ObjectUtils.isBlank(contentType)) {
                properties.setContentType(contentType);
                withCreate = withCreate.withContentType(contentType);
            }

            properties.setEnabled(enabled);
            properties.setExpiresOn(DateTime.parse(expires).toDate().toInstant().atOffset(ZoneOffset.UTC));
            properties.setNotBefore(DateTime.parse(notBefore).toDate().toInstant().atOffset(ZoneOffset.UTC));

            withCreate = withCreate.withAttributes(properties);
            withCreate.create();

            GyroCore.ui().write("\nSecret added.");

        } else {
            throw new GyroException("'add-secret' needs exactly three arguments, <vault-name> <secret-name> <value>");
        }
    }
}
