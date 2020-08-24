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

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.CertificateItem;
import com.microsoft.azure.management.keyvault.Vault;
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

            PagedList<CertificateItem> certificateItemPagedList = vault.client().listCertificates(vault.vaultUri());
            if (!certificateItemPagedList.isEmpty()) {
                certificateItemPagedList.loadAll();

                for (CertificateItem certificate : certificateItemPagedList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n***********************");
                    sb.append(String.format("\nName: %s", certificate.identifier().name()));
                    sb.append(String.format("\nVersion: %s", certificate.identifier().version()));

                    if (showThumbprint) {
                        sb.append(String.format(
                            "\nThumbprint: %s",
                            certificate.x509Thumbprint() != null ? new String(certificate.x509Thumbprint()) : null));
                    }

                    GyroCore.ui().write(sb.toString());
                }
            } else {
                GyroCore.ui().write("No certificates found!");
            }

        } else {
            throw new GyroException("'List-certificate' needs exactly one argument, <vault-name>");
        }
    }
}
