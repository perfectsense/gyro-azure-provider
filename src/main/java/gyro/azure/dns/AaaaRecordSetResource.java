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
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.AaaaRecordSetBlank;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.WithAaaaRecordIPv6AddressOrAttachable;
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
 * Creates an AAAA Record Set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::aaaa-record-set aaaa-record-set
 *         dns-zone: $(azure::dns-zone dns-zone-example-zones)
 *         name: "aaaarecexample"
 *         ipv6-addresses: ["2001:0db8:85a3:0000:0000:8a2e:0370:7334", "2001:0db8:85a3:0000:0000:8a2e:0370:7335"]
 *     end
 */
@Type("aaaa-record-set")
public class AaaaRecordSetResource extends AzureResource implements Copyable<AaaaRecordSet> {

    private DnsZoneResource dnsZone;
    private Set<String> ipv6Addresses;
    private Map<String, String> metadata;
    private String name;
    private Long ttl;
    private String id;

    /**
     * The Dns Zone where the AAAA Record Set resides.
     */
    @Required
    public DnsZoneResource getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(DnsZoneResource dnsZone) {
        this.dnsZone = dnsZone;
    }

    /**
     * The IPV6 addresses associated with the AAAA Record Set set.
     */
    @Required
    @Updatable
    public Set<String> getIpv6Addresses() {
        if (ipv6Addresses == null) {
            ipv6Addresses = new HashSet<>();
        }

        ipv6Addresses = ipv6Addresses.stream().map(this::adjustIp).collect(Collectors.toSet());
        return ipv6Addresses;
    }

    public void setIpv6Addresses(Set<String> ipv6Addresses) {
        this.ipv6Addresses = ipv6Addresses;
    }

    /**
     * The metadata for the AAAA Record Set. (Optional)
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
     * The name of the AAAA Record Set.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Time To Live in Seconds for the AAAA Record Set in the set.
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
     * The ID for the AAAA Record Set.
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
    public void copyFrom(AaaaRecordSet aaaaRecordSet) {
        getIpv6Addresses().clear();
        setIpv6Addresses(new HashSet<>(aaaaRecordSet.ipv6Addresses()));
        setMetadata(aaaaRecordSet.metadata());
        setName(aaaaRecordSet.name());
        setTtl(aaaaRecordSet.timeToLive());
        setDnsZone(findById(DnsZoneResource.class, aaaaRecordSet.parent().id()));
        setId(aaaaRecordSet.id());
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        AaaaRecordSet aaaaRecordSet = client.dnsZones().getById(getDnsZone().getId()).aaaaRecordSets().getByName(getName());

        if (aaaaRecordSet == null) {
            return false;
        }

        copyFrom(aaaaRecordSet);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        AaaaRecordSetBlank<DnsZone.Update> defineAaaaRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().defineAaaaRecordSet(getName());

        WithAaaaRecordIPv6AddressOrAttachable<DnsZone.Update> createAaaaRecordSet = null;
        for (String ip : getIpv6Addresses()) {
            createAaaaRecordSet = defineAaaaRecordSet.withIPv6Address(ip);
        }

        if (getTtl() != null) {
            createAaaaRecordSet.withTimeToLive(getTtl());
        }

        for (Map.Entry<String,String> e : getMetadata().entrySet()) {
            createAaaaRecordSet.withMetadata(e.getKey(), e.getValue());
        }

        DnsZone.Update attach = createAaaaRecordSet.attach();
        DnsZone dnsZone = attach.apply();
        copyFrom(dnsZone.aaaaRecordSets().getByName(getName()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsRecordSet.UpdateAaaaRecordSet updateAaaaRecordSet =
                client.dnsZones().getById(getDnsZone().getId()).update().updateAaaaRecordSet(getName());

        if (getTtl() != null) {
            updateAaaaRecordSet.withTimeToLive(getTtl());
        }

        AaaaRecordSetResource oldResource = (AaaaRecordSetResource) current;

        List<String> addIps = new ArrayList<>(getIpv6Addresses());
        addIps.removeAll(oldResource.getIpv6Addresses());

        List<String> removeIps = new ArrayList<>(oldResource.getIpv6Addresses());
        removeIps.removeAll(getIpv6Addresses());

        for (String ip : addIps) {
            updateAaaaRecordSet.withIPv6Address(ip);
        }

        for (String ip : removeIps) {
            updateAaaaRecordSet.withoutIPv6Address(ip);
        }

        Map<String, String> pendingMetaData = getMetadata();
        Map<String, String> currentMetaData = oldResource.getMetadata();

        MapDifference<String, String> diff = Maps.difference(currentMetaData, pendingMetaData);

        //add new metadata
        diff.entriesOnlyOnRight().forEach(updateAaaaRecordSet::withMetadata);
        //delete removed metadata
        diff.entriesOnlyOnLeft().keySet().forEach(updateAaaaRecordSet::withoutMetadata);

        //update changed keys
        for (Map.Entry<String, MapDifference.ValueDifference<String>> ele : diff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> disc = ele.getValue();
            updateAaaaRecordSet.withoutMetadata(ele.getKey());
            updateAaaaRecordSet.withMetadata(ele.getKey(), disc.rightValue());
        }

        DnsZone.Update parent = updateAaaaRecordSet.parent();
        DnsZone dnsZone = parent.apply();
        copyFrom(dnsZone.aaaaRecordSets().getByName(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.dnsZones().getById(getDnsZone().getId()).update().withoutAaaaRecordSet(getName()).apply();
    }

    private String adjustIp(String ip) {
        StringBuilder expandedIp = new StringBuilder();
        String [] splitDouble = ip.split("::");

        if (splitDouble.length == 1) {
            expandedIp = expandSegments(expandedIp, splitDouble[0].split(":"));
        } else {

            String[] firstHalf = splitDouble[0].split(":");
            String[] secondHalf = splitDouble[1].split(":");

            // take the first half and format
            expandedIp = expandSegments(expandedIp, firstHalf);

            //add the zeros to the place the double colon was found
            int addZeros = 8 - firstHalf.length - secondHalf.length;
            for (int i = 0; i < addZeros; i++) {
                expandedIp.append("0000");
            }

            // take the second half and format
            expandedIp = expandSegments(expandedIp, secondHalf);
        }

        int count = 0, offset = 4;
        while (count < 7) {
            expandedIp.insert(offset, ":");
            count++;
            offset+=5;
        }

        return expandedIp.toString();
    }

    private StringBuilder expandSegments(StringBuilder builder, String[] segments){
        for (String first : segments) {
            if (first.length() < 4) {
                builder.append(addLeadingZeros(first));
            } else {
                builder.append(first);
            }
        }

        return builder;
    }

    private String addLeadingZeros(String bitBlock) {
        StringBuilder zeros = new StringBuilder();

        int offset = 4 - bitBlock.length();

        for (int i = 0; i < offset; i++) {
            zeros.insert(0, "0");
        }

        zeros.append(bitBlock);

        return zeros.toString();
    }
}
