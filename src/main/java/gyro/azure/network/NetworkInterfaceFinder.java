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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkInterface;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query network interface.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    network-interface: $(external-query azure::network-interface {})
 */
@Type("network-interface")
public class NetworkInterfaceFinder extends AzureFinder<NetworkInterface, NetworkInterfaceResource> {
    private String id;

    /**
     * The ID of the Network Interface.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<NetworkInterface> findAllAzure(Azure client) {
        return client.networkInterfaces().list();
    }

    @Override
    protected List<NetworkInterface> findAzure(Azure client, Map<String, String> filters) {
        NetworkInterface networkInterface = client.networkInterfaces().getById(filters.get("id"));
        if (networkInterface == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(networkInterface);
        }
    }
}
