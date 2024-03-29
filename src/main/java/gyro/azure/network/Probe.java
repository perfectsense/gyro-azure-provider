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
import java.util.Set;

import com.azure.resourcemanager.network.models.ApplicationGateway.DefinitionStages.WithCreate;
import com.azure.resourcemanager.network.models.ApplicationGateway.Update;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe.UpdateDefinitionStages.WithAttach;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe.UpdateDefinitionStages.WithProtocol;
import com.azure.resourcemanager.network.models.ApplicationGatewayProtocol;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

/**
 * Creates a Probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     probe
 *         name: "probe-example"
 *         host-name: "example.com"
 *         path: "/path"
 *         interval: 40
 *         timeout: 40
 *         unhealthy-threshold: 4
 *         https-protocol: false
 *         http-response-codes: [
 *             "200-210"
 *         ]
 *         http-response-body-match: "probe-body"
 *     end
 */
public class Probe extends Diffable implements Copyable<ApplicationGatewayProbe> {

    private String name;
    private String hostName;
    private String path;
    private Integer interval;
    private Integer timeout;
    private Integer unhealthyThreshold;
    private Boolean httpsProtocol;
    private Set<String> httpResponseCodes;
    private String httpResponseBodyMatch;

    /**
     * Name of the Probe.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Host name associated with this Probe.
     */
    @Required
    @Updatable
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Path associated with this Probe.
     */
    @Required
    @Updatable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Interval for the Probe. Defaults to ``30`` sec.
     */
    @Updatable
    public Integer getInterval() {
        if (interval == null) {
            interval = 30;
        }

        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * Timeout for the Probe. Defaults to ``30`` sec.
     */
    @Updatable
    public Integer getTimeout() {
        if (timeout == null) {
            timeout = 30;
        }

        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Threshold for unhealthy instances before it triggers the Probe. Defaults to ``3``.
     */
    @Updatable
    public Integer getUnhealthyThreshold() {
        if (unhealthyThreshold == null) {
            unhealthyThreshold = 3;
        }

        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    /**
     * Enable https protocol for the Probe. Defaults to ``false``.
     */
    @Updatable
    public Boolean getHttpsProtocol() {
        if (httpsProtocol == null) {
            httpsProtocol = false;
        }

        return httpsProtocol;
    }

    public void setHttpsProtocol(Boolean httpsProtocol) {
        this.httpsProtocol = httpsProtocol;
    }

    /**
     * List of https response codes for the Probe.
     */
    @Updatable
    public Set<String> getHttpResponseCodes() {
        if (httpResponseCodes == null) {
            httpResponseCodes = new HashSet<>();
        }

        return httpResponseCodes;
    }

    public void setHttpResponseCodes(Set<String> httpResponseCodes) {
        this.httpResponseCodes = httpResponseCodes;
    }

    /**
     * String to match with the request body for the Probe.
     */
    @Updatable
    public String getHttpResponseBodyMatch() {
        return httpResponseBodyMatch;
    }

    public void setHttpResponseBodyMatch(String httpResponseBodyMatch) {
        this.httpResponseBodyMatch = httpResponseBodyMatch;
    }

    @Override
    public void copyFrom(ApplicationGatewayProbe probe) {
        setName(probe.name());
        setHostName(probe.host());
        setPath(probe.path());
        setInterval(probe.timeBetweenProbesInSeconds());
        setTimeout(probe.timeoutInSeconds());
        setUnhealthyThreshold(probe.retriesBeforeUnhealthy());
        setHttpResponseCodes(new HashSet<>(probe.healthyHttpResponseStatusCodeRanges()));
        setHttpResponseBodyMatch(probe.healthyHttpResponseBodyContents());
        setHttpsProtocol(probe.innerModel().protocol().equals(ApplicationGatewayProtocol.HTTPS));
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    WithCreate createProbe(WithCreate attach) {
        ApplicationGatewayProbe.DefinitionStages.WithProtocol<WithCreate> attachWithProtocol = attach.defineProbe(
                getName())
            .withHost(getHostName()).withPath(getPath());

        ApplicationGatewayProbe.DefinitionStages.WithAttach<WithCreate> withCreateWithAttach;

        if (getHttpsProtocol()) {
            withCreateWithAttach = attachWithProtocol.withHttps().withTimeoutInSeconds(getTimeout());
        } else {
            withCreateWithAttach = attachWithProtocol.withHttp().withTimeoutInSeconds(getTimeout());
        }

        withCreateWithAttach = withCreateWithAttach.withRetriesBeforeUnhealthy(getUnhealthyThreshold())
            .withTimeBetweenProbesInSeconds(getInterval());

        if (!getHttpResponseCodes().isEmpty()) {
            attach = withCreateWithAttach.withHealthyHttpResponseStatusCodeRanges(new HashSet<>(getHttpResponseCodes()))
                .withHealthyHttpResponseBodyContents(getHttpResponseBodyMatch())
                .attach();
        } else {
            attach = withCreateWithAttach.attach();
        }

        return attach;
    }

    Update createProbe(Update update) {
        WithProtocol<Update> updateWithProtocol = update.defineProbe(getName())
            .withHost(getHostName()).withPath(getPath());

        WithAttach<Update> updateWithAttach;

        if (getHttpsProtocol()) {
            updateWithAttach = updateWithProtocol.withHttps().withTimeoutInSeconds(getTimeout());
        } else {
            updateWithAttach = updateWithProtocol.withHttp().withTimeoutInSeconds(getTimeout());
        }

        updateWithAttach = updateWithAttach.withRetriesBeforeUnhealthy(getUnhealthyThreshold())
            .withTimeBetweenProbesInSeconds(getInterval());

        if (!getHttpResponseCodes().isEmpty()) {
            update = updateWithAttach.withHealthyHttpResponseStatusCodeRanges(new HashSet<>(getHttpResponseCodes()))
                .withHealthyHttpResponseBodyContents(getHttpResponseBodyMatch())
                .attach();
        } else {
            update = updateWithAttach.attach();
        }

        return update;
    }

    Update updateProbe(Update update) {
        ApplicationGatewayProbe.Update partialUpdate = update.updateProbe(getName())
            .withHost(getHostName()).withPath(getPath());

        if (getHttpsProtocol()) {
            partialUpdate = partialUpdate.withHttps().withTimeoutInSeconds(getTimeout());
        } else {
            partialUpdate = partialUpdate.withHttp().withTimeoutInSeconds(getTimeout());
        }

        partialUpdate = partialUpdate.withRetriesBeforeUnhealthy(getUnhealthyThreshold())
            .withTimeBetweenProbesInSeconds(getInterval());

        if (!getHttpResponseCodes().isEmpty()) {
            update = partialUpdate.withoutHealthyHttpResponseStatusCodeRanges()
                .withHealthyHttpResponseStatusCodeRanges(new HashSet<>(getHttpResponseCodes()))
                .withHealthyHttpResponseBodyContents(getHttpResponseBodyMatch())
                .parent();
        } else {
            update = partialUpdate.parent();
        }

        return update;
    }
}
