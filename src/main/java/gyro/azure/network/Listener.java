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

import java.util.Optional;

import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.ApplicationGateway.Update;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.psddev.dari.util.ObjectUtils;
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
    private ListenerSslCertificate sslCertificate;

    /**
     * Name of the listener.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Port for the Listener to listen to.
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

    /**
     * Ssl configuration for the listener.
     *
     * @subresource gyro.azure.network.ListenerSslCertificate
     */
    @Updatable
    public ListenerSslCertificate getSslCertificate() {
        return sslCertificate;
    }

    public void setSslCertificate(ListenerSslCertificate sslCertificate) {
        this.sslCertificate = sslCertificate;
    }

    @Override
    public void copyFrom(ApplicationGatewayListener listener) {
        setName(listener.name());
        setPort(listener.frontendPortNumber());
        setPrivateFrontend(listener.frontend().isPrivate());
        setSslCertificate(Optional.ofNullable(listener.sslCertificate()).map(o -> {
            ListenerSslCertificate sslCertificate = newSubresource(ListenerSslCertificate.class);
            sslCertificate.copyFrom(o);
            return sslCertificate;
        }).orElse(null));
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithCreate createListener(WithCreate attach) {
        ApplicationGatewayListener.DefinitionStages.WithAttach<WithCreate> withCreateWithAttach;

        if (getPrivateFrontend()) {
            withCreateWithAttach = attach.defineListener(getName())
                .withPrivateFrontend()
                .withFrontendPort(getPort());
        } else {
            withCreateWithAttach = attach.defineListener(getName())
                .withPublicFrontend()
                .withFrontendPort(getPort());
        }

        ListenerSslCertificate sslCertificate = getSslCertificate();
        if (sslCertificate != null) {
            attach = withCreateWithAttach.withHttps()
                .withSslCertificate(sslCertificate.getCertificateName())
                .attach();

            if (!ObjectUtils.isBlank(sslCertificate.getCertificateSecretId())) {
                attach = attach.defineSslCertificate(sslCertificate.getCertificateName())
                    .withKeyVaultSecretId(sslCertificate.getCertificateSecretId())
                    .attach();
            }
        } else {
            attach = withCreateWithAttach.withHttp().attach();
        }

        return attach;
    }

    Update createListener(Update update) {
        ApplicationGatewayListener.UpdateDefinitionStages.WithAttach<Update> updateWithAttach;

        if (getPrivateFrontend()) {
            updateWithAttach = update.defineListener(getName())
                .withPrivateFrontend()
                .withFrontendPort(getPort());
        } else {
            updateWithAttach = update.defineListener(getName())
                .withPublicFrontend()
                .withFrontendPort(getPort());
        }

        ListenerSslCertificate sslCertificate = getSslCertificate();
        if (sslCertificate != null) {
            update = updateWithAttach.withHttps()
                .withSslCertificate(sslCertificate.getCertificateName())
                .attach();

            if (!ObjectUtils.isBlank(sslCertificate.getCertificateSecretId())) {
                update = update.defineSslCertificate(sslCertificate.getCertificateName())
                    .withKeyVaultSecretId(sslCertificate.getCertificateSecretId())
                    .attach();
            }
        } else {
            update = updateWithAttach.withHttp().attach();
        }

        return update;
    }

    Update updateListener(Update update) {
        ApplicationGatewayListener.Update listenerUpdate = update.updateListener(getName());

        if (getPrivateFrontend()) {
            listenerUpdate = listenerUpdate.withPrivateFrontend();
        } else {
            listenerUpdate = listenerUpdate.withPublicFrontend();
        }

        listenerUpdate = listenerUpdate.withFrontendPort(getPort());

        ListenerSslCertificate sslCertificate = getSslCertificate();
        if (sslCertificate != null) {
            update = listenerUpdate.withHttps()
                .withSslCertificate(sslCertificate.getCertificateName())
                .parent();

            if (!ObjectUtils.isBlank(sslCertificate.getCertificateSecretId())) {
                update = update.defineSslCertificate(sslCertificate.getCertificateName())
                    .withKeyVaultSecretId(sslCertificate.getCertificateSecretId())
                    .attach();
            }
        } else {
            update = listenerUpdate.withHttp().parent();
        }

        return update;
    }
}
