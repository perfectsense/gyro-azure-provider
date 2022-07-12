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

import com.azure.resourcemanager.network.models.LoadBalancerTcpProbe;
import gyro.azure.Copyable;

/**
 * Creates a tcp health check probe.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    health-check-probe-tcp
 *        name: "healthcheck-tcp-test-sat"
 *        interval: 5
 *        port: 80
 *        probes: 2
 *    end
 */
public class HealthCheckProbeTcp extends AbstractHealthCheckProbe implements Copyable<LoadBalancerTcpProbe> {

    @Override
    public void copyFrom(LoadBalancerTcpProbe tcpProbe) {
        setName(tcpProbe.name());
        setInterval(tcpProbe.intervalInSeconds());
        setPort(tcpProbe.port());
        setProbes(tcpProbe.numberOfProbes());
    }
}
