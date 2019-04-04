package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackend.UpdateDefinitionStages.WithAttach;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Backend extends Diffable {
    private String backendName;
    private List<String> ipAddresses;
    private List<String> fqdns;

    public Backend () {

    }

    public Backend(ApplicationGatewayBackend backend) {
        setBackendName(backend.name());
        setIpAddresses(backend.addresses().stream().map(ApplicationGatewayBackendAddress::ipAddress).collect(Collectors.toList()));
        setFqdns(backend.addresses().stream().map(ApplicationGatewayBackendAddress::fqdn).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getIpAddresses() {
        if (ipAddresses == null) {
            ipAddresses = new ArrayList<>();
        }

        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getFqdns() {
        if (fqdns == null) {
            fqdns = new ArrayList<>();
        }

        return fqdns;
    }

    public void setFqdns(List<String> fqdns) {
        this.fqdns = fqdns;
    }

    @Override
    public String primaryKey() {
        return getBackendName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("backend");

        if (!ObjectUtils.isBlank(getBackendName())) {
            sb.append(" - ").append(getBackendName());
        }

        return sb.toString();
    }

    Update createBackend(Update update) {
        WithAttach<ApplicationGateway.Update> updateWithAttach = update.defineBackend(getBackendName());

        for (String ipAddress : getIpAddresses()) {
            updateWithAttach = updateWithAttach.withIPAddress(ipAddress);

        }

        for (String fqdn : getFqdns()) {
            updateWithAttach = updateWithAttach.withFqdn(fqdn);

        }

        updateWithAttach.attach();

        return update;
    }

    Update updateBackend(Update update, List<String> oldIpAddress, List<String> oldFqdns) {
        ApplicationGatewayBackend.Update updateWithAttach = update.updateBackend(getBackendName());

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
