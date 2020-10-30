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
import java.util.Set;
import java.util.stream.Stream;

import com.microsoft.azure.keyvault.models.KeyProperties;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidNumbers;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

public class KeyVaultCertificateKeyProperties extends Diffable implements Copyable<KeyProperties> {

    private Boolean exportable;
    private Boolean reuseKey;
    private Integer size;
    private String type;

    /**
     * When set to ``true`` allows the certificates private key to be exportable.
     */
    @Required
    public Boolean getExportable() {
        return exportable;
    }

    public void setExportable(Boolean exportable) {
        this.exportable = exportable;
    }

    /**
     * When set to ``true`` allows the certificate key to be reused or renewed.
     */
    @Required
    public Boolean getReuseKey() {
        return reuseKey;
    }

    public void setReuseKey(Boolean reuseKey) {
        this.reuseKey = reuseKey;
    }

    /**
     * The key size.
     */
    @ValidNumbers({2048, 3072, 4096})
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The key type. Currently only supported value is ``RSA``.
     */
    @ValidStrings("RSA")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    KeyProperties toKeyProperties() {
        return new KeyProperties()
            .withReuseKey(getReuseKey())
            .withKeyType(getType())
            .withKeySize(getSize())
            .withExportable(getExportable());
    }

    @Override
    public void copyFrom(KeyProperties keyProperties) {
        setExportable(keyProperties.exportable());
        setReuseKey(keyProperties.reuseKey());
        setType(keyProperties.keyType());
        setSize(keyProperties.keySize());
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getSize() != null && configuredFields.contains("size") && Stream.of(2048, 3072, 4096).noneMatch(o -> getSize().equals(o))) {
            errors.add(new ValidationError(this, "size", "Valid value for `size` are 2048, 3072 or 4096"));
        }

        return errors;
    }
}
