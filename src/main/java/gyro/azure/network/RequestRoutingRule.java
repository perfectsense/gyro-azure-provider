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

import com.azure.resourcemanager.network.models.ApplicationGateway.DefinitionStages.WithRequestRoutingRule;
import com.azure.resourcemanager.network.models.ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate;
import com.azure.resourcemanager.network.models.ApplicationGateway.Update;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule.DefinitionStages;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule.UpdateDefinitionStages.WithBackendHttpConfigOrRedirect;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

/**
 * Creates a Request Routing Rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     request-routing-rule
 *         name: "request-routing-rule-example"
 *         listener: "listener-example"
 *         backend: "backend-example"
 *         backend-http-configuration: "backend-http-configuration-example"
 *     end
 */
public class RequestRoutingRule extends Diffable implements Copyable<ApplicationGatewayRequestRoutingRule> {

    private String name;
    private String listener;
    private String backend;
    private String backendHttpConfiguration;
    private String redirectConfiguration;

    /**
     * Name of the rule.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Name of the listener to associated with the rule.
     */
    @Required
    @Updatable
    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }

    /**
     * Name of the backend to be associated with the rule. Required if redirection not present.
     */
    @Updatable
    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    /**
     * Name of the backend http configuration to be associated with the rule. Required if redirection not present.
     */
    @Updatable
    public String getBackendHttpConfiguration() {
        return backendHttpConfiguration;
    }

    public void setBackendHttpConfiguration(String backendHttpConfiguration) {
        this.backendHttpConfiguration = backendHttpConfiguration;
    }

    /**
     * Name of the redirect configuration to be associated with the rule. Required if backend not present.
     */
    @Updatable
    public String getRedirectConfiguration() {
        return redirectConfiguration;
    }

    public void setRedirectConfiguration(String redirectConfiguration) {
        this.redirectConfiguration = redirectConfiguration;
    }

    @Override
    public void copyFrom(ApplicationGatewayRequestRoutingRule rule) {
        setBackend(rule.backend() != null ? rule.backend().name() : null);
        setListener(rule.listener() != null ? rule.listener().name() : null);
        setBackendHttpConfiguration(
            rule.backendHttpConfiguration() != null ? rule.backendHttpConfiguration().name() : null);
        setRedirectConfiguration(rule.redirectConfiguration() != null ? rule.redirectConfiguration().name() : null);
        setName(rule.name());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithRequestRoutingRuleOrCreate createRequestRoutingRule(
        WithRequestRoutingRule preAttach,
        WithRequestRoutingRuleOrCreate attach) {
        DefinitionStages.WithBackendHttpConfigOrRedirect<WithRequestRoutingRuleOrCreate> partialAttach;
        if (attach == null) {
            partialAttach = preAttach.defineRequestRoutingRule(getName()).fromListener(getListener());
        } else {
            partialAttach = attach.defineRequestRoutingRule(getName())
                .fromListener(getListener());
        }

        if (!ObjectUtils.isBlank(getRedirectConfiguration())) {
            attach = partialAttach.withRedirectConfiguration(getRedirectConfiguration())
                .attach();
        } else {
            attach = partialAttach.toBackendHttpConfiguration(getBackendHttpConfiguration())
                .toBackend(getBackend())
                .attach();
        }

        return attach;
    }

    Update createRequestRoutingRule(Update update) {
        WithBackendHttpConfigOrRedirect<Update> partialUpdate = update.defineRequestRoutingRule(getName())
            .fromListener(getListener());

        if (!ObjectUtils.isBlank(getRedirectConfiguration())) {
            update = partialUpdate.withRedirectConfiguration(getRedirectConfiguration())
                .attach();
        } else {
            update = partialUpdate.toBackendHttpConfiguration(getBackendHttpConfiguration())
                .toBackend(getBackend())
                .attach();
        }

        return update;
    }

    Update updateRequestRoutingRule(Update update) {
        ApplicationGatewayRequestRoutingRule.Update partialUpdate = update.updateRequestRoutingRule(getName())
            .fromListener(getListener());

        if (!ObjectUtils.isBlank(getRedirectConfiguration())) {
            update = partialUpdate.withRedirectConfiguration(getRedirectConfiguration())
                .parent();
        } else {
            update = partialUpdate.toBackendHttpConfiguration(getBackendHttpConfiguration())
                .toBackend(getBackend())
                .parent();
        }

        return update;
    }
}
