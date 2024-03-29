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

import java.time.Duration;
import java.util.List;

import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "remove-certificate",
    description = "Remove a certificate from an Azure key vault",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class RemoveVaultCertificateCommand extends AbstractVaultCommand {

    @Parameters(description = "The command requires two arguments. <vault-name>: the key-vault resource name used in the config from which the certificate would be removed. <cert-name>: name of the certificate to be removed.", arity = "1")
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 2) {
            String vaultResourceName = arguments.get(0);
            String certificateName = arguments.get(1);

            Vault vault = getVault(vaultResourceName);

            CertificateClient client = new CertificateClientBuilder()
                .vaultUrl(vault.vaultUri())
                .credential(getTokenCredential())
                .buildClient();

            SyncPoller<DeletedCertificate, Void> syncPoller = client.beginDeleteCertificate(
                certificateName);
            try {
                syncPoller.waitUntil(Duration.ofMinutes(5), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
                syncPoller.getFinalResult();
            } catch (IllegalArgumentException ex) {
                // Do something
            }

            GyroCore.ui().write("\nCertificate removed.");

        } else {
            throw new GyroException("'remove-certificate' needs exactly two arguments, <vault-name> <cert-name>");
        }
    }
}
