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

import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Min;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class KeyVaultCertificateLifetime extends Diffable implements Copyable<LifetimeAction> {

    private String action;
    private Integer daysBeforeExpiry;
    private Integer lifetimePercentage;

    /**
     * The lifetime action type.
     */
    @ValidStrings({"EmailContacts", "AutoRenew"})
    @Required
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

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
        return String.format("with action - %s", getAction());
    }

    LifetimeAction toLifetimeAction() {
        LifetimeAction lifetimeAction = new LifetimeAction(CertificatePolicyAction.fromString(getAction()));
        lifetimeAction.setLifetimePercentage(getLifetimePercentage());
        lifetimeAction.setDaysBeforeExpiry(getDaysBeforeExpiry());

        return lifetimeAction;
    }

    @Override
    public void copyFrom(LifetimeAction action) {
        setAction(action.getAction().toString());
        setLifetimePercentage(action.getLifetimePercentage());
        setDaysBeforeExpiry(action.getDaysBeforeExpiry());
    }
}
