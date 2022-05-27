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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import gyro.azure.AzureResourceManagerFinder;
import gyro.core.Type;

/**
 * Query network security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    network-security-group: $(external-query azure::network-security-group {})
 */
@Type("network-security-group")
public class NetworkSecurityGroupFinder
    extends AzureResourceManagerFinder<NetworkSecurityGroup, NetworkSecurityGroupResource> {

    private String id;

    /**
     * The ID of the Network Security Group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<NetworkSecurityGroup> findAllAzure(AzureResourceManager client) {
        return client.networkSecurityGroups().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<NetworkSecurityGroup> findAzure(AzureResourceManager client, Map<String, String> filters) {
        NetworkSecurityGroup networkSecurityGroup = client.networkSecurityGroups().getById(filters.get("id"));
        if (networkSecurityGroup == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(networkSecurityGroup);
        }
    }
}
