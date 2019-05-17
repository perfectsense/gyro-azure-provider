package gyro.azure.cdn;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
import com.microsoft.azure.management.cdn.CdnEndpoint.UpdateDefinitionStages.WithPremiumAttach;
import com.microsoft.azure.management.cdn.CdnEndpoint.UpdateDefinitionStages.WithStandardAttach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a cdn endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         azure::cdn-endpoint standard-cdn-endpoint-example
 *             cdn-profile: $(azure::cdn-profile cdn-profile-example)
 *             compression-enabled: false
 *             content-types-to-compress: ["application/eot", "application/json", "text/html"]
 *
 *             geo-filter
 *                 action: "ALLOW"
 *                 country-codes: ["CA", "CL"]
 *                 relative-path: "/"
 *             end
 *
 *             geo-filter
 *                 action: "BLOCK"
 *                 country-codes: ["CA"]
 *                 relative-path: "/relativepath"
 *             end
 *
 *             host-header: "my.host.com"
 *             http-enabled: true
 *             http-port: 81
 *             https-enabled: true
 *             https-port: 8443
 *             name: "test-endpoint-cdn"
 *             origin-hostname: "origin.hostname.com"
 *             origin-path: "/example/path"
 *             query-caching-behavior: "BYPASS_CACHING"
 *             type: "Standard"
 *         end
 */
@ResourceType("cdn-endpoint")
public class CdnEndpointResource extends AzureResource {

    private static final String TYPE_PREMIUM = "Premium";
    private static final String TYPE_STANDARD = "Standard";

    private CdnProfileResource cdnProfile;
    private Boolean compressionEnabled;
    private Set<String> contentTypesToCompress;
    private Set<String> customDomains;
    private List<GeoFilter> geoFilter;
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
     * The cdn profile resource where the endpoint is found. (Required)
     */
    public CdnProfileResource getCdnProfile() {
        return cdnProfile;
    }

    public void setCdnProfile(CdnProfileResource cdnProfile) {
        this.cdnProfile = cdnProfile;
    }

