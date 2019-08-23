package gyro.azure.cdn;

import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import com.microsoft.azure.management.cdn.GeoFilterActions;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GeoFilter extends Diffable implements Copyable<com.microsoft.azure.management.cdn.GeoFilter> {

    private String action;
    private Set<String> countryCodes;
    private String relativePath;

    /**
     * The action to be taken. Valid values are ``ALLOW`` or ``BLOCK``. (Required)
     */
    @Required
    @ValidStrings({"ALLOW", "BLOCK"})
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * The country codes that will either be allowed content or be blocked. (Required)
     */
    @Required
    public Set<String> getCountryCodes() {
        if (countryCodes == null) {
            countryCodes = new HashSet<>();
        }

        return countryCodes;
    }

    public void setCountryCodes(Set<String> countryCodes) {
        this.countryCodes = countryCodes;
    }

    /**
     * The relative path of the content. (Required)
     */
    @Required
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public void copyFrom(com.microsoft.azure.management.cdn.GeoFilter geoFilter) {
        setAction(geoFilter.action().name());
        setCountryCodes(new HashSet<>(geoFilter.countryCodes()));
        setRelativePath(geoFilter.relativePath());
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getAction(), getCountryCodes(), getRelativePath());
    }

    public com.microsoft.azure.management.cdn.GeoFilter toGeoFilter() {
        com.microsoft.azure.management.cdn.GeoFilter geoFilter = new com.microsoft.azure.management.cdn.GeoFilter();

        geoFilter.withAction(GeoFilterActions.fromString(getAction()));
        geoFilter.withCountryCodes(new ArrayList<>(getCountryCodes()));
        geoFilter.withRelativePath(getRelativePath());

        return geoFilter;
    }
}
