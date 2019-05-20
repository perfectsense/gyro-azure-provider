package gyro.azure.cdn;

import gyro.core.resource.Diffable;
import com.microsoft.azure.management.cdn.GeoFilterActions;

import java.util.ArrayList;
import java.util.List;

public class GeoFilter extends Diffable {

    private String action;
    private List<String> countryCodes;
    private String relativePath;

    public GeoFilter() {}

    public GeoFilter(com.microsoft.azure.management.cdn.GeoFilter geoFilter) {
        setAction(geoFilter.action().name());
        setCountryCodes(geoFilter.countryCodes());
        setRelativePath(geoFilter.relativePath());
    }

    /**
     * The action to be taken. Values are ``ALLOW`` and ``BLOCK``. (Required)
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * The country codes that will either be allowed content or be blocked. (Required)
     */
    public List<String> getCountryCodes() {
        if (countryCodes == null) {
            countryCodes = new ArrayList<>();
        }

        return countryCodes;
    }

    public void setCountryCodes(List<String> countryCodes) {
        this.countryCodes = countryCodes;
    }

    /**
     * The relative path of the content. (Required)
     */
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getAction(), getCountryCodes(), getRelativePath());
    }

    public String toDisplayString() {
        String displayString = String.format("action: %s and country code %s", getAction(), getCountryCodes());
        if (getCountryCodes().size() > 1) {
            displayString = String.format("action: %s and country codes %s", getAction(), getCountryCodes());
        }
        return displayString;
    }

    public com.microsoft.azure.management.cdn.GeoFilter toGeoFilter() {
        com.microsoft.azure.management.cdn.GeoFilter geoFilter = new com.microsoft.azure.management.cdn.GeoFilter();

        geoFilter.withAction(GeoFilterActions.fromString(getAction()));
        geoFilter.withCountryCodes(getCountryCodes());
        geoFilter.withRelativePath(getRelativePath());

        return geoFilter;
    }
}
