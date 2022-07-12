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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.resourcemanager.network.models.ApplicationGateway.DefinitionStages.WithCreate;
import com.azure.resourcemanager.network.models.ApplicationGateway.Update;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend.UpdateDefinitionStages.WithAttach;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddress;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

/**
 * Creates a Backend.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     backend
 *         name: "backend-example"
 *         ip-addresses: [
 *             "10.0.0.3",
 *             "10.0.0.4"
 *         ]
 *     end
 */
public class Backend extends Diffable implements Copyable<ApplicationGatewayBackend> {

    private String name;
    private Set<String> ipAddresses;
    private Set<String> fqdns;

    /**
     * Name of the backend.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * List of ip addresses. Required if no fqdns are present.
     */
    @Updatable
    public Set<String> getIpAddresses() {
        if (ipAddresses == null) {
            ipAddresses = new HashSet<>();
        }

        return ipAddresses;
    }

    public void setIpAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    /**
     * List of fqdns. Required if no ip addresses are present.
     */
    @Updatable
    public Set<String> getFqdns() {
        if (fqdns == null) {
            fqdns = new HashSet<>();
        }

        return fqdns;
    }

    public void setFqdns(Set<String> fqdns) {
        this.fqdns = fqdns;
    }

    @Override
    public void copyFrom(ApplicationGatewayBackend backend) {
        setName(backend.name());
        setIpAddresses(backend.addresses()
            .stream()
            .map(ApplicationGatewayBackendAddress::ipAddress)
            .collect(Collectors.toSet()));
        setFqdns(backend.addresses()
            .stream()
            .map(ApplicationGatewayBackendAddress::fqdn)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithCreate createBackend(WithCreate attach) {
        ApplicationGatewayBackend.DefinitionStages.Blank<WithCreate> withCreateBlank = attach.defineBackend(getName());

        for (String ipAddress : getIpAddresses()) {
            attach = withCreateBlank.withIPAddress(ipAddress).attach();
        }

        for (String fqdn : getFqdns()) {
            attach = withCreateBlank.withFqdn(fqdn).attach();
        }

        return attach;
    }

    Update createBackend(Update update) {
        WithAttach<Update> updateWithAttach = update.defineBackend(getName());

        for (String ipAddress : getIpAddresses()) {
            updateWithAttach = updateWithAttach.withIPAddress(ipAddress);

        }

        for (String fqdn : getFqdns()) {
            updateWithAttach = updateWithAttach.withFqdn(fqdn);

        }

        updateWithAttach.attach();

        return update;
    }

    Update updateBackend(Update update, Set<String> oldIpAddress, Set<String> oldFqdns) {
        ApplicationGatewayBackend.Update updateWithAttach = update.updateBackend(getName());

        for (String ipAddress : oldIpAddress) {
            updateWithAttach = updateWithAttach.withoutIPAddress(ipAddress);
        }

        for (String ipAddress : getIpAddresses()) {
            updateWithAttach = updateWithAttach.withIPAddress(ipAddress);
        }

        for (String fqdn : oldFqdns) {
            updateWithAttach = updateWithAttach.withoutFqdn(fqdn);
        }

        for (String fqdn : getFqdns()) {
            updateWithAttach = updateWithAttach.withFqdn(fqdn);
        }

        updateWithAttach.parent();

        return update;
    }
}
