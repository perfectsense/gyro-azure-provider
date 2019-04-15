package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;

/**
 * Creates a Backend Http Configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     backend-http-configuration
 *         backend-http-configuration-name: "backend-http-configuration-example"
 *         port: 8080
 *         cookie-name: "cookieName"
 *         enable-affinity-cookie: false
 *         probe: "probe-example"
 *         connection-draining-timeout: 30
 *         host-header: "hostHeader"
 *         host-header-from-backend: false
 *         backend-path: "backendPath"
 *     end
 */
public class BackendHttpConfiguration extends Diffable {
    private String backendHttpConfigurationName;
    private Integer port;
    private String cookieName;
    private Boolean enableAffinityCookie;
    private Integer connectionDrainingTimeout;
    private String probe;
    private String hostHeader;
    private Boolean hostHeaderFromBackend;
    private String backendPath;

    public BackendHttpConfiguration() {

    }

    public BackendHttpConfiguration(ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration) {
        setBackendHttpConfigurationName(backendHttpConfiguration.name());
        setPort(backendHttpConfiguration.port());
        setConnectionDrainingTimeout(backendHttpConfiguration.connectionDrainingTimeoutInSeconds());
        setEnableAffinityCookie(backendHttpConfiguration.cookieBasedAffinity());
        setCookieName(backendHttpConfiguration.affinityCookieName());
        setProbe(backendHttpConfiguration.probe() != null ? backendHttpConfiguration.probe().name() : null);
        setHostHeader(backendHttpConfiguration.hostHeader());
        setHostHeaderFromBackend(backendHttpConfiguration.isHostHeaderFromBackend());
        setBackendPath(backendHttpConfiguration.path());
    }

    /**
     * Name of the backend http configuration. (Required)
     */
    public String getBackendHttpConfigurationName() {
        return backendHttpConfigurationName;
    }

    public void setBackendHttpConfigurationName(String backendHttpConfigurationName) {
        this.backendHttpConfigurationName = backendHttpConfigurationName;
    }

    /**
     * Port for the backend http configuration. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Cookie name for the backend http configuration.
     */
    @ResourceDiffProperty(updatable = true)
    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * Enable cookie based affinity for the backend http configuration. Defaults to false.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getEnableAffinityCookie() {
        if (enableAffinityCookie == null) {
            enableAffinityCookie = false;
        }

        return enableAffinityCookie;
    }

    public void setEnableAffinityCookie(Boolean enableAffinityCookie) {
        this.enableAffinityCookie = enableAffinityCookie;
    }

    /**
     * Connection draining timeout for the backend http configuration. defaults to 0.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getConnectionDrainingTimeout() {
        if (connectionDrainingTimeout == null) {
            connectionDrainingTimeout = 0;
        }

        return connectionDrainingTimeout;
    }

    public void setConnectionDrainingTimeout(Integer connectionDrainingTimeout) {
        this.connectionDrainingTimeout = connectionDrainingTimeout;
    }

    /**
     * Name of a probe to be associated with the backend http configuration.
     */
    @ResourceDiffProperty(updatable = true)
    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    /**
     * Override hostname for the backend http configuration.
     */
    @ResourceDiffProperty(updatable = true)
    public String getHostHeader() {
        return hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    /**
     * Get host header from the backend for the backend http configuration. Defaults to false.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getHostHeaderFromBackend() {
        if (hostHeaderFromBackend == null) {
            hostHeaderFromBackend = false;
        }

        return hostHeaderFromBackend;
    }

    public void setHostHeaderFromBackend(Boolean hostHeaderFromBackend) {
        this.hostHeaderFromBackend = hostHeaderFromBackend;
    }

    /**
     * Override backend path for the backend http configuration.
     */
    @ResourceDiffProperty(updatable = true)
    public String getBackendPath() {
        if (!ObjectUtils.isBlank(backendPath)) {
            backendPath = (!backendPath.startsWith("/") ? "/" : "") + backendPath;
            backendPath = backendPath + (!backendPath.endsWith("/") ? "/" : "");
        }

        return backendPath;
    }

    public void setBackendPath(String backendPath) {
        this.backendPath = backendPath;
    }

    @Override
    public String primaryKey() {
        return getBackendHttpConfigurationName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("backend http configuration");

        if (!ObjectUtils.isBlank(getBackendHttpConfigurationName())) {
            sb.append(" - ").append(getBackendHttpConfigurationName());
        }

        return sb.toString();
    }

    Update createBackendHttpConfiguration(Update update) {
        update = update.defineBackendHttpConfiguration(getBackendHttpConfigurationName())
            .withPort(getPort()).attach();

        update = updateBackendHttpConfiguration(update);

        return update;
    }

    Update updateBackendHttpConfiguration(Update update) {
        ApplicationGatewayBackendHttpConfiguration.Update tempUpdate = update
            .updateBackendHttpConfiguration(getBackendHttpConfigurationName())
            .withPort(getPort());

        if (getEnableAffinityCookie()) {
            tempUpdate = tempUpdate.withCookieBasedAffinity();
        } else {
            tempUpdate = tempUpdate.withoutCookieBasedAffinity();
        }

        if (!ObjectUtils.isBlank(getCookieName())) {
            tempUpdate = tempUpdate.withAffinityCookieName(getCookieName());
        }

        if (getConnectionDrainingTimeout() > 0) {
            tempUpdate = tempUpdate.withConnectionDrainingTimeoutInSeconds(getConnectionDrainingTimeout());
        } else {
            tempUpdate = tempUpdate.withoutConnectionDraining();
        }

        if (!ObjectUtils.isBlank(getProbe())) {
            tempUpdate = tempUpdate.withProbe(getProbe());
        } else {
            tempUpdate = tempUpdate.withoutProbe();
        }

        if (!ObjectUtils.isBlank(getBackendPath())) {
            tempUpdate = tempUpdate.withPath(getBackendPath());
        }

        if (!ObjectUtils.isBlank(getHostHeader())) {
            tempUpdate = tempUpdate.withHostHeader(getHostHeader());
        } else {
            tempUpdate = tempUpdate.withoutHostHeader();
        }

        if (getHostHeaderFromBackend()) {
            tempUpdate = tempUpdate.withHostHeaderFromBackend();
        }

        update = tempUpdate.parent();

        return update;
    }
}
