package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration.DefinitionStages.WithAttach;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

/**
 * Creates a Backend Http Configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     backend-http-configuration
 *         name: "backend-http-configuration-example"
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
public class BackendHttpConfiguration extends Diffable implements Copyable<ApplicationGatewayBackendHttpConfiguration> {
    private String name;
    private Integer port;
    private String cookieName;
    private Boolean enableAffinityCookie;
    private Integer connectionDrainingTimeout;
    private String probe;
    private String hostHeader;
    private Boolean hostHeaderFromBackend;
    private String backendPath;

    /**
     * Name of the backend http configuration. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Port for the backend http configuration. (Required)
     */
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Cookie name for the backend http configuration.
     */
    @Updatable
    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * Enable cookie based affinity for the backend http configuration. Defaults to false.
     */
    @Updatable
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
    @Updatable
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
    @Updatable
    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    /**
     * Override hostname for the backend http configuration.
     */
    @Updatable
    public String getHostHeader() {
        return hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    /**
     * Get host header from the backend for the backend http configuration. Defaults to false.
     */
    @Updatable
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
    @Updatable
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
    public void copyFrom(ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration) {
        setName(backendHttpConfiguration.name());
        setPort(backendHttpConfiguration.port());
        setConnectionDrainingTimeout(backendHttpConfiguration.connectionDrainingTimeoutInSeconds());
        setEnableAffinityCookie(backendHttpConfiguration.cookieBasedAffinity());
        setCookieName(backendHttpConfiguration.affinityCookieName());
        setProbe(backendHttpConfiguration.probe() != null ? backendHttpConfiguration.probe().name() : null);
        setHostHeader(backendHttpConfiguration.hostHeader());
        setHostHeaderFromBackend(backendHttpConfiguration.isHostHeaderFromBackend());
        setBackendPath(backendHttpConfiguration.path());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithCreate createBackendHttpConfiguration(WithCreate attach) {
        WithAttach<WithCreate> withCreateWithAttach = attach.defineBackendHttpConfiguration(getName())
            .withPort(getPort());

        if (getEnableAffinityCookie()) {
            withCreateWithAttach = withCreateWithAttach.withCookieBasedAffinity();
        }

        if (!ObjectUtils.isBlank(getCookieName())) {
            withCreateWithAttach = withCreateWithAttach.withAffinityCookieName(getCookieName());
        }

        if (getConnectionDrainingTimeout() > 0) {
            withCreateWithAttach = withCreateWithAttach.withConnectionDrainingTimeoutInSeconds(getConnectionDrainingTimeout());
        }

        if (!ObjectUtils.isBlank(getProbe())) {
            withCreateWithAttach = withCreateWithAttach.withProbe(getProbe());
        }

        if (!ObjectUtils.isBlank(getBackendPath())) {
            withCreateWithAttach = withCreateWithAttach.withPath(getBackendPath());
        }

        if (!ObjectUtils.isBlank(getHostHeader())) {
            withCreateWithAttach = withCreateWithAttach.withHostHeader(getHostHeader());
        }

        if (getHostHeaderFromBackend()) {
            withCreateWithAttach = withCreateWithAttach.withHostHeaderFromBackend();
        }

        attach = withCreateWithAttach.attach();

        return attach;
    }

    Update createBackendHttpConfiguration(Update update) {
        update = update.defineBackendHttpConfiguration(getName())
            .withPort(getPort()).attach();

        update = updateBackendHttpConfiguration(update);

        return update;
    }

    Update updateBackendHttpConfiguration(Update update) {
        ApplicationGatewayBackendHttpConfiguration.Update tempUpdate = update
            .updateBackendHttpConfiguration(getName())
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
