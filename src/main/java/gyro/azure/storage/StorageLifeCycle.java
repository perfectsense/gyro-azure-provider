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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.BlobTypes;
import com.microsoft.azure.management.storage.ManagementPolicy;
import com.microsoft.azure.management.storage.ManagementPolicyRule;
import com.microsoft.azure.management.storage.ManagementPolicySchema;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.PolicyRule.DefinitionStages.WithBlobTypesToFilterFor;
import com.microsoft.azure.management.storage.PolicyRule.DefinitionStages.WithPolicyRuleAttachable;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StorageLifeCycle extends AzureResource implements Copyable<ManagementPolicy> {
    private String name;
    private String id;
    private Date lastModified;
    private Set<PolicyRule> rule;

    /**
     *  The name of the lifecycle policy. Currently only supported value is ``DefaultManagementPolicy``. Defaults to ``DefaultManagementPolicy``.
     */
    @ValidStrings("DefaultManagementPolicy")
    public String getName() {
        if (name == null) {
            name = "DefaultManagementPolicy";
        }

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A set of rules for the lifecycle policy.
     *
     * @subresource gyro.azure.storage.PolicyRule
     */
    @Updatable
    @Required
    public Set<PolicyRule> getRule() {
        if (rule == null) {
            rule = new HashSet<>();
        }

        return rule;
    }

    public void setRule(Set<PolicyRule> rule) {
        this.rule = rule;
    }

    /**
     * The ID of the lifecycle policy.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The last modification time of the lifecycle policy.
     */
    @Output
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public void copyFrom(ManagementPolicy policy) {
        setName(policy.name());
        setId(policy.id());
        setLastModified(policy.lastModifiedTime().toDate());

        getRule().clear();
        for (ManagementPolicyRule rule : policy.policy().rules()) {
            PolicyRule policyRule = newSubresource(PolicyRule.class);
            policyRule.copyFrom(rule);
            getRule().add(policyRule);
        }
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Azure client = createClient();

        StorageAccountResource parent = (StorageAccountResource) parent();

        if (!parent.getUpgradeAccountV2()) {
            throw new GyroException("Cannot create lifecycle for a storage account not of 'General Purpose Account Kind V2'.");
        }

        StorageAccount storageAccount = client.storageAccounts().getById(parent.getId());

        ManagementPolicy.DefinitionStages.WithRule withRule = storageAccount.manager().managementPolicies().define(getName())
            .withExistingStorageAccount(parent.getResourceGroup().getName(), parent.getName());

        ManagementPolicy.DefinitionStages.WithCreate create = null;

        for (PolicyRule rule : getRule()) {
            WithBlobTypesToFilterFor withBlobTypesToFilterFor = create == null
                ? withRule.defineRule(rule.getName()).withLifecycleRuleType()
                : create.defineRule(rule.getName()).withLifecycleRuleType();

            WithPolicyRuleAttachable withPolicyRuleAttachable = withBlobTypesToFilterFor
                .withBlobTypesToFilterFor(rule.getDefinition().getFilter().getBlobTypes().stream().map(BlobTypes::fromString).collect(Collectors.toList()))
                .withPrefixesToFilterFor(new ArrayList<>(rule.getDefinition().getFilter().getPrefixMatches()))
                .withActionsOnBaseBlob(rule.getDefinition().getAction().getBaseBlob().toManagementPolicyBaseBlob());

            if (rule.getDefinition().getAction().getSnapshot() != null) {
                create = withPolicyRuleAttachable.withActionsOnSnapShot(rule.getDefinition().getAction().getSnapshot().toManagementPolicySnapShot()).attach();
            } else {
                create = withPolicyRuleAttachable.attach();
            }
        }

        ManagementPolicy policy = create.create();

        state.save();

        // Api does not allow creating one or more disabled rule when creating a policy.
        // If one or more rules are are configured to be disabled then an update is required.
        if (getRule().stream().anyMatch(o -> !o.getEnabled())) {
            update(ui, state, this, new HashSet<>());
        } else {
            copyFrom(policy);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        ManagementPolicySchema policySchema = new ManagementPolicySchema();
        policySchema.withRules(getRule().stream().map(PolicyRule::toManagementPolicyRule).collect(Collectors.toList()));

        ManagementPolicy.Update update = getManagementPolicy(client).update();
        ManagementPolicy policy = update.withPolicy(policySchema).apply();

        copyFrom(policy);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        StorageAccountResource parent = (StorageAccountResource) parent();
        StorageAccount storageAccount = client.storageAccounts().getById(parent.getId());

        storageAccount.manager()
            .managementPolicies()
            .inner()
            .delete(parent.getResourceGroup().getName(), parent.getName());
    }

    private ManagementPolicy getManagementPolicy(Azure client) {
        StorageAccountResource parent = (StorageAccountResource) parent();
        StorageAccount storageAccount = client.storageAccounts().getById(parent.getId());

        return storageAccount.manager()
            .managementPolicies()
            .getAsync(parent.getResourceGroup().getName(), parent.getName())
            .toBlocking()
            .single();
    }
}
