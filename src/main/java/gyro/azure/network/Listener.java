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

import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

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
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Port for the Listener to listen to. (Required)
     */
    @Required
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Enable private frontend for the Listener. Defaults to ``false``.
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
