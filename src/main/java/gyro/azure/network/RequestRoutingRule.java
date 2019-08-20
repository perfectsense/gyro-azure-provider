package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule.UpdateDefinitionStages.WithBackendHttpConfigOrRedirect;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule.DefinitionStages;
import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate;
import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithRequestRoutingRule;
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
     * Name of the rule. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Name of the listener to associated with the rule. (Required)
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
     * Name of the redirect configuration to be associated with the rule. Required if backend bot present.
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
        setBackendHttpConfiguration(rule.backendHttpConfiguration() != null ? rule.backendHttpConfiguration().name() : null);
        setRedirectConfiguration(rule.redirectConfiguration() != null ? rule.redirectConfiguration().name() : null);
        setName(rule.name());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithRequestRoutingRuleOrCreate createRequestRoutingRule(WithRequestRoutingRule preAttach, WithRequestRoutingRuleOrCreate attach) {
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
