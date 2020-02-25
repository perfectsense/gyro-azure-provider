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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.CertificatePermissions;
import com.microsoft.azure.management.keyvault.KeyPermissions;
import com.microsoft.azure.management.keyvault.Permissions;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.StoragePermissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.rest.ExpandableStringEnum;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class VaultAccessPolicy extends Diffable implements Copyable<AccessPolicy> {

    private String name;
    private Set<String> certificatePermissions;
    private Set<String> keyPermissions;
    private Set<String> secretPermissions;
    private Set<String> storagePermissions;
    private String objectId;

    /**
     * A set of allowed certificate access permissions. See `Certificate Access Policy <https://docs.microsoft.com/en-us/azure/key-vault/about-keys-secrets-and-certificates#certificate-access-control>`_.
     */
    @Updatable
    public Set<String> getCertificatePermissions() {
        if (certificatePermissions == null) {
            certificatePermissions = new HashSet<>();
        }

        return certificatePermissions;
    }

    public void setCertificatePermissions(Set<String> certificatePermissions) {
        this.certificatePermissions = certificatePermissions;
    }

    /**
     * A set of allowed key access permissions. See `Key Access Policy <https://docs.microsoft.com/en-us/azure/key-vault/about-keys-secrets-and-certificates#key-access-control>`_.
     */
    @Updatable
    public Set<String> getKeyPermissions() {
        if (keyPermissions == null) {
            keyPermissions = new HashSet<>();
        }

        return keyPermissions;
    }

    public void setKeyPermissions(Set<String> keyPermissions) {
        this.keyPermissions = keyPermissions;
    }

    /**
     * A set of allowed secret access permissions. See `Secret Access Policy <https://docs.microsoft.com/en-us/azure/key-vault/about-keys-secrets-and-certificates#secret-access-control>`_.
     */
    @Updatable
    public Set<String> getSecretPermissions() {
        if (secretPermissions == null) {
            secretPermissions = new HashSet<>();
        }

        return secretPermissions;
    }

    public void setSecretPermissions(Set<String> secretPermissions) {
        this.secretPermissions = secretPermissions;
    }

    /**
     * A set of allowed storage access permissions. See `Secret Access Policy <https://docs.microsoft.com/en-us/azure/key-vault/about-keys-secrets-and-certificates#storage-account-access-control>`_.
     */
    @Updatable
    public Set<String> getStoragePermissions() {
        if (storagePermissions == null) {
            storagePermissions = new HashSet<>();
        }

        return storagePermissions;
    }

    public void setStoragePermissions(Set<String> storagePermissions) {
        this.storagePermissions = storagePermissions;
    }

    /**
     * The service principal id of the user or application the access permissions are for.
     */
    @Required
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * The server defined name for the access policy.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(AccessPolicy policy) {
        setName(policy.name());
        setObjectId(policy.objectId());
        Permissions permissions = policy.permissions();
        if (permissions != null) {
            setCertificatePermissions(permissions.certificates() != null ? permissions.certificates()
                .stream()
                .map(ExpandableStringEnum::toString)
                .collect(
                    Collectors.toSet()) : null);
            setKeyPermissions(
                permissions.keys() != null ? permissions.keys().stream().map(ExpandableStringEnum::toString).collect(
                    Collectors.toSet()) : null);
            setSecretPermissions(permissions.secrets() != null ? permissions.secrets()
                .stream()
                .map(ExpandableStringEnum::toString)
                .collect(
                    Collectors.toSet()) : null);
            setStoragePermissions(permissions.storage() != null ? permissions.storage()
                .stream()
                .map(ExpandableStringEnum::toString)
                .collect(
                    Collectors.toSet()) : null);
        }
    }

    @Override
    public String primaryKey() {
        return getObjectId();
    }

    protected Vault.DefinitionStages.WithCreate createAccessPolicy(Vault.DefinitionStages.WithCreate withCreate) {
        AccessPolicy.DefinitionStages.WithAttach<Vault.DefinitionStages.WithCreate> withCreateWithAttach = withCreate
            .defineAccessPolicy()
            .forObjectId(getObjectId());

        if (!getCertificatePermissions().isEmpty()) {
            withCreateWithAttach = withCreateWithAttach
                .allowCertificatePermissions(getCertificatePermissions().stream()
                    .map(CertificatePermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getKeyPermissions().isEmpty()) {
            withCreateWithAttach = withCreateWithAttach
                .allowKeyPermissions(getKeyPermissions().stream()
                    .map(KeyPermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getSecretPermissions().isEmpty()) {
            withCreateWithAttach = withCreateWithAttach
                .allowSecretPermissions(getSecretPermissions().stream()
                    .map(SecretPermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getStoragePermissions().isEmpty()) {
            withCreateWithAttach = withCreateWithAttach
                .allowStoragePermissions(getStoragePermissions().stream()
                    .map(StoragePermissions::fromString)
                    .collect(Collectors.toList()));
        }

        return withCreateWithAttach.attach();
    }

    protected Vault.Update createAccessPolicy(Vault.Update update) {
        AccessPolicy.UpdateDefinitionStages.WithAttach<Vault.Update> updateWithAttach = update
            .defineAccessPolicy()
            .forObjectId(getObjectId());

        if (!getKeyPermissions().isEmpty()) {
            updateWithAttach = updateWithAttach
                .allowKeyPermissions(getKeyPermissions().stream()
                    .map(KeyPermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getSecretPermissions().isEmpty()) {
            updateWithAttach = updateWithAttach
                .allowSecretPermissions(getSecretPermissions().stream()
                    .map(SecretPermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getStoragePermissions().isEmpty()) {
            updateWithAttach = updateWithAttach
                .allowStoragePermissions(getStoragePermissions().stream()
                    .map(StoragePermissions::fromString)
                    .collect(Collectors.toList()));
        }

        return updateWithAttach.attach();
    }

    protected Vault.Update updateAccessPolicy(Vault.Update update) {
        AccessPolicy.Update policyUpdate = update.updateAccessPolicy(getObjectId());

        if (!getCertificatePermissions().isEmpty()) {
            policyUpdate = policyUpdate
                .allowCertificatePermissions(getCertificatePermissions().stream()
                    .map(CertificatePermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getKeyPermissions().isEmpty()) {
            policyUpdate = policyUpdate
                .allowKeyPermissions(getKeyPermissions().stream()
                    .map(KeyPermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getSecretPermissions().isEmpty()) {
            policyUpdate = policyUpdate
                .allowSecretPermissions(getSecretPermissions().stream()
                    .map(SecretPermissions::fromString)
                    .collect(Collectors.toList()));
        }

        if (!getStoragePermissions().isEmpty()) {
            policyUpdate = policyUpdate
                .allowStoragePermissions(getStoragePermissions().stream()
                    .map(StoragePermissions::fromString)
                    .collect(Collectors.toList()));
        }

        return policyUpdate.parent();
    }
}
