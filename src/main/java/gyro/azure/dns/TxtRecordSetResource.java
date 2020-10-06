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
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithTxtRecordTextValueOrAttachable;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.TxtRecordSet;
import gyro.core.scope.State;
import gyro.core.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an TXT Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::txt-record-set txt-record-set
 *         name: "txtrecexample"
 *         txt-records: ["record1", "record2"]
 *         ttl: 3
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *     end
 */
@Type("txt-record-set")
public class TxtRecordSetResource extends AzureResource implements Copyable<TxtRecordSet> {

    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private Set<String> txtRecords;
    private Long ttl;
    private String id;

    /**
     * The dns zone where the Txt Record Set resides.
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The metadata for the Txt Record Set. (Optional)
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
     * The name of the Txt Record Set.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of txt Txt Records Set.
     */
    @Required
    @Updatable
    public Set<String> getTxtRecords() {
        if (txtRecords == null) {
            txtRecords = new HashSet<>();
        }

        return txtRecords;
    }

    public void setTxtRecords(Set<String> txtRecords) {
        this.txtRecords = txtRecords;
    }

    /**
     * The Time To Live in Seconds for the Txt Record Set in the set.
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
     * The ID of the Txt Record Set.
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
    public void copyFrom(TxtRecordSet txtRecordSet) {
        getTxtRecords().clear();
        txtRecordSet.records().forEach(rec -> getTxtRecords().add(rec.value().get(0)));
        setMetadata(txtRecordSet.metadata());
        setName(txtRecordSet.name());
        setTtl(txtRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, txtRecordSet.parent().id()));
        setId(txtRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        TxtRecordSet txtRecordSet = client.dnsZones().getById(getDnsZone().getId()).txtRecordSets().getByName(getName());

        if (txtRecordSet == null) {
            return false;
        }

        copyFrom(txtRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        DnsRecordSet.UpdateDefinitionStages.TxtRecordSetBlank<DnsZone.Update> defineTxtRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().defineTxtRecordSet(getName());

        WithTxtRecordTextValueOrAttachable<DnsZone.Update> createTxtRecordSet = null;
        for (String txtRecord : getTxtRecords()) {
            createTxtRecordSet = defineTxtRecordSet.withText(txtRecord);
        }

        if (getTtl() != null) {
            createTxtRecordSet.withTimeToLive(getTtl());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createTxtRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createTxtRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.txtRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateTxtRecordSet updateTxtRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateTxtRecordSet(getName());

        if (getTtl() != null) {
            updateTxtRecordSet.withTimeToLive(getTtl());
        }

        TxtRecordSetResource oldRecord = (TxtRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldRecord.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateTxtRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateTxtRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateTxtRecordSet.withoutMetadata(ele.getKey());
            updateTxtRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        List<String> addRecords = new ArrayList<>(getTxtRecords());
        addRecords.removeAll(oldRecord.getTxtRecords());

        List<String> removeRecords = new ArrayList<>(oldRecord.getTxtRecords());
        removeRecords.removeAll(getTxtRecords());

        for (String addRecord : addRecords) {
            updateTxtRecordSet.withText(addRecord);
        }

        for (String removeRecord : removeRecords) {
            updateTxtRecordSet.withoutText(removeRecord);
        }

        DnsZone.Update parent = updateTxtRecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.txtRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutTxtRecordSet(getName()).apply();
    }
}
