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

package gyro.azure.network;

import com.azure.resourcemanager.network.models.ApplicationGateway.DefinitionStages.WithCreate;
import com.azure.resourcemanager.network.models.ApplicationGateway.Update;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration.DefinitionStages;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration.UpdateDefinitionStages.WithTarget;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectType;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a Redirect Configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     redirect-configuration
 *         name: "redirect-configuration-example"
 *         type: "Temporary"
 *         target-listener: "listener-example"
 *         include-query-string: true
 *         include-path: true
 *     end
 */
public class RedirectConfiguration extends Diffable implements Copyable<ApplicationGatewayRedirectConfiguration> {

    private String name;
    private String type;
    private String targetListener;
    private String targetUrl;
    private Boolean includeQueryString;
    private Boolean includePath;

    /**
     * Name of the redirect configuration.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Type of the redirect configuration. Valid values are ``Permanent`` or ``Found`` or ``SeeOther`` or ``Temporary``
     */
    @ValidStrings({ "Permanent", "Found", "SeeOther", "Temporary" })
    @Required
    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Name of the target listener to be associated with this redirect configuration. Required if target url not present.
     */
    @Updatable
    public String getTargetListener() {
        return targetListener;
    }

    public void setTargetListener(String targetListener) {
        this.targetListener = targetListener;
    }

    /**
     * Target url to be associated with this redirect configuration. Required if target listener not present.
     */
    @Updatable
    public String getTargetUrl() {
        if (!ObjectUtils.isBlank(targetUrl)) {
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                targetUrl = "http://" + targetUrl;
            }
        }

        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * Include query string or not. Defaults to ``false``.
     */
    @Updatable
    public Boolean getIncludeQueryString() {
        if (includeQueryString == null) {
            includeQueryString = false;
        }

        return includeQueryString;
    }

    public void setIncludeQueryString(Boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    /**
     * Include path or not. Defaults to ``false``.
     */
    @Updatable
    public Boolean getIncludePath() {
        if (includePath == null) {
            includePath = false;
        }

        return includePath;
    }

    public void setIncludePath(Boolean includePath) {
        this.includePath = includePath;
    }

    @Override
    public void copyFrom(ApplicationGatewayRedirectConfiguration redirectConfiguration) {
        setName(redirectConfiguration.name());
        setType(redirectConfiguration.type().toString());
        setTargetListener(
            redirectConfiguration.targetListener() != null ? redirectConfiguration.targetListener().name() : null);
        setTargetUrl(redirectConfiguration.targetUrl());
        setIncludePath(redirectConfiguration.isPathIncluded());
        setIncludeQueryString(redirectConfiguration.isQueryStringIncluded());

    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithCreate createRedirectConfiguration(WithCreate attach) {
        DefinitionStages.WithTarget<WithCreate> withCreateWithTarget = attach.defineRedirectConfiguration(getName())
            .withType(ApplicationGatewayRedirectType.fromString(getType()));

        if (!ObjectUtils.isBlank(getTargetListener())) {
            if (getIncludePath() && getIncludeQueryString()) {
                attach = withCreateWithTarget.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .withQueryStringIncluded()
                    .attach();
            } else if (getIncludePath()) {
                attach = withCreateWithTarget.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .attach();
            } else if (getIncludeQueryString()) {
                attach = withCreateWithTarget.withTargetListener(getTargetListener())
                    .withQueryStringIncluded()
                    .attach();
            } else {
                attach = withCreateWithTarget.withTargetListener(getTargetListener())
                    .attach();
            }
        } else {
            if (getIncludeQueryString()) {
                attach = withCreateWithTarget.withTargetUrl(getTargetUrl())
                    .withQueryStringIncluded()
                    .attach();
            } else {
                attach = withCreateWithTarget.withTargetUrl(getTargetUrl())
                    .attach();
            }
        }

        return attach;
    }

    Update createRedirectConfiguration(Update update) {
        WithTarget<Update> updateWithTarget = update.defineRedirectConfiguration(getName())
            .withType(ApplicationGatewayRedirectType.fromString(getType()));

        if (!ObjectUtils.isBlank(getTargetListener())) {
            if (getIncludePath() && getIncludeQueryString()) {
                update = updateWithTarget.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .withQueryStringIncluded()
                    .attach();
            } else if (getIncludePath()) {
                update = updateWithTarget.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .attach();
            } else if (getIncludeQueryString()) {
                update = updateWithTarget.withTargetListener(getTargetListener())
                    .withQueryStringIncluded()
                    .attach();
            } else {
                update = updateWithTarget.withTargetListener(getTargetListener())
                    .attach();
            }
        } else {
            if (getIncludeQueryString()) {
                update = updateWithTarget.withTargetUrl(getTargetUrl())
                    .withQueryStringIncluded()
                    .attach();
            } else {
                update = updateWithTarget.withTargetUrl(getTargetUrl())
                    .attach();
            }
        }

        return update;
    }

    Update updateRedirectConfiguration(Update update) {
        ApplicationGatewayRedirectConfiguration.Update partialUpdate = update
            .updateRedirectConfiguration(getName())
            .withType(ApplicationGatewayRedirectType.fromString(getType()));

        if (!ObjectUtils.isBlank(getTargetListener())) {
            if (getIncludePath() && getIncludeQueryString()) {
                update = partialUpdate.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .withQueryStringIncluded()
                    .parent();
            } else if (getIncludePath()) {
                update = partialUpdate.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .parent();
            } else if (getIncludeQueryString()) {
                update = partialUpdate.withTargetListener(getTargetListener())
                    .withQueryStringIncluded()
                    .parent();
            } else {
                update = partialUpdate.withTargetListener(getTargetListener())
                    .parent();
            }
        } else {
            if (getIncludeQueryString()) {
                update = partialUpdate.withTargetUrl(getTargetUrl())
                    .withQueryStringIncluded()
                    .parent();
            } else {
                update = partialUpdate.withTargetUrl(getTargetUrl())
                    .parent();
            }
        }

        return update;
    }
}
