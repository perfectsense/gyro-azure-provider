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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroCore;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;

/**
 * Creates a key vault certificate.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::key-vault-certificate vault-certificate-example
 *         name: "certificate-example"
 *         vault: $(azure::key-vault vault-example)
 *
 *         policy
 *             key-properties
 *                 exportable: false
 *                 reuse-key: false
 *                 size: 2048
 *                 type: "RSA"
 *             end
 *
 *             lifetime-action
 *                 action
 *                     type: "EmailContacts"
 *                 end
 *
 *                 trigger
 *                     lifetime-percentage: 90
 *                 end
 *             end
 *
 *             secret-properties
 *                 content-type: "application/x-pkcs12"
 *             end
 *
 *             x509-properties
 *                 key-usage: ["digitalSignature", "keyEncipherment"]
 *                 subject: "CN=a1.com"
 *                 validity-in-months:  2
 *                 ekus: ["1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"]
 *             end
 *
 *             attribute
 *                 "enabled" : true
 *                 "expires" : "2020-04-03T15:54:12.000Z"
 *             end
 *
 *             issuer-parameter
 *                 name: "Self"
 *             end
 *         end
 *     end
 */
@Type("key-vault-certificate")
public class KeyVaultCertificateResource extends AzureResource implements Copyable<KeyVaultCertificateWithPolicy> {

    private String name;
    private KeyVaultResource vault;
    private KeyVaultCertificatePolicy policy;
    private String version;
    private String id;
    private String secretId;
    private Map<String, String> tags;
    private Boolean enabled;

    /**
     * The name of the certificate.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The key vault under which the certificate is going to be created.
     */
    @Required
    public KeyVaultResource getVault() {
        return vault;
    }

    public void setVault(KeyVaultResource vault) {
        this.vault = vault;
    }

    /**
     * The policy config for the certificate.
     *
     * @subresource gyro.azure.keyvault.KeyVaultCertificatePolicy
     */
    @Required
    public KeyVaultCertificatePolicy getPolicy() {
        return policy;
    }

    public void setPolicy(KeyVaultCertificatePolicy policy) {
        this.policy = policy;
    }

    /**
     * The version of the certificate.
     */
    @Output
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The Id of the certificate.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The secret ID of the certificate.
     */
    @Output
    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    /**
     * Tags for the certificate.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * Enable or Disable the certificate for use.
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(KeyVaultCertificateWithPolicy certificate) {
        setName(certificate.getName());
        setVersion(certificate.getProperties().getVersion());
        setId(certificate.getId());
        setVault(findById(KeyVaultResource.class, certificate.getProperties().getVaultUrl()));
        setSecretId(certificate.getSecretId());
        setTags(certificate.getProperties().getTags());

        setPolicy(Optional.ofNullable(certificate.getPolicy())
            .map(o -> {
                KeyVaultCertificatePolicy certificatePolicy = newSubresource(KeyVaultCertificatePolicy.class);
                certificatePolicy.copyFrom(o);
                return certificatePolicy;
            }).orElse(null));
    }

    @Override
    public boolean refresh() {
        CertificateClient client = getClient();

        try {
            KeyVaultCertificateWithPolicy certificate = client.getCertificate(getName());
            copyFrom(certificate);
            return true;
        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CertificateClient client = getClient();

        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> policySyncPoller = client.beginCreateCertificate(
            getName(),
            getPolicy().toCertificatePolicy(),
            getEnabled(),
            getTags());

        KeyVaultCertificateWithPolicy certificate = null;

        try {
            policySyncPoller.waitUntil(Duration.ofMinutes(5), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
            certificate = policySyncPoller.getFinalResult();
        } catch (IllegalArgumentException ex) {
            // Do something
        }

        if (certificate != null) {
            copyFrom(certificate);

            state.save();

            if (getPolicy().getIssuerName().equals("Unknown")) {
                GyroCore.ui()
                    .write(
                        "\n@|blue Certificate created, but needs to be merged before use. "
                            + "Please use the Azure console to merge the certificate! |@\n\n");
            }
        }
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CertificateClient client = getClient();

        SyncPoller<DeletedCertificate, Void> syncPoller = client.beginDeleteCertificate(getName());
        try {
            syncPoller.waitUntil(Duration.ofMinutes(5), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
            syncPoller.getFinalResult();
        } catch (IllegalArgumentException ex) {
            // Do something
            // TODO verify certificate exception, also affect finders and command
        }
    }

    private CertificateClient getClient() {
        return new CertificateClientBuilder()
            .vaultUrl(getVault().getUrl())
            .credential(getTokenCredential())
            .buildClient();
    }
}
