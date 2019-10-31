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
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithSrvRecordEntryOrAttachable;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.SrvRecordSetBlank;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an SRV Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::srv-record-set
 *         name: "srvrecexample"
 *         ttl: 4
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *
 *         srv-record
 *             port: 80
 *             priority: 1
 *             target: "hi.com"
 *             weight: 100
 *         end
 *     end
 */
@Type("srv-record-set")
public class SrvRecordSetResource extends AzureResource implements Copyable<SrvRecordSet> {

    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private Set<SrvRecord> srvRecord;
    private Long ttl;
    private String id;

    /**
     * The DNS Zone where the Srv Record Set resides. (Required)
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The metadata for the Srv Record Set. (Optional)
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
     * The name of the Srv Record Set. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of srv Srv Records Set. (Required)
     *
     * @subresource gyro.azure.dns.SrvRecord
     */
    @Required
    @Updatable
    public Set<SrvRecord> getSrvRecord() {
        if (srvRecord == null) {
            srvRecord = new HashSet<>();
        }

        return srvRecord;
    }

    public void setSrvRecord(Set<SrvRecord> srvRecord) {
        this.srvRecord = srvRecord;
    }

    /**
     * The Time To Live in Seconds for the Srv Record Set in the set. (Required)
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
     * The ID of the Srv Record Set.
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
    public void copyFrom(SrvRecordSet srvRecordSet) {
        setSrvRecord(srvRecordSet.records().stream().map(o -> {
            SrvRecord srvRecord = newSubresource(SrvRecord.class);
            srvRecord.copyFrom(o);
            return srvRecord;
        }).collect(Collectors.toSet()));
        setMetadata(srvRecordSet.metadata());
        setName(srvRecordSet.name());
        setTtl(srvRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, srvRecordSet.parent().id()));
        setId(srvRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        SrvRecordSet srvRecordSet = client.dnsZones().getById(getDnsZone().getId()).srvRecordSets().getByName(getName());

        if (srvRecordSet == null) {
            return false;
        }

        copyFrom(srvRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        SrvRecordSetBlank<DnsZone.Update> defineSrvRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().defineSrvRecordSet(getName());

        WithSrvRecordEntryOrAttachable<DnsZone.Update> createSrvRecordSet = null;
        for (SrvRecord srvRecord : getSrvRecord()) {
            createSrvRecordSet = defineSrvRecordSet
                    .withRecord(srvRecord.getTarget(), srvRecord.getPort(), srvRecord.getPriority(), srvRecord.getWeight());
        }

        if (getTtl() != null) {
            createSrvRecordSet.withTimeToLive(getTtl());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createSrvRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createSrvRecordSet.attach();
        attach.apply();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateSrvRecordSet updateSrvRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateSrvRecordSet(getName());

        if (getTtl() != null) {
            updateSrvRecordSet.withTimeToLive(getTtl());
        }

        SrvRecordSetResource oldRecord = (SrvRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateSrvRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateSrvRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateSrvRecordSet.withoutMetadata(ele.getKey());
            updateSrvRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<SrvRecord> addRecords = comparator(getSrvRecord(), oldRecord.getSrvRecord());

        for (SrvRecord addRecord : addRecords) {
            updateSrvRecordSet.withRecord(addRecord.getTarget(), addRecord.getPort(), addRecord.getPriority(), addRecord.getWeight());
        }

        List<SrvRecord> removeRecords = comparator(oldRecord.getSrvRecord(), getSrvRecord());

        for (SrvRecord removeRecord : removeRecords) {
            updateSrvRecordSet.withoutRecord(removeRecord.getTarget(), removeRecord.getPort(), removeRecord.getPriority(), removeRecord.getWeight());
        }

        DnsZone.Update parent = updateSrvRecordSet.parent();
        parent.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutSrvRecordSet(getName()).apply();
    }

    private List<SrvRecord> comparator(Set<SrvRecord> original, Set<SrvRecord> compareTo) {
        List<SrvRecord> differences = new ArrayList<>(original);

        for (SrvRecord record : original) {
            for (SrvRecord comp : compareTo) {
                if (record.primaryKey().equals(comp.primaryKey())) {
                    differences.remove(record);
                }
            }
        }

        return differences;
    }
}
