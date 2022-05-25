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

package gyro.azure.compute;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import gyro.azure.AzureResourceManagerFinder;
import gyro.core.Type;

/**
 * Query virtual machine.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    virtual-machine: $(external-query azure::virtual-machine {})
 */
@Type("virtual-machine")
public class VirtualMachineFinder extends AzureResourceManagerFinder<VirtualMachine, VirtualMachineResource> {

    private String id;

    /**
     * The ID of the Virtual Machine.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<VirtualMachine> findAllAzure(AzureResourceManager client) {
        return client.virtualMachines().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<VirtualMachine> findAzure(AzureResourceManager client, Map<String, String> filters) {
        VirtualMachine virtualMachine = client.virtualMachines().getById(filters.get("id"));

        if (virtualMachine == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(virtualMachine);
        }
    }
}
