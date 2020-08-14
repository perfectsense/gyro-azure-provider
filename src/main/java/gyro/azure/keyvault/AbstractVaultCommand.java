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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.azure.AbstractAzureCommand;
import gyro.core.GyroException;
import gyro.core.command.GyroCommand;
import gyro.core.resource.Resource;
import gyro.core.scope.RootScope;

public abstract class AbstractVaultCommand extends AbstractAzureCommand implements GyroCommand {

    public static Vault getVault(String vaultResourceName, RootScope scope, Azure client) {
        Resource resource = scope.findResource("azure::key-vault::" + vaultResourceName);

        if (resource instanceof KeyVaultResource) {
            Vault vault = client.vaults().getById(((KeyVaultResource) resource).getId());

            if (vault == null) {
                throw new GyroException("The key-vault no longer exists!!");
            }

            return vault;

        } else {
            throw new GyroException(String.format("No 'key-vault' resource found with name - %s", vaultResourceName));
        }
    }

    Vault getVault(String vaultResourceName) {
        RootScope scope = getScope();
        Azure client = getClient();
        return getVault(vaultResourceName, scope, client);
    }
}
