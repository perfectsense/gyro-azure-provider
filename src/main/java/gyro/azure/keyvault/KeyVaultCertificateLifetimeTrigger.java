/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.azure.keyvault;

import com.microsoft.azure.keyvault.models.Trigger;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Min;
import gyro.core.validation.Range;
import gyro.core.validation.Required;

public class KeyVaultCertificateLifetimeTrigger extends Diffable implements Copyable<Trigger> {

    private Integer daysBeforeExpiry;
    private Integer lifetimePercentage;

    /**
     * Days before certificate expires of lifetime at which to trigger.
     */
    @Min(0)
    public Integer getDaysBeforeExpiry() {
        return daysBeforeExpiry;
    }

    public void setDaysBeforeExpiry(Integer daysBeforeExpiry) {
        this.daysBeforeExpiry = daysBeforeExpiry;
    }

    /**
     * Percentage of lifetime at which to trigger. Value should be between ``1`` and ``99``.
     */
    @Required
    @Range(min = 1, max = 99)
    public Integer getLifetimePercentage() {
        return lifetimePercentage;
    }

    public void setLifetimePercentage(Integer lifetimePercentage) {
        this.lifetimePercentage = lifetimePercentage;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    Trigger totTrigger() {
        return new Trigger()
            .withDaysBeforeExpiry(getDaysBeforeExpiry())
            .withLifetimePercentage(getLifetimePercentage());
    }

    @Override
    public void copyFrom(Trigger trigger) {
        setDaysBeforeExpiry(trigger.daysBeforeExpiry());
        setLifetimePercentage(trigger.daysBeforeExpiry());
    }
}
