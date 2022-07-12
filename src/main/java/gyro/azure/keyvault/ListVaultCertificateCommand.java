/*
 * Copyright 2020, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.keyvault;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "list-certificate",
    header = "List all certificates present in an Azure key vault.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class ListVaultCertificateCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires one argument. <vault-name>: the key-vault resource name used in the config whose certificates would be listed.", arity = "1")
    private List<String> arguments;

    @Option(names = "--show-thumbprint", description = "Show thumbprint of the certificate.")
    private boolean showThumbprint;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 1) {
            String vaultResourceName = arguments.get(0);

            Vault vault = getVault(vaultResourceName);

            CertificateClient client = new CertificateClientBuilder()
                .vaultUrl(vault.vaultUri())
                .credential(getTokenCredential())
                .buildClient();

            PagedIterable<CertificateProperties> certificateProperties = client.listPropertiesOfCertificates();

            AtomicBoolean found = new AtomicBoolean(false);
            certificateProperties.forEach(certProp -> {
                found.set(true);
                KeyVaultCertificateWithPolicy certificate = client.getCertificate(certProp.getName());

                StringBuilder sb = new StringBuilder();
                sb.append("\n***********************");
                sb.append(String.format("\nName: %s", certificate.getName()));
                sb.append(String.format("\nVersion: %s", certificate.getProperties().getVersion()));

                if (showThumbprint) {
                    sb.append(String.format(
                        "\nThumbprint: %s",
                        certificate.getProperties().getX509Thumbprint() != null ? new String(certificate.getProperties().getX509Thumbprint()) : null));
                }

                GyroCore.ui().write(sb.toString());
            });

            if (!found.get()) {
                GyroCore.ui().write("No certificates found!");
            }

        } else {
            throw new GyroException("'List-certificate' needs exactly one argument, <vault-name>");
        }
    }
}
