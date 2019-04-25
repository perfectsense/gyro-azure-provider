package gyro.azure.storage;

import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;

import com.microsoft.azure.storage.CorsHttpMethods;
import com.microsoft.azure.storage.CorsRule;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Creates a cors rule
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      cors
 *          allowed-headers: ["*"]
 *          allowed-methods: ["GET"]
 *          allowed-origins: ["*"]
 *          exposed-headers: ["*"]
 *          max-age: 6
 *      end
 */
public class Cors extends Diffable {

    private List<String> allowedHeaders;
    private List<String> allowedMethods;
    private List<String> allowedOrigins;
    private List<String> exposedHeaders;
    private Integer maxAge;

    public Cors() {}

    public Cors(CorsRule rule) {
        setAllowedHeaders(rule.getAllowedHeaders());
        setAllowedOrigins(rule.getAllowedOrigins());
        rule.getAllowedMethods().forEach(r -> getAllowedMethods().add(r.name()));
        setExposedHeaders(rule.getExposedHeaders());
        setMaxAge(rule.getMaxAgeInSeconds());
    }

    /**
     * A list of the allowed headers. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getAllowedHeaders() {
        if (allowedHeaders == null) {
            allowedHeaders = new ArrayList<>();
        }

        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * A list of the allowed methods. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new ArrayList<>();
        }

        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * A list of the allowed origins. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getAllowedOrigins() {
        if (allowedOrigins == null) {
            allowedOrigins = new ArrayList<>();
        }

        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * A list of the exposed headers. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getExposedHeaders() {
        if (exposedHeaders == null) {
            exposedHeaders = new ArrayList<>();
        }

        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * A maximum age, in seconds. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s/%s/%s", getAllowedHeaders(), getAllowedMethods(),
                getAllowedOrigins(), getExposedHeaders(), getMaxAge());
    }

    public String toDisplayString() {
        return String.format("cors rule %s from %s, header %s", getAllowedMethods(), getAllowedOrigins(), getAllowedHeaders());
    }

    public CorsRule toCors() {
        CorsRule rule = new CorsRule();

        rule.setAllowedHeaders(getAllowedHeaders());
        rule.setAllowedMethods(toAllowedMethods());
        rule.setAllowedOrigins(getAllowedOrigins());
        rule.setExposedHeaders(getExposedHeaders());
        rule.setMaxAgeInSeconds(getMaxAge());

        return rule;
    }

    private EnumSet<CorsHttpMethods> toAllowedMethods() {
        EnumSet<CorsHttpMethods> httpMethods = null;

        for (String method : getAllowedMethods()) {
            httpMethods = EnumSet.of(CorsHttpMethods.valueOf(method));
        }

        return httpMethods;
    }
}
