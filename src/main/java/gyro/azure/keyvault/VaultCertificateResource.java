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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.CertificateOperation;
import com.microsoft.azure.keyvault.requests.CreateCertificateRequest;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Vault;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;

/**
 * Creates a vault certificate.
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
public class VaultCertificateResource extends AzureResource implements Copyable<CertificateBundle> {

    private String name;
    private VaultResource vault;
    private Map<String, String> tags;
    private VaultCertificatePolicy policy;
    private String version;
    private String id;
    private String sid;
    private String secretId;
    private String kid;
    private String keyId;

    /**
     * The name of the certificate.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The vault under which the certificate is going to be created.
     */
    public VaultResource getVault() {
        return vault;
    }

    public void setVault(VaultResource vault) {
        this.vault = vault;
    }

    /**
     * Tags for the certificate
     */
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
     * The policy config for the certificate. (Required)
     *
     * @subresource gyro.azure.keyvault.VaultCertificatePolicy
     */
    @Required
    public VaultCertificatePolicy getPolicy() {
        return policy;
    }

    public void setPolicy(VaultCertificatePolicy policy) {
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
     * The SID of the certificate.
     */
    @Output
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
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
     * The KID of the certificate.
     */
    @Output
    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * The key ID of the certificate.
     */
    @Output
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    @Override
    public void copyFrom(CertificateBundle certificateBundle) {
        setName(certificateBundle.certificateIdentifier().name());
        setVersion(certificateBundle.certificateIdentifier().version());
        setId(certificateBundle.id());
        setVault(findById(VaultResource.class, certificateBundle.certificateIdentifier().vault()));
        setSecretId(certificateBundle.secretIdentifier().identifier());
        setSid(certificateBundle.sid());
        setKeyId(certificateBundle.keyIdentifier().identifier());
        setKid(certificateBundle.kid());

        setPolicy(Optional.ofNullable(certificateBundle.policy())
            .map(o -> {
                VaultCertificatePolicy certificatePolicy = newSubresource(VaultCertificatePolicy.class);
                certificatePolicy.copyFrom(o);
                return certificatePolicy;
            }).orElse(null));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        Vault vault = client.vaults().getById(getVault().getId());
        CertificateBundle certificateBundle = vault.client().getCertificate(vault.vaultUri(), getName());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        Vault vault = client.vaults().getById(getVault().getId());
        CreateCertificateRequest.Builder builder = new CreateCertificateRequest.Builder(vault.vaultUri(), getName());

        builder = builder.withPolicy(getPolicy().toCertificatePolicy());

        if (!getTags().isEmpty()) {
            builder = builder.withTags(getTags());
        }

        vault.client().createCertificate(builder.build());

        if (getPolicy().getIssuerParameter().getName().equals("Unknown")) {
            GyroCore.ui().write("\n@|blue Certificate created, but needs to be merged before use. Please use the Azure console to merge the certificate! |@\n\n");
        } else {
            Wait.atMost(1, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .until(() -> certificateCreationSuccess(vault));
        }
    }

    private boolean certificateCreationSuccess(Vault vault) {
        CertificateOperation operation = vault.client().getCertificateOperation(vault.vaultUri(), getName());

        if (operation.error() != null) {
            throw new GyroException(operation.error().message());
        }

        return operation.status().equals("completed");
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        Vault vault = client.vaults().getById(getVault().getId());
        vault.client().deleteCertificate(vault.vaultUri(), getName());
    }
}
