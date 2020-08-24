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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import com.microsoft.azure.keyvault.requests.ImportCertificateRequest;
import com.microsoft.azure.management.keyvault.Vault;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "add-certificate",
    header = "Add a certificate to an Azure key vault.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class AddVaultCertificateCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires three arguments. <vault-name>: the key-vault resource name used in the config to which the certificate would be added. <cert-name>: name of the certificate to be added. <path>: the path to the certificate file (.pfx)", arity = "1")
    private List<String> arguments;

    @Option(names = "--password", description = "Password used to encrypt the certificate file.", arity = "0..1", interactive = true)
    private String password;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 3) {
            String vaultResourceName = arguments.get(0);
            String certificateName = arguments.get(1);
            String certificatePath = arguments.get(2);

            Vault vault = getVault(vaultResourceName);

            ImportCertificateRequest.Builder builder = new ImportCertificateRequest.Builder(
                vault.vaultUri(),
                certificateName,
                Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(certificatePath))));

            if (!ObjectUtils.isBlank(password)) {
                builder = builder.withPassword(password);
            }

            vault.client().importCertificate(builder.build());
            GyroCore.ui().write("\nCertificate added.");

        } else {
            throw new GyroException("'add-certificate' needs exactly three arguments, <vault-name> <cert-name> <path>");
        }
    }
}
