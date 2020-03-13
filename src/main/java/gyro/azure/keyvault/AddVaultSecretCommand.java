package gyro.azure.keyvault;

import java.util.List;

import com.microsoft.azure.keyvault.models.SecretAttributes;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.management.keyvault.Vault;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.joda.time.DateTime;

@Command(name = "add-secret", description = "Add a secret to an Azure key vault")
public class AddVaultSecretCommand extends AbstractVaultCommand {

    @Arguments(description = "The command requires three arguments. <vault-name>: the key-vault resource name used in the config to which the secret would be added. <secret-name>: name of the secret to be added. <value>: the secret value", required = true)
    private List<String> arguments;

    @Option(name = { "--content-type" }, description = "Content type for the secret")
    private String contentType;

    @Option(name = { "--expires" }, description = "A date time value value in UTC specifying the expiration time. Format 'YYYY-MM-DDTHH:MM:SS.sssZ'")
    private String expires;

    @Option(name = { "--not-before" }, description = "A date time value value in UTC specifying the expiration not before a specific time. Format 'YYYY-MM-DDTHH:MM:SS.sssZ'")
    private String notBefore;

    @Option(name = { "--enabled" }, description = "Enable/Disable the secret. Defaults to 'false'")
    private boolean enabled;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 3) {
            String vaultResourceName = arguments.get(0);
            String secretName = arguments.get(1);
            String secretValue = arguments.get(2);

            Vault vault = getVault(vaultResourceName);

            SetSecretRequest.Builder builder = new SetSecretRequest.Builder(vault.vaultUri(), secretName, secretValue);

            SecretAttributes attributes = new SecretAttributes();

            if (!ObjectUtils.isBlank(contentType)) {
                builder = builder.withContentType(contentType);
            }

            builder = builder.withAttributes(attributes.withEnabled(enabled)
                .withExpires(expires != null ? DateTime.parse(expires) : null)
                .withNotBefore(notBefore != null ? DateTime.parse(notBefore) : null));

            vault.client().setSecret(builder.build());
            GyroCore.ui().write("\nSecret added.");

        } else {
            throw new GyroException("'add-secret' needs exactly three arguments, <vault-name> <secret-name> <value>");
        }
    }
}
