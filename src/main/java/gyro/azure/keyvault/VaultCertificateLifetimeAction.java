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

import com.microsoft.azure.keyvault.models.Action;
import com.microsoft.azure.keyvault.models.ActionType;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class VaultCertificateLifetimeAction extends Diffable implements Copyable<Action> {

    private ActionType type;

    /**
     * The lifetime action type. Valid values are ``EmailContacts`` or ``AutoRenew``. (Required)
     */
    @Required
    @ValidStrings({"EmailContacts", "AutoRenew"})
    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    Action toAction() {
        return new Action().withActionType(getType());
    }

    @Override
    public void copyFrom(Action action) {
        setType(action.actionType());
    }
}
