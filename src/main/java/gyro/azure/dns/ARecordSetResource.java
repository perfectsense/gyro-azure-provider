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

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.DnsRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an A Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::a-record-set a-record-set
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *         name: "arecexample"
 *         ttl: 3
 *         ipv4-addresses: ["10.0.0.1"]
 *     end
 */
@Type("a-record-set")
public class ARecordSetResource extends AzureResource implements Copyable<ARecordSet> {

    private DnsZoneResource dnsZone;
    private Set<String> ipv4Addresses;
    private Map<String, String> metadata;
    private String name;
    private Long ttl;
    private String id;

    /**
     * The DNS Zone where the A Record Set resides.
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The ipv4 addresses associated with the A Record Set set.
     */
    @Required
    @Updatable
    public Set<String> getIpv4Addresses() {
        if (ipv4Addresses == null) {
            ipv4Addresses = new HashSet<>();
        }

        return ipv4Addresses;
    }

    public void setIpv4Addresses(Set<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
    }

    /**
     * The metadata for the A Record Set.
     */
    @Updatable
    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * The name of the A Record Set.
     */
    @Required
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live in Seconds for the A Record Set in the set.
     */
    @Required
    @Updatable
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * The ID of the A Record Set.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(ARecordSet aRecordSet) {
        getIpv4Addresses().clear();
        setIpv4Addresses(new HashSet<>(aRecordSet.ipv4Addresses()));
        setMetadata(aRecordSet.metadata());
        setName(aRecordSet.name());
        setTtl(aRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, aRecordSet.parent().id()));
        setId(aRecordSet.id());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createResourceManagerClient();

        com.azure.resourcemanager.dns.models.ARecordSet aRecordSet = client.dnsZones()
            .getById(getDnsZone().getId())
            .aRecordSets()
            .getByName(getName());

        if (aRecordSet == null) {
            return false;
        }

        copyFrom(aRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        DnsRecordSet.UpdateDefinitionStages.ARecordSetBlank<DnsZone.Update> updateARecordSetBlank = client.dnsZones()
            .getById(getDnsZone().getId())
            .update()
            .defineARecordSet(getName());

        DnsRecordSet.UpdateDefinitionStages.WithARecordIPv4AddressOrAttachable<DnsZone.Update> createARecordSet = null;
        for (String ip : getIpv4Addresses()) {
            createARecordSet = updateARecordSetBlank.withIPv4Address(ip);
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createARecordSet.withMetadata(e.getKey(), e.getValue());
        }

        if (getTtl() != null) {
            createARecordSet.withTimeToLive(getTtl());
        }

        DnsZone.Update attach = createARecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.aRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createResourceManagerClient();

        DnsRecordSet.UpdateARecordSet updateARecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateARecordSet(getName());

        if (getTtl() != null) {
            updateARecordSet.withTimeToLive(getTtl());
        }

        ARecordSetResource oldResource = (ARecordSetResource) current;

        List<String> addIps = new ArrayList<>(getIpv4Addresses());
        addIps.removeAll(oldResource.getIpv4Addresses());

        List<String> removeIps = new ArrayList<>(oldResource.getIpv4Addresses());
        removeIps.removeAll(getIpv4Addresses());

        for (String ip : addIps) {
            updateARecordSet.withIPv4Address(ip);
        }

        for (String ip : removeIps) {
            updateARecordSet.withoutIPv4Address(ip);
        }

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldResource.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateARecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateARecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateARecordSet.withoutMetadata(ele.getKey());
            updateARecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        DnsZone.Update parent = updateARecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.aRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createResourceManagerClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutARecordSet(getName()).apply();
    }
}
