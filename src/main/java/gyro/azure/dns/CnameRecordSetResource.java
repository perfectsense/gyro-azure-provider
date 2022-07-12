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
import com.azure.resourcemanager.dns.models.CnameRecordSet;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates an CNAME Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::cname-record-set
 *         name: "cnamerecexample"
 *         ttl: 5
 *         alias: "cnamerecalias"
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *     end
 */
@Type("cname-record-set")
public class CnameRecordSetResource extends AzureResource implements Copyable<CnameRecordSet> {

    private String alias;
    private DnsZoneResource dnsZone;
    private Map<String, String> metadata;
    private String name;
    private Long ttl;
    private String id;

    /**
     * The alias for the Cname Record Set.
     */
    @Required
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The DNS Zone where the Cname Record Set resides.
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The metadata for the Cname Record Set.
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
     * The name of the Cname Record Set.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live in Seconds for the Cname Records Set in the set.
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
     * The ID of the Cname Record Set.
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
    public void copyFrom(CnameRecordSet cnameRecordSet) {
        setAlias(cnameRecordSet.canonicalName());
        setMetadata(cnameRecordSet.metadata());
        setName(cnameRecordSet.name());
        setTtl(cnameRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, cnameRecordSet.parent().id()));
        setId(cnameRecordSet.id());
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        CnameRecordSet cnameRecordSet = client.dnsZones().getById(getDnsZone().getId()).cNameRecordSets().getByName(getName());

        if (cnameRecordSet == null) {
            return false;
        }

        copyFrom(cnameRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        DnsRecordSet.UpdateDefinitionStages.WithCNameRecordSetAttachable<DnsZone.Update> createCNameRecordSet = client.dnsZones()
            .getById(getDnsZone().getId())
            .update().
            defineCNameRecordSet(getName())
            .withAlias(getAlias());

        if (getTtl() != null) {
            createCNameRecordSet.withTimeToLive(getTtl());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createCNameRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createCNameRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.cNameRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        AzureResourceManager client = createClient();

        DnsRecordSet.UpdateCNameRecordSet updateCNameRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateCNameRecordSet(getName());

        if (getAlias() != null) {
            updateCNameRecordSet.withAlias(getAlias());
        }

        if (getTtl() != null) {
            updateCNameRecordSet.withTimeToLive(getTtl());
        }

        CnameRecordSetResource oldResource = (CnameRecordSetResource) current;

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldResource.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateCNameRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateCNameRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateCNameRecordSet.withoutMetadata(ele.getKey());
            updateCNameRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        DnsZone.Update parent = updateCNameRecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.cNameRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutCaaRecordSet(getName()).apply();
    }
}
