package gyro.azure.storage;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import com.microsoft.azure.storage.CorsHttpMethods;
import com.microsoft.azure.storage.CorsRule;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Set;

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
 *          type: "blob"
 *      end
 */
public class Cors extends Diffable implements Copyable<CorsRule> {

    private Set<String> allowedHeaders;
    private Set<String> allowedMethods;
    private Set<String> allowedOrigins;
    private Set<String> exposedHeaders;
    private Integer maxAge;
    private String type;

    /**
     * A list of the allowed headers. (Required)
     */
    @Required
    @Updatable
    public Set<String> getAllowedHeaders() {
        if (allowedHeaders == null) {
            allowedHeaders = new HashSet<>();
        }

        return allowedHeaders;
    }

    public void setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * A list of the allowed methods. (Required)
     */
    @Required
    @Updatable
    public Set<String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new HashSet<>();
        }

        return allowedMethods;
    }

    public void setAllowedMethods(Set<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * A list of the allowed origins. (Required)
     */
    @Required
    @Updatable
    public Set<String> getAllowedOrigins() {
        if (allowedOrigins == null) {
            allowedOrigins = new HashSet<>();
        }

        return allowedOrigins;
    }

    public void setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * A list of the exposed headers. (Required)
     */
    @Required
    @Updatable
    public Set<String> getExposedHeaders() {
        if (exposedHeaders == null) {
            exposedHeaders = new HashSet<>();
        }

        return exposedHeaders;
    }

    public void setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * A maximum age, in seconds. (Required)
     */
    @Required
    @Updatable
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Specifies which service the rule belongs to. Valid values are ``blob`` or ``file`` or ``queue`` or ``table``. (Required)
     */
    @Required
    @ValidStrings({"blob", "file", "queue", "table"})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(CorsRule rule) {
        setAllowedHeaders(new HashSet<>(rule.getAllowedHeaders()));
        setAllowedOrigins(new HashSet<>(rule.getAllowedOrigins()));
        rule.getAllowedMethods().forEach(r -> getAllowedMethods().add(r.name()));
        setExposedHeaders(new HashSet<>(rule.getExposedHeaders()));
        setMaxAge(rule.getMaxAgeInSeconds());
    }

    public String primaryKey() {
        return String.format("%s/%s/%s/%s/%s/%s", getAllowedHeaders(), getAllowedMethods(),
            getAllowedOrigins(), getExposedHeaders(), getMaxAge(), getType());
    }

    public CorsRule toCors() {
        CorsRule rule = new CorsRule();

        rule.setAllowedHeaders(new ArrayList<>(getAllowedHeaders()));
        rule.setAllowedMethods(toAllowedMethods());
        rule.setAllowedOrigins(new ArrayList<>(getAllowedOrigins()));
        rule.setExposedHeaders(new ArrayList<>(getExposedHeaders()));
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
