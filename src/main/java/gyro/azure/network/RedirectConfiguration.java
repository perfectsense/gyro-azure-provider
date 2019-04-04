package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayRedirectConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayRedirectConfiguration.UpdateDefinitionStages.WithTarget;
import com.microsoft.azure.management.network.ApplicationGatewayRedirectType;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;

public class RedirectConfiguration extends Diffable {
    private String redirectConfigurationName;
    private String type;
    private String targetListener;
    private String targetUrl;
    private Boolean includeQueryString;
    private Boolean includePath;

    public RedirectConfiguration() {

    }

    public RedirectConfiguration(ApplicationGatewayRedirectConfiguration redirectConfiguration) {
        setRedirectConfigurationName(redirectConfiguration.name());
        setType(redirectConfiguration.type().toString());
        setTargetListener(redirectConfiguration.targetListener() != null ? redirectConfiguration.targetListener().name() : null);
        setTargetUrl(redirectConfiguration.targetUrl());
        setIncludePath(redirectConfiguration.isPathIncluded());
        setIncludeQueryString(redirectConfiguration.isQueryStringIncluded());

    }

    public String getRedirectConfigurationName() {
        return redirectConfigurationName;
    }

    public void setRedirectConfigurationName(String redirectConfigurationName) {
        this.redirectConfigurationName = redirectConfigurationName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetListener() {
        return targetListener;
    }

    public void setTargetListener(String targetListener) {
        this.targetListener = targetListener;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public Boolean getIncludeQueryString() {
        if (includeQueryString == null) {
            includeQueryString = false;
        }

        return includeQueryString;
    }

    public void setIncludeQueryString(Boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

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
    public String primaryKey() {
        return null;
    }

    @Override
    public String toDisplayString() {
        return null;
    }

    Update createRedirectConfiguration(Update update) {
        WithTarget<Update> updateWithTarget = update.defineRedirectConfiguration(getRedirectConfigurationName())
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
        ApplicationGatewayRedirectConfiguration.Update parialUpdate = update
            .updateRedirectConfiguration(getRedirectConfigurationName())
            .withType(ApplicationGatewayRedirectType.fromString(getType()));

        if (!ObjectUtils.isBlank(getTargetListener())) {
            if (getIncludePath() && getIncludeQueryString()) {
                update = parialUpdate.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .withQueryStringIncluded()
                    .parent();
            } else if (getIncludePath()) {
                update = parialUpdate.withTargetListener(getTargetListener())
                    .withPathIncluded()
                    .parent();
            } else if (getIncludeQueryString()) {
                update = parialUpdate.withTargetListener(getTargetListener())
                    .withQueryStringIncluded()
                    .parent();
            } else {
                update = parialUpdate.withTargetListener(getTargetListener())
                    .parent();
            }
        } else {
            if (getIncludeQueryString()) {
                update = parialUpdate.withTargetUrl(getTargetUrl())
                    .withQueryStringIncluded()
                    .parent();
            } else {
                update = parialUpdate.withTargetUrl(getTargetUrl())
                    .parent();
            }
        }

        return update;
    }
}
