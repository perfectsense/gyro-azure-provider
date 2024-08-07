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

package gyro.azure.dns;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

/**
 * Query dns zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dns-zone: $(external-query azure::dns-zone {})
 */
@Type("dns-zone")
public class DnsZoneFinder extends AzureFinder<AzureResourceManager, DnsZone, DnsZoneResource> {
    private String id;

    /**
     * The ID of the DNS Zone.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<DnsZone> findAllAzure(AzureResourceManager client) {
        return client.dnsZones().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DnsZone> findAzure(AzureResourceManager client, Map<String, String> filters) {
        if (ObjectUtils.isBlank(filters.get("id"))) {
            throw new GyroException("'id' is required.");
        }

        DnsZone dnsZone = client.dnsZones().getById(filters.get("id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(dnsZone);
        }
    }
}
