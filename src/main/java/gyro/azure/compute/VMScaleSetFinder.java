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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query scale set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    scale-set: $(external-query azure::scale-set {})
 */
@Type("scale-set")
public class VMScaleSetFinder extends AzureFinder<VirtualMachineScaleSet, VMScaleSetResource> {
    private String id;

    /**
     * The ID of the Scale Set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<VirtualMachineScaleSet> findAllAzure(Azure client) {
        return client.virtualMachineScaleSets().list();
    }

    @Override
    protected List<VirtualMachineScaleSet> findAzure(Azure client, Map<String, String> filters) {
        VirtualMachineScaleSet scaleSet = client.virtualMachineScaleSets().getById(filters.get("id"));
        if (scaleSet == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(scaleSet);
        }
    }
}
