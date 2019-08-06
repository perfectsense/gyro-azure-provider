package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

/**
 * Creates a Listener.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     listener
 *         name: "AG-ex-1-listener"
 *         port: 81
 *     end
 */
public class Listener extends Diffable implements Copyable<ApplicationGatewayListener> {
    private String name;
    private Integer port;
    private Boolean privateFrontend;

    /**
     * Name of the listener. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Port for the listener to listen to. (Required)
     */
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Enable private frontend. Defaults to false.
     */
    @Updatable
    public Boolean getPrivateFrontend() {
        if (privateFrontend == null) {
            privateFrontend = false;
        }

        return privateFrontend;
    }

    public void setPrivateFrontend(Boolean privateFrontend) {
        this.privateFrontend = privateFrontend;
    }

    @Override
    public void copyFrom(ApplicationGatewayListener listener) {
        setName(listener.name());
        setPort(listener.frontendPortNumber());
        setPrivateFrontend(listener.frontend().isPrivate());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithCreate createListener(WithCreate attach) {
        if (getPrivateFrontend()) {
            attach = attach.defineListener(getName())
                .withPrivateFrontend()
                .withFrontendPort(getPort())
                .attach();
        } else {
            attach = attach.defineListener(getName())
                .withPublicFrontend()
                .withFrontendPort(getPort())
                .attach();
        }

        return attach;
    }

    Update createListener(Update update) {
        if (getPrivateFrontend()) {
            update = update.defineListener(getName())
                .withPrivateFrontend()
                .withFrontendPort(getPort())
                .attach();
        } else {
            update = update.defineListener(getName())
                .withPublicFrontend()
                .withFrontendPort(getPort())
                .attach();
        }

        return update;
    }

    Update updateListener(Update update) {
        if (getPrivateFrontend()) {
            update = update.updateListener(getName())
                .withPrivateFrontend()
                .withFrontendPort(getPort())
                .parent();
        } else {
            update = update.updateListener(getName())
                .withPublicFrontend()
                .withFrontendPort(getPort())
                .parent();
        }

        return update;
    }
}
