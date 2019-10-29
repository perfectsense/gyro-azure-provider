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

package gyro.azure.storage;

import com.microsoft.azure.management.storage.ManagementPolicyRule;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class PolicyRule extends Diffable implements Copyable<ManagementPolicyRule> {
    private String name;
    private String type;
    private Boolean enabled;
    private PolicyDefinition definition;

    /**
     * Name of the rule.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Type of rule. Currently only supported value is ``Lifecycle``. Defaults to ``Lifecycle``.
     */
    public String getType() {
        if (type == null) {
            type = "Lifecycle";
        }

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Enable/Disable the rule. Defaults to ``true`` i.e Enabled.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The rule details. (Required)
     *
     * @sunresource gyro.azure.storage.PolicyDefinition
     */
    @Required
    @Updatable
    public PolicyDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(PolicyDefinition definition) {
        this.definition = definition;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public void copyFrom(ManagementPolicyRule rule) {
        setName(rule.name());
        setType(rule.type());
        setEnabled(rule.enabled());
        PolicyDefinition policyDefinition = newSubresource(PolicyDefinition.class);
        policyDefinition.copyFrom(rule.definition());
        setDefinition(policyDefinition);
    }

    ManagementPolicyRule toManagementPolicyRule() {
        ManagementPolicyRule rule = new ManagementPolicyRule();
        rule = rule.withName(getName())
            .withType(getType())
            .withEnabled(getEnabled())
            .withDefinition(getDefinition().toManagementPolicyDefinition());

        return rule;
    }
}
