/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.azure.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.resourcemanager.storage.models.CorsRule;
import com.azure.resourcemanager.storage.models.CorsRuleAllowedMethodsItem;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a cors rule
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      cors
 *          allowed-headers: ["*"]
 *          allowed-methods: ["GET"]
 *          allowed-origins: ["*"]
 *          exposed-headers: ["*"]
 *          max-age: 6
 *          type: "blob"
 *      end
 */
public class Cors extends Diffable implements Copyable<CorsRule> {

    private Set<String> allowedHeaders;
    private Set<String> allowedMethods;
    private Set<String> allowedOrigins;
    private Set<String> exposedHeaders;
    private Integer maxAge;
    private String type;

    /**
     * A list of the allowed headers.
     */
    @Required
    @Updatable
    public Set<String> getAllowedHeaders() {
        if (allowedHeaders == null) {
            allowedHeaders = new HashSet<>();
        }

        return allowedHeaders;
    }

    public void setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * A list of the allowed methods.
     */
    @Required
    @Updatable
    public Set<String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new HashSet<>();
        }

        return allowedMethods;
    }

    public void setAllowedMethods(Set<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * A list of the allowed origins.
     */
    @Required
    @Updatable
    public Set<String> getAllowedOrigins() {
        if (allowedOrigins == null) {
            allowedOrigins = new HashSet<>();
        }

        return allowedOrigins;
    }

    public void setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * A list of the exposed headers.
     */
    @Updatable
    public Set<String> getExposedHeaders() {
        if (exposedHeaders == null) {
            exposedHeaders = new HashSet<>();
        }

        return exposedHeaders;
    }

    public void setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * A maximum age, in seconds.
     */
    @Required
    @Updatable
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Specifies which service the rule belongs to.
     */
    @Required
    @ValidStrings({ "blob", "file", "queue", "table" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(CorsRule rule) {
        setAllowedHeaders(new HashSet<>(rule.allowedHeaders()));
        setAllowedOrigins(new HashSet<>(rule.allowedOrigins()));
        rule.allowedMethods().forEach(r -> getAllowedMethods().add(r.toString()));
        setExposedHeaders(new HashSet<>(rule.exposedHeaders()));
        setMaxAge(rule.maxAgeInSeconds());
    }

    public String primaryKey() {
        return String.format("%s/%s/%s/%s/%s/%s", getAllowedHeaders(), getAllowedMethods(),
            getAllowedOrigins(), getExposedHeaders(), getMaxAge(), getType());
    }

    public CorsRule toCors() {
        CorsRule rule = new CorsRule();

        rule.withAllowedHeaders(new ArrayList<>(getAllowedHeaders()));
        rule.withAllowedMethods(getAllowedMethods().stream()
            .map(CorsRuleAllowedMethodsItem::fromString).collect(Collectors.toList()));
        rule.withAllowedOrigins(new ArrayList<>(getAllowedOrigins()));
        rule.withExposedHeaders(new ArrayList<>(getExposedHeaders()));
        rule.withMaxAgeInSeconds(getMaxAge());

        return rule;
    }
}
