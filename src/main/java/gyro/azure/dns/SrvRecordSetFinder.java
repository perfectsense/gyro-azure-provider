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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureFinder;
import gyro.core.GyroException;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query srv record set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    srv-record-set: $(external-query azure::srv-record-set {})
 */
@Type("srv-record-set")
public class SrvRecordSetFinder extends AzureFinder<SrvRecordSet, SrvRecordSetResource> {
    private String dnsZoneId;
    private String name;

    /**
     * The ID of the DNS Zone.
     */
    public String getDnsZoneId() {
        return dnsZoneId;
    }

    public void setDnsZoneId(String dnsZoneId) {
        this.dnsZoneId = dnsZoneId;
    }

    /**
     * The Name of the Srv Record Set.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SrvRecordSet> findAllAzure(Azure client) {
        return client.dnsZones().list().stream().map(o -> o.srvRecordSets().list()).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    protected List<SrvRecordSet> findAzure(Azure client, Map<String, String> filters) {
        if (ObjectUtils.isBlank(filters.get("dns-zone-id"))) {
            throw new GyroException("'dns-zone-id' is required.");
        }

        DnsZone dnsZone = client.dnsZones().getById(filters.get("dns-zone-id"));
        if (dnsZone == null) {
            return Collections.emptyList();
        } else {
            if (filters.containsKey("name")) {
                return Collections.singletonList(dnsZone.srvRecordSets().getByName(filters.get("name")));
            } else {
                return dnsZone.srvRecordSets().list();
            }
        }
    }
}
