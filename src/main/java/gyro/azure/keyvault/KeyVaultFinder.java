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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query key vault.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    identity: $(external-query azure::key-vault {resource-group: "resource-group-example", name: "vault-example"})
 */
@Type("key-vault")
public class KeyVaultFinder extends AzureFinder<AzureResourceManager, Vault, KeyVaultResource> {

    private String resourceGroup;
    private String name;

    /**
     * The name of the resource group the key vault belongs to.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The name of the key vault.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Vault> findAllAzure(AzureResourceManager client) {
        List<Vault> vaults = new ArrayList<>();
        List<String> resourceGroups = client.resourceGroups().list().stream().map(HasName::name).collect(Collectors.toList());
        resourceGroups.forEach(o -> {
            List<Vault> vaultList = client.vaults()
                .listByResourceGroup(o).stream().collect(Collectors.toList());
            vaults.addAll(vaultList);
        });

        return vaults;
    }

    @Override
    protected List<Vault> findAzure(AzureResourceManager client, Map<String, String> filters) {
        List<Vault> vaults = new ArrayList<>();

        if (filters.containsKey("resource-group")) {
            if (filters.containsKey("name")) {
                Vault vault = client.vaults()
                    .getByResourceGroup(filters.get("resource-group"), filters.get("name"));

                if (vault != null) {
                    vaults.add(vault);
                }
            } else {
                List<Vault> vaultList = client.vaults().listByResourceGroup(filters.get("resource-group")).stream().collect(Collectors.toList());
                if (!vaultList.isEmpty()) {
                    vaults.addAll(vaultList);
                }
            }
        }

        return vaults;
    }
}
