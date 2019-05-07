package gyro.azure.storage;

import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceUpdatable;

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
 *          type: "blob"
 *      end
 */
public class Cors extends Diffable {

    private List<String> allowedHeaders;
    private List<String> allowedMethods;
    private List<String> allowedOrigins;
    private List<String> exposedHeaders;
    private Integer maxAge;
    private String type;

    public Cors() {}

    public Cors(CorsRule rule, String type) {
        setAllowedHeaders(rule.getAllowedHeaders());
        setAllowedOrigins(rule.getAllowedOrigins());
        rule.getAllowedMethods().forEach(r -> getAllowedMethods().add(r.name()));
        setExposedHeaders(rule.getExposedHeaders());
        setMaxAge(rule.getMaxAgeInSeconds());
        setType(type);
    }

    /**
     * A list of the allowed headers. (Required)
     */
    @ResourceUpdatable
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
    @ResourceUpdatable
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
    @ResourceUpdatable
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
    @ResourceUpdatable
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
    @ResourceUpdatable
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Specifies which service the rule belongs to. Options include blob, file, queue, and table. (Required)
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s/%s/%s/%s", getAllowedHeaders(), getAllowedMethods(),
                getAllowedOrigins(), getExposedHeaders(), getMaxAge(), getType());
    }

    public String toDisplayString() {
        return String.format("%s cors rule %s from %s, header %s", getType(), getAllowedMethods(), getAllowedOrigins(), getAllowedHeaders());
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
