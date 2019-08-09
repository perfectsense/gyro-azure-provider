package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackend.UpdateDefinitionStages.WithAttach;
import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Name of the backend. (Required)
     */
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
        setIpAddresses(backend.addresses().stream().map(ApplicationGatewayBackendAddress::ipAddress).collect(Collectors.toSet()));
        setFqdns(backend.addresses().stream().map(ApplicationGatewayBackendAddress::fqdn).filter(Objects::nonNull).collect(Collectors.toSet()));
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
        WithAttach<ApplicationGateway.Update> updateWithAttach = update.defineBackend(getName());

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