    /**
     * Determines whether compression is enabled. (Optional)
     */
    @ResourceUpdatable
    public Boolean getCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(Boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    /**
     * Determines what content types to be compressed. Used if compression is enabled. (Conditional)
     */
    @ResourceUpdatable
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
     * Determines what custom domains are associated with the endpoint. (Optional)
     */
    @ResourceUpdatable
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
     * The list of geo-filters associated with the endpoint. (Optional)
     */
    @ResourceUpdatable
    public List<GeoFilter> getGeoFilter() {
        if (geoFilter == null) {
            geoFilter = new ArrayList<>();
        }

        return geoFilter;
    }

    public void setGeoFilter(List<GeoFilter> geoFilter) {
        this.geoFilter = geoFilter;
    }

    /**
     * The host header of the endpoint. (Optional)
     */
    @ResourceUpdatable
    public String getHostHeader() {
        return hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    /**
     * Determines whether http protocol is enabled. (Optional)
     */
    @ResourceUpdatable
    public Boolean getHttpEnabled() {
        return httpEnabled;
    }

    public void setHttpEnabled(Boolean httpEnabled) {
        this.httpEnabled = httpEnabled;
    }

    /**
     * The http port. Required if http protocol is enabled. (Optional)
     */
    @ResourceUpdatable
    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    /**
     * Determines whether https protocol is enabled. (Optional)
     */
    @ResourceUpdatable
    public Boolean getHttpsEnabled() {
        return httpsEnabled;
    }

    public void setHttpsEnabled(Boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    /**
     * The https port. Required if https port is allowed. (Optional)
     */
    @ResourceUpdatable
    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    /**
     * The origin path. (Optional)
     */
    @ResourceUpdatable
    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    /**
     * The name of the endpoint. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The origin hostname. (Required)
     */
    public String getOriginHostname() {
        return originHostname;
    }

    public void setOriginHostname(String originHostname) {
        this.originHostname = originHostname;
    }

    /**
     * Determines the query caching behavior. Values are ``IGNORE_QUERY_STRING``, ``BYPASS_CACHING``,
     * and ``USE_QUERY_STRING``. (Optional)
     */
    @ResourceUpdatable
    public String getQueryCachingBehavior() {
        return queryCachingBehavior;
    }

    public void setQueryCachingBehavior(String queryCachingBehavior) {
        this.queryCachingBehavior = queryCachingBehavior;
    }

    /**
     * The tags associated with the endpoint. (Optional)
     */
    @ResourceUpdatable
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
     * The type of the endpoint. Values are ``Standard`` and ``Premium``. (Required)
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        CdnEndpoint cdnEndpoint = client.cdnProfiles().getById(getCdnProfile().getId()).endpoints().get(getName());
        if (cdnEndpoint == null) {
            return false;
        }

        setCompressionEnabled(cdnEndpoint.isCompressionEnabled());
        setContentTypesToCompress(cdnEndpoint.contentTypesToCompress());
        setCustomDomains(cdnEndpoint.customDomains());
        cdnEndpoint.geoFilters().forEach(geo -> getGeoFilter().add(new GeoFilter(geo)));
        setHostHeader(cdnEndpoint.originHostHeader());
        setHttpEnabled(cdnEndpoint.isHttpAllowed());
        setHttpPort(cdnEndpoint.httpPort());
        setHttpsEnabled(cdnEndpoint.isHttpsAllowed());
        setHttpsPort(cdnEndpoint.httpsPort());
        setName(cdnEndpoint.name());
        setOriginHostname(cdnEndpoint.originHostName());
        setOriginPath(cdnEndpoint.originPath());
        setQueryCachingBehavior(adjustQueueCaching(cdnEndpoint.queryStringCachingBehavior().toString()));

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        CdnProfile cdnProfile = client.cdnProfiles().getById(getCdnProfile().getId());

        if (TYPE_PREMIUM.equalsIgnoreCase(getType())) {
            WithPremiumAttach<CdnProfile.Update> createPremiumEndpoint =
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
            attach.apply();

        } else if (TYPE_STANDARD.equalsIgnoreCase(getType())) {
            WithStandardAttach<CdnProfile.Update> createStandardEndpoint =
                    cdnProfile.update().defineNewEndpoint(getName(), getOriginHostname());

            if (getCompressionEnabled() != null && getContentTypesToCompress() != null) {
                createStandardEndpoint.withCompressionEnabled(getCompressionEnabled())
                        .withContentTypesToCompress(getContentTypesToCompress());
            }

            if (getGeoFilter() != null) {
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
            attach.apply();

        } else {
            throw new GyroException("Invalid endpoint type. Valid values are Premium and Standard");
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        CdnProfile cdnProfile = client.cdnProfiles().getById(getCdnProfile().getId());

        if (TYPE_PREMIUM.equalsIgnoreCase(getType())) {
            CdnEndpoint.UpdatePremiumEndpoint updatePremiumEndpoint =
                    cdnProfile.update().updatePremiumEndpoint(getName());

            if (getHostHeader() != null) {
                updatePremiumEndpoint.withHostHeader(getHostHeader());
            }

            if (getHttpEnabled() != null && getHttpPort() != null) {
                updatePremiumEndpoint.withHttpAllowed(getHttpEnabled());
                if (getHttpEnabled()) {
                    updatePremiumEndpoint.withHttpPort(getHttpPort());
                }
            }

            if (getHttpsEnabled() != null && getHttpsPort() != null) {
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

            CdnProfile.Update parent = updatePremiumEndpoint.parent();
            parent.apply();

        } else if (TYPE_STANDARD.equalsIgnoreCase(getType())) {
            CdnEndpoint.UpdateStandardEndpoint updateStandardEndpoint =
                    cdnProfile
                    .update().updateEndpoint(getName());

            if (getCompressionEnabled() != null && getContentTypesToCompress() != null) {
                updateStandardEndpoint.withCompressionEnabled(getCompressionEnabled())
                        .withContentTypesToCompress(getContentTypesToCompress());
            }

            if (getGeoFilter() != null) {
                updateStandardEndpoint.withGeoFilters(toGeoFilters());
            }

            if (getHostHeader() != null) {
                updateStandardEndpoint.withHostHeader(getHostHeader());
            }

            if (getHttpEnabled() != null && getHttpPort() != null) {
                updateStandardEndpoint.withHttpAllowed(getHttpEnabled());
                if (getHttpEnabled()) {
                    updateStandardEndpoint.withHttpPort(getHttpPort());
                }
            }

            if (getHttpsEnabled() != null && getHttpsPort() != null) {
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

            CdnProfile.Update parent = updateStandardEndpoint.parent();
            parent.apply();
        }
    }

    @Override
    public void delete() {
        Azure client = createClient();

        CdnProfile cdnProfile = client.cdnProfiles().getById(getCdnProfile().getId());

        CdnProfile.Update update = cdnProfile.update().withoutEndpoint(getName());
        update.apply();
    }

    @Override
    public String toDisplayString() {
        return getType().toLowerCase() + " cdn endpoint " + getName();
    }

    private List<com.microsoft.azure.management.cdn.GeoFilter> toGeoFilters() {
        List<com.microsoft.azure.management.cdn.GeoFilter> geoFilters = new ArrayList<>();

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
}
