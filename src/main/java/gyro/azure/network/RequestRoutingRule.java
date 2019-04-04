package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule.UpdateDefinitionStages.WithBackendHttpConfigOrRedirect;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;

import java.util.List;

public class RequestRoutingRule extends Diffable {
    private String requestRoutingRuleName;
    private Integer frontendHttpPort;
    private Integer backendHttpPort;
    private List<String> backendIpAddresses;

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
        setRequestRoutingRuleName(rule.name());
    }

    public String getRequestRoutingRuleName() {
        return requestRoutingRuleName;
    }

    public void setRequestRoutingRuleName(String requestRoutingRuleName) {
        this.requestRoutingRuleName = requestRoutingRuleName;
    }

    public Integer getFrontendHttpPort() {
        return frontendHttpPort;
    }

    public void setFrontendHttpPort(Integer frontendHttpPort) {
        this.frontendHttpPort = frontendHttpPort;
    }

    public Integer getBackendHttpPort() {
        return backendHttpPort;
    }

    public void setBackendHttpPort(Integer backendHttpPort) {
        this.backendHttpPort = backendHttpPort;
    }

    public List<String> getBackendIpAddresses() {
        return backendIpAddresses;
    }

    public void setBackendIpAddresses(List<String> backendIpAddresses) {
        this.backendIpAddresses = backendIpAddresses;
    }

    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public String getBackendHttpConfiguration() {
        return backendHttpConfiguration;
    }

    public void setBackendHttpConfiguration(String backendHttpConfiguration) {
        this.backendHttpConfiguration = backendHttpConfiguration;
    }

    public String getRedirectConfiguration() {
        return redirectConfiguration;
    }

    public void setRedirectConfiguration(String redirectConfiguration) {
        this.redirectConfiguration = redirectConfiguration;
    }

    @Override
    public String primaryKey() {
        return getRequestRoutingRuleName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Request routing rule");

        if (!ObjectUtils.isBlank(getRequestRoutingRuleName())) {
            sb.append(" - ").append(getRequestRoutingRuleName());
        }

        return sb.toString();
    }

    Update createRequestRoutingRule(Update update) {
        WithBackendHttpConfigOrRedirect<Update> partialUpdate = update.defineRequestRoutingRule(getRequestRoutingRuleName())
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
        ApplicationGatewayRequestRoutingRule.Update partialUpdate = update.updateRequestRoutingRule(getRequestRoutingRuleName())
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
