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
import com.microsoft.azure.management.compute.Disk;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query disk.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    disk: $(external-query azure::disk {})
 */
@Type("disk")
public class DiskFinder extends AzureFinder<Disk, DiskResource> {
    private String id;

    /**
     * The ID of the Disk.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Disk> findAllAzure(Azure client) {
        return client.disks().list();
    }

    @Override
    protected List<Disk> findAzure(Azure client, Map<String, String> filters) {
        Disk disk = client.disks().getById(filters.get("id"));
        if (disk == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(disk);
        }
    }
}
