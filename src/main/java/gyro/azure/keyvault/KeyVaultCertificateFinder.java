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

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query key vault certificate.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    certificate: $(external-query azure::key-vault-certificate {resource-group: "resource-group-example", vault: "vault-example", name: "certificate-example"})
 */
@Type("key-vault-certificate")
public class KeyVaultCertificateFinder extends AzureFinder<KeyVaultCertificateWithPolicy, KeyVaultCertificateResource> {

    private String resourceGroup;
    private String vault;
    private String name;

    /**
     * The resource group for the vault where the certificate resides.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The vault where the certificate resides.
     */
    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    /**
     * The name of the certificate.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<KeyVaultCertificateWithPolicy> findAllAzure(AzureResourceManager client) {
        throw new UnsupportedOperationException("Finding all certificates without any filter is not supported!!");
    }

    @Override
    protected List<KeyVaultCertificateWithPolicy> findAzure(AzureResourceManager client, Map<String, String> filters) {
        List<KeyVaultCertificateWithPolicy> certificates = new ArrayList<>();
        Vault vault = client.vaults().getByResourceGroup(filters.get("resource-group"), filters.get("vault"));
        if (vault != null) {
            CertificateClient certificateClient = new CertificateClientBuilder()
                .vaultUrl(vault.vaultUri())
                .credential(getTokenCredential())
                .buildClient();

            try {
                KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate(filters.get("name"));
                certificates.add(certificate);
            } catch (ResourceNotFoundException ex) {
                // ignore
            }
        }

        return certificates;
    }
}
