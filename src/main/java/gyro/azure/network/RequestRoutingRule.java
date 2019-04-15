package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule.UpdateDefinitionStages.WithBackendHttpConfigOrRedirect;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;

/**
 * Creates a Request Routing Rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     request-routing-rule
 *         rule-name: "request-routing-rule-example"
 *         listener: "listener-example"
 *         backend: "backend-example"
 *         backend-http-configuration: "backend-http-configuration-example"
 *     end
 */
public class RequestRoutingRule extends Diffable {
    private String ruleName;
    private String listener;
    private String backend;
    private String backendHttpConfiguration;
    private String redirectConfiguration;

    public RequestRoutingRule() {

    }

    public RequestRoutingRule(ApplicationGatewayRequestRoutingRule rule) {
        setBackend(rule.backend() != null ? rule.backend().name() : null);
        setListener(rule.listener() != null ? rule.listener().name() : null);
        setBackendHttpConfiguration(rule.backendHttpConfiguration() != null ? rule.backendHttpConfiguration().name() : null);
        setRedirectConfiguration(rule.redirectConfiguration() != null ? rule.redirectConfiguration().name() : null);
        setRuleName(rule.name());
    }

    /**
     * Name of the rule. (Required)
     */
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * Name of the listener to associated with the rule. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }

    /**
     * Name of the backend to be associated with the rule. Required if redirection not present.
     */
    @ResourceDiffProperty(updatable = true)
    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    /**
     * Name of the backend http configuration to be associated with the rule. Required if redirection not present.
     */
    @ResourceDiffProperty(updatable = true)
    public String getBackendHttpConfiguration() {
        return backendHttpConfiguration;
    }

    public void setBackendHttpConfiguration(String backendHttpConfiguration) {
        this.backendHttpConfiguration = backendHttpConfiguration;
    }

    /**
     * Name of the redirect configuration to be associated with the rule. Required if backend bot present.
     */
    @ResourceDiffProperty(updatable = true)
    public String getRedirectConfiguration() {
        return redirectConfiguration;
    }

    public void setRedirectConfiguration(String redirectConfiguration) {
        this.redirectConfiguration = redirectConfiguration;
    }

    @Override
    public String primaryKey() {
        return getRuleName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Request routing rule");

        if (!ObjectUtils.isBlank(getRuleName())) {
            sb.append(" - ").append(getRuleName());
        }

        return sb.toString();
    }

    Update createRequestRoutingRule(Update update) {
        WithBackendHttpConfigOrRedirect<Update> partialUpdate = update.defineRequestRoutingRule(getRuleName())
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
        ApplicationGatewayRequestRoutingRule.Update partialUpdate = update.updateRequestRoutingRule(getRuleName())
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
