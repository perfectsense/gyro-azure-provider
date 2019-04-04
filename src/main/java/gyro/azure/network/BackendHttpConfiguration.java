package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;

public class BackendHttpConfiguration extends Diffable {
    private String backendHttpConfigurationName;
    private Integer port;

    public BackendHttpConfiguration() {

    }

    public BackendHttpConfiguration(ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration) {
        setBackendHttpConfigurationName(backendHttpConfiguration.name());
        setPort(backendHttpConfiguration.port());
    }

    public String getBackendHttpConfigurationName() {
        return backendHttpConfigurationName;
    }

    public void setBackendHttpConfigurationName(String backendHttpConfigurationName) {
        this.backendHttpConfigurationName = backendHttpConfigurationName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String primaryKey() {
        return getBackendHttpConfigurationName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("backend http configuration");

        if (!ObjectUtils.isBlank(getBackendHttpConfigurationName())) {
            sb.append(" - ").append(getBackendHttpConfigurationName());
        }

        return sb.toString();
    }

    Update createBackendHttpConfiguration(Update update) {
        update.defineBackendHttpConfiguration(getBackendHttpConfigurationName())
            .withPort(getPort())
            .attach();

        return update;
    }

    Update updateBackendHttpConfiguration(Update update) {
        update.updateBackendHttpConfiguration(getBackendHttpConfigurationName())
            .withPort(getPort())
            .parent();

        return update;
    }
}
