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

import java.util.Optional;

import com.microsoft.azure.keyvault.models.LifetimeAction;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class VaultCertificateLifetime extends Diffable implements Copyable<LifetimeAction> {

    private VaultCertificateLifetimeAction action;
    private VaultCertificateLifetimeTrigger trigger;

    /**
     * Lifetime action config for the certificate policy. (Required)
     *
     * @subresource gyro.azure.keyvault.VaultCertificateLifetimeAction
     */
    @Required
    public VaultCertificateLifetimeAction getAction() {
        return action;
    }

    public void setAction(VaultCertificateLifetimeAction action) {
        this.action = action;
    }

    /**
     * Lifetime trigger config for the certificate policy.
     *
     * @subresource gyro.azure.keyvault.VaultCertificateLifetimeTrigger
     */
    public VaultCertificateLifetimeTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(VaultCertificateLifetimeTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public String primaryKey() {
        return String.format("with action - %s", getAction().getType());
    }

    LifetimeAction toLifetimeAction() {
        return new LifetimeAction()
            .withAction(getAction() != null ? getAction().toAction() : null)
            .withTrigger(getTrigger() != null ? getTrigger().totTrigger() : null);
    }

    @Override
    public void copyFrom(LifetimeAction lifetimeAction) {
        setAction(Optional.ofNullable(lifetimeAction.action()).map(o -> {
            VaultCertificateLifetimeAction action = newSubresource(VaultCertificateLifetimeAction.class);
            action.copyFrom(o);
            return action;
        }).orElse(null));

        setTrigger(Optional.ofNullable(lifetimeAction.trigger()).map(o -> {
            VaultCertificateLifetimeTrigger trigger = newSubresource(VaultCertificateLifetimeTrigger.class);
            trigger.copyFrom(o);
            return trigger;
        }).orElse(null));
    }
}
