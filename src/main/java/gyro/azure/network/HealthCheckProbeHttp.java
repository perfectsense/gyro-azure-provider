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

import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import gyro.azure.Copyable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

/**
 * Creates a http health check probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    health-check-probe-http
 *        name: "healthcheck"
 *        interval: 8
 *        request-path: "/"
 *        port: 80
 *        probes: 3
 *    end
 */
public class HealthCheckProbeHttp extends AbstractHealthCheckProbe implements Copyable<LoadBalancerHttpProbe> {

    private String requestPath;

    /**
     * The HTTP request path by the probe to call to check the health status.
     */
    @Required
    @Updatable
    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    @Override
    public void copyFrom(LoadBalancerHttpProbe httpProbe) {
        setName(httpProbe.name());
        setInterval(httpProbe.intervalInSeconds());
        setRequestPath(httpProbe.requestPath());
        setPort(httpProbe.port());
        setProbes(httpProbe.numberOfProbes());
    }
}
