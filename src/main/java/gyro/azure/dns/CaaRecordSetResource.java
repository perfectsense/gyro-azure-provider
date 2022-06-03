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
import com.azure.resourcemanager.dns.models.CaaRecordSet;
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
import java.util.stream.Collectors;

/**
 * Creates an CAA Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::caa-record-set caa-record-set
 *         name: "caaexample"
 *         ttl: 3
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *
 *         caa-record
 *             flags: 1
 *             tag: "tag1"
 *             value: "val1"
 *         end
 *
 *         caa-record
 *             flags: 2
 *             tag: "tag2"
 *             value: "val2"
 *         end
 *     end
 */
@Type("caa-record-set")
public class CaaRecordSetResource extends AzureResource implements Copyable<CaaRecordSet> {

    private Set<CaaRecord> caaRecord;
    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private Long ttl;
    private String id;

    /**
     * The set of CAA Records associated with the CAA Record Set.
     *
     * @subresource gyro.azure.dns.CaaRecord
     */
    @Required
    @Updatable
    public Set<CaaRecord> getCaaRecord() {
        if (caaRecord == null) {
            caaRecord = new HashSet<>();
        }

        return caaRecord;
    }

    public void setCaaRecord(Set<CaaRecord> caaRecord) {
        this.caaRecord = caaRecord;
    }

    /**
     * The DNS Zone where the CAA Record resides Set.
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The metadata for the CAA Record Set.
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
     * The name of the CAA Record Set.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live in Seconds for the CAA Record Set in the set.
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
     * The ID of the CAA Record Set.
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
    public void copyFrom(CaaRecordSet caaRecordSet) {
        setCaaRecord(caaRecordSet.records().stream().map(o -> {
            CaaRecord caaRecord = newSubresource(CaaRecord.class);
            caaRecord.copyFrom(o);
            return caaRecord;
        }).collect(Collectors.toSet()));
        setMetadata(caaRecordSet.metadata());
        setName(caaRecordSet.name());
        setTtl(caaRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, caaRecordSet.parent().id()));
        setId(caaRecordSet.id());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        CaaRecordSet caaRecordSet = client.dnsZones().getById(getDnsZone().getId()).caaRecordSets().getByName(getName());

        if (caaRecordSet == null) {
            return false;
        }

        copyFrom(caaRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        DnsRecordSet.UpdateDefinitionStages.CaaRecordSetBlank<DnsZone.Update> defineCaaRecordSet =
            client.dnsZones()
            .getById(getDnsZone().getId())
            .update()
            .defineCaaRecordSet(getName());

        DnsRecordSet.UpdateDefinitionStages.WithCaaRecordEntryOrAttachable<DnsZone.Update> createCaaRecordSet = null;
        for (CaaRecord caaRecord : getCaaRecord()) {
            createCaaRecordSet = defineCaaRecordSet.withRecord(
                caaRecord.getFlags(),
                caaRecord.getTag(),
                caaRecord.getValue());
        }

        if (getTtl() != null) {
            createCaaRecordSet.withTimeToLive(getTtl());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCaaRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createCaaRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.caaRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createClient();

        DnsRecordSet.UpdateCaaRecordSet updateCaaRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateCaaRecordSet(getName());

        if (getTtl() != null) {
            updateCaaRecordSet.withTimeToLive(getTtl());
        }

        CaaRecordSetResource oldRecord = (CaaRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateCaaRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateCaaRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateCaaRecordSet.withoutMetadata(ele.getKey());
            updateCaaRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<CaaRecord> addRecords = comparator(getCaaRecord(), oldRecord.getCaaRecord());

        for (CaaRecord addRecord : addRecords) {
            updateCaaRecordSet.withRecord(addRecord.getFlags(), addRecord.getTag(), addRecord.getValue());
        }

        List<CaaRecord> removeRecords = comparator(oldRecord.getCaaRecord(), getCaaRecord());

        for (CaaRecord removeRecord : removeRecords) {
            updateCaaRecordSet.withoutRecord(removeRecord.getFlags(), removeRecord.getTag(), removeRecord.getValue());
        }

        DnsZone.Update parent = updateCaaRecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.caaRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutCaaRecordSet(getName()).apply();
    }

    private List<CaaRecord> comparator(Set<CaaRecord> original, Set<CaaRecord> compareTo) {
        List<CaaRecord> differences = new ArrayList<>(original);

        for (CaaRecord record : original) {
            for (CaaRecord comp : compareTo) {
                if (record.primaryKey().equals(comp.primaryKey())) {
                    differences.remove(record);
                }
            }
        }

        return differences;
    }
}
