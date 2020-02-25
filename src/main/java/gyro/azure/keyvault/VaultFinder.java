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

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
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
public class VaultFinder extends AzureFinder<Vault, VaultResource> {

    private String resourceGroup;
    private String name;

    /**
     * The name of the resource group the vault belongs to.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The name of the vault.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Vault> findAllAzure(Azure client) {
        List<Vault> vaults = new ArrayList<>();
        List<String> resourceGroups = client.resourceGroups().list().stream().map(HasName::name).collect(Collectors.toList());
        resourceGroups.forEach(o -> {
            PagedList<Vault> vaultPagedList = client.vaults().listByResourceGroup(o);
            vaultPagedList.loadAll();
            vaults.addAll(vaultPagedList);
        });

        return vaults;
    }

    @Override
    protected List<Vault> findAzure(Azure client, Map<String, String> filters) {
        List<Vault> vaults = new ArrayList<>();

        if (filters.containsKey("resource-group")) {
            if (filters.containsKey("name")) {
                Vault vault = client.vaults().getByResourceGroup(filters.get("resource-group"), filters.get("name"));

                if (vault != null) {
                    vaults.add(vault);
                }
            } else {
                PagedList<Vault> vaultPagedList = client.vaults().listByResourceGroup(filters.get("resource-group"));
                if (vaultPagedList != null) {
                    vaultPagedList.loadAll();
                    vaults.addAll(vaultPagedList);
                }
            }
        }

        return vaults;
    }
}
