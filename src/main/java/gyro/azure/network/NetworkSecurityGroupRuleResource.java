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

package gyro.azure.network;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.SecurityRuleAccess;
import com.microsoft.azure.management.network.SecurityRuleDirection;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetworkSecurityGroupRuleResource extends AzureResource implements Copyable<NetworkSecurityRule> {
    private String name;
    private Boolean inboundRule;
    private Boolean allowRule;
    private Set<String> fromAddresses;
    private Set<String> fromPorts;
    private Set<String> toAddresses;
    private Set<String> toPorts;
    private ApplicationSecurityGroupResource fromApplicationSecurityGroup;
    private ApplicationSecurityGroupResource toApplicationSecurityGroup;
    private String description;
    private Integer priority;
    private String protocol;

    private static final Map<String, SecurityRuleProtocol> protocolMap = ImmutableMap
        .of("all", SecurityRuleProtocol.ASTERISK,
            "tcp", SecurityRuleProtocol.TCP,
            "udp", SecurityRuleProtocol.UDP);

    /**
     * Name of the Network Security Rule.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set Network Security Rule type as inbound or outbound. Defaults to ``true`` i.e inbound.
     */
    @Updatable
    public Boolean getInboundRule() {
        if (inboundRule == null) {
            inboundRule = true;
        }

        return inboundRule;
    }

    public void setInboundRule(Boolean inboundRule) {
        this.inboundRule = inboundRule;
    }

    /**
     * Set Network Security Rule to allow or block traffic. Defaults to ``true`` i.e allow.
     */
    @Updatable
    public Boolean getAllowRule() {
        if (allowRule == null) {
            allowRule = true;
        }

        return allowRule;
    }

    public void setAllowRule(Boolean allowRule) {
        this.allowRule = allowRule;
    }

    /**
     * A list of source addresses for the Network Security Rule to work. Required if ``from-Application-Security-Group`` is not set.
     */
    @Updatable
    public Set<String> getFromAddresses() {
        if (fromAddresses == null) {
            fromAddresses = new HashSet<>();
        }

        return fromAddresses;
    }

    public void setFromAddresses(Set<String> fromAddresses) {
        this.fromAddresses = fromAddresses;
    }

    /**
     * A list of source ports for the Network Security Rule to work.
     */
    @Required
    @Updatable
    public Set<String> getFromPorts() {
        if (fromPorts == null) {
            fromPorts = new HashSet<>();
        }

        return fromPorts;
    }

    public void setFromPorts(Set<String> fromPorts) {
        this.fromPorts = fromPorts;
    }

    /**
     * A list of destination addresses for the Network Security Rule to work. Required if ``to-Application-Security-Group`` is not set.
     */
    @Updatable
    public Set<String> getToAddresses() {
        if (toAddresses == null) {
            toAddresses = new HashSet<>();
        }

        return toAddresses;
    }

    public void setToAddresses(Set<String> toAddresses) {
        this.toAddresses = toAddresses;
    }

    /**
     * A list of destination ports for the Network Security Rule to work.
     */
    @Required
    @Updatable
    public Set<String> getToPorts() {
        if (toPorts == null) {
            toPorts = new HashSet<>();
        }

        return toPorts;
    }

    public void setToPorts(Set<String> toPorts) {
        this.toPorts = toPorts;
    }

    /**
     * Source Application Security Group for the Network Security Rule. Required if ``from-Addresses`` not set.
     */
    @Updatable
    public ApplicationSecurityGroupResource getFromApplicationSecurityGroup() {
        return fromApplicationSecurityGroup;
    }

    public void setFromApplicationSecurityGroup(ApplicationSecurityGroupResource fromApplicationSecurityGroup) {
        this.fromApplicationSecurityGroup = fromApplicationSecurityGroup;
    }

    /**
     * Destination Application Security Group for the Network Security Rule. Required if ``to-Addresses`` not set.
     */
    @Updatable
    public ApplicationSecurityGroupResource getToApplicationSecurityGroup() {
        return toApplicationSecurityGroup;
    }

    public void setToApplicationSecurityGroup(ApplicationSecurityGroupResource toApplicationSecurityGroup) {
        this.toApplicationSecurityGroup = toApplicationSecurityGroup;
    }

    /**
     * Description for the Network Security Rule.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Priority for the Network Security Rule.
     */
    @Required
    @Range(min = 100, max = 4096)
    @Updatable
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Protocol for the Network Security Rule. Defaults to ``all``.
     */
    @ValidStrings({"all", "tcp", "udp"})
    @Updatable
    public String getProtocol() {
        if (protocol == null) {
            protocol = "all";
        }

        return protocol.toLowerCase();
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void copyFrom(NetworkSecurityRule networkSecurityRule) {
        setName(networkSecurityRule.name());
        setInboundRule(networkSecurityRule.direction().equals(SecurityRuleDirection.INBOUND));
        setAllowRule(networkSecurityRule.access().equals(SecurityRuleAccess.ALLOW));

        if (networkSecurityRule.sourceAddressPrefix() != null) {
            setFromAddresses(new HashSet<>(Collections.singletonList(networkSecurityRule.sourceAddressPrefix())));
        } else if (!networkSecurityRule.sourceAddressPrefixes().isEmpty()) {
            setFromAddresses(new HashSet<>(networkSecurityRule.sourceAddressPrefixes()));
        }

        if (networkSecurityRule.sourcePortRange() != null) {
            setFromPorts(new HashSet<>(Collections.singletonList(networkSecurityRule.sourcePortRange())));
        } else if (!networkSecurityRule.sourcePortRanges().isEmpty()) {
            setFromPorts(new HashSet<>(networkSecurityRule.sourcePortRanges()));
        }

        if (networkSecurityRule.destinationAddressPrefix() != null) {
            setToAddresses(new HashSet<>(Collections.singletonList(networkSecurityRule.destinationAddressPrefix())));
        } else if (!networkSecurityRule.destinationAddressPrefixes().isEmpty()) {
            setToAddresses(new HashSet<>(networkSecurityRule.destinationAddressPrefixes()));
        }

        if (networkSecurityRule.destinationPortRange() != null) {
            setToPorts(new HashSet<>(Collections.singletonList(networkSecurityRule.destinationPortRange())));
        } else if (!networkSecurityRule.destinationPortRanges().isEmpty()) {
            setToPorts(new HashSet<>(networkSecurityRule.destinationPortRanges()));
        }

        setDescription(networkSecurityRule.description());
        setPriority(networkSecurityRule.priority());
        setProtocol(networkSecurityRule.protocol().toString().equals("*") ? "all" : networkSecurityRule.protocol().toString());

        if (!networkSecurityRule.sourceApplicationSecurityGroupIds().isEmpty()) {
            setFromApplicationSecurityGroup(findById(ApplicationSecurityGroupResource.class, networkSecurityRule.sourceApplicationSecurityGroupIds().iterator().next()));
        }

        if (!networkSecurityRule.destinationApplicationSecurityGroupIds().isEmpty()) {
            setToApplicationSecurityGroup(findById(ApplicationSecurityGroupResource.class, networkSecurityRule.destinationApplicationSecurityGroupIds().iterator().next()));
        }
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkSecurityGroupResource parent = (NetworkSecurityGroupResource) parent();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(parent.getId());

        NetworkSecurityRule.UpdateDefinitionStages
            .Blank<NetworkSecurityGroup.Update> updateBlank = networkSecurityGroup
            .update().defineRule(getName());

        NetworkSecurityRule.UpdateDefinitionStages.WithSourceAddressOrSecurityGroup<NetworkSecurityGroup.Update> withDirection;

        if (getInboundRule()) {
            withDirection = getAllowRule() ? updateBlank.allowInbound() : updateBlank.denyInbound();
        } else {
            withDirection = getAllowRule() ? updateBlank.allowOutbound() : updateBlank.denyOutbound();
        }

        NetworkSecurityRule.UpdateDefinitionStages.WithSourcePort<NetworkSecurityGroup.Update> withFromAddress;

        if (getFromApplicationSecurityGroup() != null) {
            withFromAddress = withDirection.withSourceApplicationSecurityGroup(getFromApplicationSecurityGroup().getId());
        } else {
            if (getFromAddresses().size() == 1 && getFromAddresses().contains("*")) {
                withFromAddress = withDirection.fromAnyAddress();
            } else {
                withFromAddress = withDirection.fromAddresses(getFromAddresses().toArray(new String[0]));
            }
        }

        NetworkSecurityRule.UpdateDefinitionStages.WithDestinationAddressOrSecurityGroup<NetworkSecurityGroup.Update> withFromPorts;

        if (getFromPorts().size() == 1 && getFromPorts().contains("*")) {
            withFromPorts = withFromAddress.fromAnyPort();
        } else {
            withFromPorts = withFromAddress.fromPortRanges(getFromPorts().toArray(new String[0]));
        }

        NetworkSecurityRule.UpdateDefinitionStages.WithDestinationPort<NetworkSecurityGroup.Update> withToAddress;

        if (getToApplicationSecurityGroup() != null) {
            withToAddress = withFromPorts.withDestinationApplicationSecurityGroup(getToApplicationSecurityGroup().getId());
        } else {
            if (getToAddresses().size() == 1 && getToAddresses().contains("*")) {
                withToAddress = withFromPorts.toAnyAddress();
            } else {
                withToAddress = withFromPorts.toAddresses(getToAddresses().toArray(new String[0]));
            }
        }

        NetworkSecurityRule.UpdateDefinitionStages.WithProtocol<NetworkSecurityGroup.Update> withToPorts;
        if (getToPorts().size() == 1 && getToPorts().contains("*")) {
            withToPorts = withToAddress.toAnyPort();
        } else {
            withToPorts = withToAddress.toPortRanges(getToPorts().toArray(new String[0]));
        }

        withToPorts
            .withProtocol(protocolMap.get(getProtocol()))
            .withDescription(getDescription())
            .withPriority(getPriority())
            .attach().apply();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Azure client = createClient();

        NetworkSecurityGroupResource parent = (NetworkSecurityGroupResource) parent();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(parent.getId());

        NetworkSecurityRule.Update update = networkSecurityGroup.update().updateRule(getName());

        if (getInboundRule()) {
            update = getAllowRule() ? update.allowInbound() : update.denyInbound();
        } else {
            update = getAllowRule() ? update.allowOutbound() : update.denyOutbound();
        }

        if (getFromApplicationSecurityGroup() != null) {
            update = update.withSourceApplicationSecurityGroup(getFromApplicationSecurityGroup().getId());
        } else {
            if (getFromAddresses().size() == 1 && getFromAddresses().contains("*")) {
                update = update.fromAnyAddress();
            } else {
                update = update.fromAddresses(getFromAddresses().toArray(new String[0]));
            }
        }

        if (getFromPorts().size() == 1 && getFromPorts().contains("*")) {
            update = update.fromAnyPort();
        } else {
            update = update.fromPortRanges(getFromPorts().toArray(new String[0]));
        }

        if (getToApplicationSecurityGroup() != null) {
            update = update.withDestinationApplicationSecurityGroup(getToApplicationSecurityGroup().getId());
        } else {
            if (getToAddresses().size() == 1 && getToAddresses().contains("*")) {
                update = update.toAnyAddress();
            } else {
                update = update.toAddresses(getToAddresses().toArray(new String[0]));
            }
        }

        if (getToPorts().size() == 1 && getToPorts().contains("*")) {
            update = update.toAnyPort();
        } else {
            update = update.toPortRanges(getToPorts().toArray(new String[0]));
        }

        update
            .withProtocol(protocolMap.get(getProtocol()))
            .withDescription(getDescription())
            .withPriority(getPriority())
            .parent().apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        NetworkSecurityGroupResource parent = (NetworkSecurityGroupResource) parent();

        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(parent.getId());

        networkSecurityGroup.update().withoutRule(getName()).apply();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }
}
