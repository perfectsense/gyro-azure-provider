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

package gyro.azure.compute;

import com.microsoft.azure.management.compute.AdditionalCapabilities;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class AdditionalCapability extends Diffable implements Copyable<AdditionalCapabilities> {
    private Boolean ultraSSDEnabled;

    /**
     * Enable ultra SSD. Defaults to ``false``.
     */
    @Updatable
    public Boolean getUltraSSDEnabled() {
        if (ultraSSDEnabled == null) {
            ultraSSDEnabled = false;
        }

        return ultraSSDEnabled;
    }

    public void setUltraSSDEnabled(Boolean ultraSSDEnabled) {
        this.ultraSSDEnabled = ultraSSDEnabled;
    }

    @Override
    public void copyFrom(AdditionalCapabilities additionalCapabilities) {
        setUltraSSDEnabled(additionalCapabilities.ultraSSDEnabled());
    }

    AdditionalCapabilities toAdditionalCapabilities() {
        AdditionalCapabilities capabilities = new AdditionalCapabilities();
        capabilities.withUltraSSDEnabled(getUltraSSDEnabled());
        return capabilities;
    }

    @Override
    public String primaryKey() {
        return "capability";
    }
}
