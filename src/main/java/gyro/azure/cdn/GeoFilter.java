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
import java.util.HashSet;
import java.util.Set;

import com.azure.resourcemanager.cdn.models.GeoFilterActions;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class GeoFilter extends Diffable implements Copyable<com.azure.resourcemanager.cdn.models.GeoFilter> {

    private String action;
    private Set<String> countryCodes;
    private String relativePath;

    /**
     * The action to be taken.
     */
    @Required
    @ValidStrings({ "ALLOW", "BLOCK" })
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * The country codes that will either be allowed content or be blocked.
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
     * The relative path of the content.
     */
    @Required
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public void copyFrom(com.azure.resourcemanager.cdn.models.GeoFilter geoFilter) {
        setAction(geoFilter.action().name());
        setCountryCodes(new HashSet<>(geoFilter.countryCodes()));
        setRelativePath(geoFilter.relativePath());
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getAction(), getCountryCodes(), getRelativePath());
    }

    public com.azure.resourcemanager.cdn.models.GeoFilter toGeoFilter() {
        com.azure.resourcemanager.cdn.models.GeoFilter geoFilter = new com.azure.resourcemanager.cdn.models.GeoFilter();

        geoFilter.withAction(GeoFilterActions.fromString(getAction()));
        geoFilter.withCountryCodes(new ArrayList<>(getCountryCodes()));
        geoFilter.withRelativePath(getRelativePath());

        return geoFilter;
    }
}
