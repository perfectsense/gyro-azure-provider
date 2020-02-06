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
import java.util.stream.Collectors;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.CertificateItem;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.core.GyroException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "list-certificate", description = "List all certificates present in an Azure vault")
public class ListVaultCertificate extends AbstractVaultCommand {

    @Arguments(description = "The command requires one argument. <vault-name>: the vault resource name used in the config whose certificates would be listed", required = true)
    private List<String> arguments;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 1) {
            String vaultResourceName = arguments.get(0);

            Vault vault = getVault(vaultResourceName);

            PagedList<CertificateItem> certificateItemPagedList = vault.client().listCertificates(vault.vaultUri());
            if (!certificateItemPagedList.isEmpty()) {
                certificateItemPagedList.loadAll();
                System.out.println(certificateItemPagedList.stream()
                    .map(certificateItem -> certificateItem.identifier().name())
                    .collect(
                        Collectors.joining("\n")));
            } else {
                System.out.println("No certificates found!");
            }

        } else {
            throw new GyroException("'List-certificate' needs exactly one argument, <vault-name>");
        }
    }
}