package gyro.azure.network;

import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

/**
 * Creates a Listener.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     listener
 *         listener-name: "AG-ex-1-listener"
 *         port: 81
 *     end
 */
public class Listener extends Diffable {
    private String listenerName;
    private Integer port;
    private Boolean privateFrontend;

    public Listener() {

    }

    public Listener(ApplicationGatewayListener listener) {
        setListenerName(listener.name());
        setPort(listener.frontendPortNumber());
        setPrivateFrontend(listener.frontend().isPrivate());
    }

    /**
     * Name of the listener. (Required)
     */
    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    /**
     * Port for the listener to listen to. (Required)
     */
    @ResourceUpdatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Enable private frontend. Defaults to false.
     */
    @ResourceUpdatable
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
    public String primaryKey() {
        return getListenerName();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("listener");

        if (!ObjectUtils.isBlank(getListenerName())) {
            sb.append(" - ").append(getListenerName());
        }

        return sb.toString();
    }

    WithCreate createListener(WithCreate attach) {
        if (getPrivateFrontend()) {
            attach = attach.defineListener(getListenerName())
                .withPrivateFrontend()
                .withFrontendPort(getPort())
                .attach();
        } else {
            attach = attach.defineListener(getListenerName())
                .withPublicFrontend()
                .withFrontendPort(getPort())
                .attach();
        }

        return attach;
    }

    Update createListener(Update update) {
        if (getPrivateFrontend()) {
            update = update.defineListener(getListenerName())
                .withPrivateFrontend()
                .withFrontendPort(getPort())
                .attach();
        } else {
            update = update.defineListener(getListenerName())
                .withPublicFrontend()
                .withFrontendPort(getPort())
                .attach();
        }

        return update;
    }

    Update updateListener(Update update) {
        if (getPrivateFrontend()) {
            update = update.updateListener(getListenerName())
                .withPrivateFrontend()
                .withFrontendPort(getPort())
                .parent();
        } else {
            update = update.updateListener(getListenerName())
                .withPublicFrontend()
                .withFrontendPort(getPort())
                .parent();
        }

        return update;
    }
}
