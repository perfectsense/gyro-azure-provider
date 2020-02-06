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

import com.microsoft.azure.management.keyvault.Vault;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "remove-certificate", description = "Remove a certificate from an Azure vault")
public class RemoveVaultCertificateCommand extends AbstractVaultCommand {

    @Arguments(description = "The command requires two arguments. <vault-name>: the vault resource name used in the config from which the certificate would be removed. <cert-name>: name of the certificate to be removed.", required = true)
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 2) {
            String vaultResourceName = arguments.get(0);
            String certificateName = arguments.get(1);

            Vault vault = getVault(vaultResourceName);

            vault.client().deleteCertificate(vault.vaultUri(), certificateName);
            System.out.println("\nCertificate removed.");

        } else {
            throw new GyroException("'remove-certificate' needs exactly two arguments, <vault-name> <cert-name>");
        }
    }
}
