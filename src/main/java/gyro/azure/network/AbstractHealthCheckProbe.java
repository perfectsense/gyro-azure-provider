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

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public abstract class AbstractHealthCheckProbe extends Diffable {
    private String name;
    private Integer interval;
    private Integer port;
    private Integer probes;

    /**
     * The name of the Health Probe. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The amount of time before a Health Probe signals unhealthy. (Required)
     */
    @Required
    @Updatable
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * The destination port used for the Health Probe. (Required)
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
     * The number of Health probe failures required by an unhealthy target to be considered unhealthy. (Required)
     */
    @Required
    @Updatable
    public Integer getProbes() {
        return probes;
    }

    public void setProbes(Integer probes) {
        this.probes = probes;
    }

    public String primaryKey() {
        return getName();
    }
}
