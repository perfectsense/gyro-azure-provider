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

package gyro.azure.cdn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.QueryStringCachingBehavior;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

/**
 * Creates a cdn endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    endpoint
 *        compression-enabled: false
 *        content-types-to-compress: ["application/eot", "application/json", "text/html"]
 *
 *        geo-filter
 *            action: "ALLOW"
 *            country-codes: ["CA", "CL"]
 *            relative-path: "/"
 *        end
 *
 *        geo-filter
 *            action: "BLOCK"
 *            country-codes: ["CA"]
 *            relative-path: "/relativepath"
 *        end
 *
 *        host-header: "my.host.com"
 *        http-enabled: true
 *        http-port: 81
 *        https-enabled: true
 *        https-port: 8443
 *        name: "test-endpoint-cdn"
 *        origin-hostname: "origin.hostname.com"
 *        origin-path: "/example/path"
 *        query-caching-behavior: "BYPASS_CACHING"
 *        type: "Standard"
 *    end
 */
public class CdnEndpointResource extends AzureResource implements Copyable<CdnEndpoint> {

    private static final String TYPE_PREMIUM = "Premium";
    private static final String TYPE_STANDARD = "Standard";

    private Boolean compressionEnabled;
    private Set<String> contentTypesToCompress;
    private Set<String> customDomains;
    private Set<GeoFilter> geoFilter;
    private String hostHeader;
    private Boolean httpEnabled;
    private Integer httpPort;
    private Boolean httpsEnabled;
    private Integer httpsPort;
    private String name;
    private String originHostname;
    private String originPath;
    private String queryCachingBehavior;
    private Map<String, String> tags;
    private String type;

    /**
     * Determines whether compression is enabled.
     */
    @Updatable
    public Boolean getCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(Boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    /**
     * Determines what content types to be compressed. Used if compression is enabled. (Conditional)
     */
    @Updatable
    public Set<String> getContentTypesToCompress() {
        if (contentTypesToCompress == null) {
            contentTypesToCompress = new HashSet<>();
        }

        return contentTypesToCompress;
    }

    public void setContentTypesToCompress(Set<String> contentTypesToCompress) {
        this.contentTypesToCompress = contentTypesToCompress;
    }

    /**
     * Determines what custom domains are associated with the endpoint.
     */
    @Updatable
    public Set<String> getCustomDomains() {
        if (customDomains == null) {
            customDomains = new HashSet<>();
        }

        return customDomains;
    }

    public void setCustomDomains(Set<String> customDomains) {
        this.customDomains = customDomains;
    }

    /**
     * The set of geo-filters associated with the endpoint.
     *
     * @subresource gyro.azure.cdn.GeoFilter
     */
    @Updatable
    public Set<GeoFilter> getGeoFilter() {
        if (geoFilter == null) {
            geoFilter = new HashSet<>();
        }

        return geoFilter;
    }

    public void setGeoFilter(Set<GeoFilter> geoFilter) {
        this.geoFilter = geoFilter;
    }

    /**
     * The host header of the endpoint.
     */
    @Updatable
    public String getHostHeader() {
        return hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    /**
     * Determines whether http protocol is enabled. Defaults to ``false``.
     */
    @Updatable
    public Boolean getHttpEnabled() {
        if (httpEnabled == null) {
            httpEnabled = false;
        }

        return httpEnabled;
    }

    public void setHttpEnabled(Boolean httpEnabled) {
        this.httpEnabled = httpEnabled;
    }

    /**
     * The http port. Required if http protocol is enabled.
     */
    @Updatable
    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    /**
     * Determines whether https protocol is enabled. Defaults to ``false``.
     */
    @Updatable
    public Boolean getHttpsEnabled() {
        if (httpsEnabled == null) {
            httpsEnabled = false;
        }

        return httpsEnabled;
    }

    public void setHttpsEnabled(Boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    /**
     * The https port. Required if https port is allowed.
     */
    @Updatable
    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    /**
     * The origin path.
     */
    @Updatable
    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    /**
     * The name of the endpoint.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The origin hostname.
     */
    @Required
    public String getOriginHostname() {
        return originHostname;
    }

    public void setOriginHostname(String originHostname) {
        this.originHostname = originHostname;
    }

    /**
     * Determines the query caching behavior.
     */
    @ValidStrings({ "IGNORE_QUERY_STRING", "BYPASS_CACHING", "USE_QUERY_STRING" })
    @Updatable
    public String getQueryCachingBehavior() {
        return queryCachingBehavior;
    }

    public void setQueryCachingBehavior(String queryCachingBehavior) {
        this.queryCachingBehavior = queryCachingBehavior;
    }

    /**
     * The tags associated with the endpoint.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The type of the endpoint. Defaults to ``Standard``.
     */
    @ValidStrings({ "Standard", "Premium" })
    public String getType() {
        if (type == null) {
            type = TYPE_STANDARD;
        }

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(CdnEndpoint cdnEndpoint) {
        setCompressionEnabled(cdnEndpoint.isCompressionEnabled());
        setContentTypesToCompress(cdnEndpoint.contentTypesToCompress());
        setCustomDomains(cdnEndpoint.customDomains());
        setGeoFilter(cdnEndpoint.geoFilters().stream().map(geo -> {
            GeoFilter filter = newSubresource(GeoFilter.class);
            filter.copyFrom(geo);
            return filter;
        }).collect(Collectors.toSet()));
        setHostHeader(cdnEndpoint.originHostHeader());
        setHttpEnabled(cdnEndpoint.isHttpAllowed());
        setHttpPort(getHttpEnabled() ? cdnEndpoint.httpPort() : null);
        setHttpsEnabled(cdnEndpoint.isHttpsAllowed());
        setHttpsPort(getHttpsEnabled() ? cdnEndpoint.httpsPort() : null);
        setName(cdnEndpoint.name());
        setOriginHostname(cdnEndpoint.originHostName());
        setOriginPath(cdnEndpoint.originPath());
        setQueryCachingBehavior(adjustQueueCaching(cdnEndpoint.queryStringCachingBehavior().toString()));
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        CdnProfileResource parent = (CdnProfileResource) parent();

        CdnProfile cdnProfile = client.cdnProfiles().getById(parent.getId());

        if (TYPE_PREMIUM.equalsIgnoreCase(getType())) {
            CdnEndpoint.UpdateDefinitionStages.WithPremiumAttach<CdnProfile.Update> createPremiumEndpoint =
                cdnProfile.update().defineNewPremiumEndpoint(getName(), getOriginHostname());

            if (getHostHeader() != null) {
                createPremiumEndpoint.withHostHeader(getHostHeader());
            }

            if (getHttpEnabled() != null && getHttpPort() != null) {
                createPremiumEndpoint.withHttpAllowed(getHttpEnabled());
                if (getHttpEnabled()) {
                    createPremiumEndpoint.withHttpPort(getHttpPort());
                }
            }

            if (getHttpsEnabled() != null && getHttpsPort() != null) {
                createPremiumEndpoint.withHttpsAllowed(getHttpsEnabled());
                if (getHttpsEnabled()) {
                    createPremiumEndpoint.withHttpsPort(getHttpsPort());
                }
            }

            if (getOriginPath() != null) {
                createPremiumEndpoint.withOriginPath(getOriginPath());
            }

            for (String customDomain : getCustomDomains()) {
                createPremiumEndpoint.withCustomDomain(customDomain);
            }

            CdnProfile.Update attach = createPremiumEndpoint.attach();
            CdnProfile profile = attach.apply();
            copyFrom(profile.endpoints().get(getName()));

        } else if (TYPE_STANDARD.equalsIgnoreCase(getType())) {
            CdnEndpoint.UpdateDefinitionStages.WithStandardAttach<CdnProfile.Update> createStandardEndpoint =
                cdnProfile.update().defineNewEndpoint(getName(), getOriginHostname());

            if (getCompressionEnabled() != null && getContentTypesToCompress() != null) {
                createStandardEndpoint.withCompressionEnabled(getCompressionEnabled())
                    .withContentTypesToCompress(getContentTypesToCompress());
            }

            if (!getGeoFilter().isEmpty()) {
                createStandardEndpoint.withGeoFilters(toGeoFilters());
            }

            if (getHostHeader() != null) {
                createStandardEndpoint.withHostHeader(getHostHeader());
            }

            if (getHttpEnabled() != null && getHttpPort() != null) {
                createStandardEndpoint.withHttpAllowed(getHttpEnabled());
                if (getHttpEnabled()) {
                    createStandardEndpoint.withHttpPort(getHttpPort());
                }
            }

            if (getHttpsEnabled() != null && getHttpsPort() != null) {
                createStandardEndpoint.withHttpsAllowed(getHttpsEnabled());
                if (getHttpsEnabled()) {
                    createStandardEndpoint.withHttpsPort(getHttpsPort());
                }
            }

            if (getOriginPath() != null) {
                createStandardEndpoint.withOriginPath(getOriginPath());
            }

            if (getQueryCachingBehavior() != null) {
                createStandardEndpoint
                    .withQueryStringCachingBehavior(QueryStringCachingBehavior
                        .valueOf(getQueryCachingBehavior()));
            }

            for (String customDomain : getCustomDomains()) {
                createStandardEndpoint.withCustomDomain(customDomain);
            }

            CdnProfile.Update attach = createStandardEndpoint.attach();
            CdnProfile profile = attach.apply();
            copyFrom(profile.endpoints().get(getName()));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createResourceManagerClient();

        CdnProfileResource parent = (CdnProfileResource) parent();

        CdnProfile cdnProfile = client.cdnProfiles().getById(parent.getId());

        if (TYPE_PREMIUM.equalsIgnoreCase(getType())) {
            CdnEndpoint.UpdatePremiumEndpoint updatePremiumEndpoint =
                cdnProfile.update().updatePremiumEndpoint(getName());

            if (getHostHeader() != null) {
                updatePremiumEndpoint.withHostHeader(getHostHeader());
            }

            if (getHttpEnabled() != null) {
                updatePremiumEndpoint.withHttpAllowed(getHttpEnabled());
                if (getHttpEnabled()) {
                    updatePremiumEndpoint.withHttpPort(getHttpPort());
                }
            }

            if (getHttpsEnabled() != null) {
                updatePremiumEndpoint.withHttpsAllowed(getHttpsEnabled());
                if (getHttpsEnabled()) {
                    updatePremiumEndpoint.withHttpsPort(getHttpsPort());
                }
            }

            if (getOriginPath() != null) {
                updatePremiumEndpoint.withOriginPath(getOriginPath());
            }

            for (String customDomain : getCustomDomains()) {
                updatePremiumEndpoint.withCustomDomain(customDomain);
            }

            CdnProfile profile = updatePremiumEndpoint.parent().apply();
            copyFrom(profile.endpoints().get(getName()));

        } else if (TYPE_STANDARD.equalsIgnoreCase(getType())) {
            CdnEndpoint.UpdateStandardEndpoint updateStandardEndpoint =
                cdnProfile
                    .update().updateEndpoint(getName());

            if (getCompressionEnabled() != null && getContentTypesToCompress() != null) {
                updateStandardEndpoint.withCompressionEnabled(getCompressionEnabled())
                    .withContentTypesToCompress(getContentTypesToCompress());
            }

            if (!getGeoFilter().isEmpty()) {
                updateStandardEndpoint.withGeoFilters(toGeoFilters());
            }

            if (getHostHeader() != null) {
                updateStandardEndpoint.withHostHeader(getHostHeader());
            }

            if (getHttpEnabled() != null) {
                updateStandardEndpoint.withHttpAllowed(getHttpEnabled());
                if (getHttpEnabled()) {
                    updateStandardEndpoint.withHttpPort(getHttpPort());
                }
            }

            if (getHttpsEnabled() != null) {
                updateStandardEndpoint.withHttpsAllowed(getHttpsEnabled());
                if (getHttpsEnabled()) {
                    updateStandardEndpoint.withHttpsPort(getHttpsPort());
                }
            }

            if (getOriginPath() != null) {
                updateStandardEndpoint.withOriginPath(getOriginPath());
            }

            if (getQueryCachingBehavior() != null) {
                updateStandardEndpoint
                    .withQueryStringCachingBehavior(QueryStringCachingBehavior
                        .valueOf(getQueryCachingBehavior()));
            }

            for (String customDomain : getCustomDomains()) {
                updateStandardEndpoint.withCustomDomain(customDomain);
            }

            CdnProfile profile = updateStandardEndpoint.parent().apply();
            copyFrom(profile.endpoints().get(getName()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        CdnProfileResource parent = (CdnProfileResource) parent();

        CdnProfile cdnProfile = client.cdnProfiles().getById(parent.getId());

        CdnProfile.Update update = cdnProfile.update().withoutEndpoint(getName());

        update.apply();
    }

    private List<com.azure.resourcemanager.cdn.models.GeoFilter> toGeoFilters() {
        List<com.azure.resourcemanager.cdn.models.GeoFilter> geoFilters = new ArrayList<>();

        getGeoFilter().forEach(geo -> geoFilters.add(geo.toGeoFilter()));

        return geoFilters;
    }

    private String adjustQueueCaching(String behavior) {
        if ("BypassCaching".equals(behavior)) {
            return "BYPASS_CACHING";
        } else if ("IgnoreQueryString".equals(behavior)) {
            return "IGNORE_QUERY_STRING";
        } else if ("NotSet".equals(behavior)) {
            return "NOT_SET";
        } else if ("UseQueryString".equals(behavior)) {
            return "USE_QUERY_STRING";
        }

        return null;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (!getHttpEnabled() && !getHttpsEnabled()) {
            errors.add(new ValidationError(
                this,
                null,
                "Both 'http-enabled' and 'https-enabled' cannot be set to false."));
        }

        if (!getHttpEnabled() && getHttpPort() != null) {
            errors.add(new ValidationError(
                this,
                "http-port",
                "'http-port' cannot be configured when 'http-enabled' is set to false."));
        }

        if (getHttpEnabled() && getHttpPort() == null) {
            errors.add(new ValidationError(
                this,
                "http-port",
                "'http-port' is required when 'http-enabled' is set to true."));
        }

        if (!getHttpsEnabled() && getHttpsPort() != null) {
            errors.add(new ValidationError(
                this,
                "https-port",
                "'https-port' cannot be configured when 'https-enabled' is set to false."));
        }

        if (getHttpsEnabled() && getHttpsPort() == null) {
            errors.add(new ValidationError(
                this,
                "https-port",
                "'https-port' is required when 'https-enabled' is set to true."));
        }

        return errors;
    }
}
