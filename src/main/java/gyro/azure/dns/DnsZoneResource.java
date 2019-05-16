package gyro.azure.dns;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.CNameRecordSet;
import com.microsoft.azure.management.dns.CaaRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.MXRecordSet;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.TxtRecordSet;
import com.microsoft.azure.management.dns.ZoneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a DNS Zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::dns-zone dns-zone-example-zones
 *         name: "zones.example.com"
 *         public-access: false
 *         resource-group-name: $(azure::resource-group resource-group-dns-zone-example | resource-group-name)
 *         tags: {
 *            Name: "resource-group-dns-zone-example"
 *         }
 *
 *         a-record-set
 *             name: "arecexample"
 *             time-to-live: "3"
 *             ipv4-addresses: ["10.0.0.1"]
 *         end
 *
 *         aaaa-record-set
 *             name: "aaaarecexample"
 *             ipv6-addresses: ["2001:0db8:85a3:0000:0000:8a2e:0370:7334", "2001:0db8:85a3:0000:0000:8a2e:0370:7335"]
 *         end
 *     end
 */
@ResourceType("dns-zone")
public class DnsZoneResource extends AzureResource {

    private List<ARecordSetResource> aRecordSet;
    private List<AaaaRecordSetResource> aaaaRecordSet;
    private List<CaaRecordSetResource> caaRecordSet;
    private List<CnameRecordSetResource> cnameRecordSet;
    private DnsZone.Update dnsZone;
    private String id;
    private Boolean publicAccess;
    private List<MxRecordSetResource> mxRecordSet;
    private String name;
    private List<PtrRecordSetResource> ptrRecordSet;
    private List<String> registrationVirtualNetworkIds;
    private List<String> resolutionVirtualNetworkIds;
    private String resourceGroupName;
    private List<SrvRecordSetResource> srvRecordSet;
    private Map<String, String> tags;
    private List<TxtRecordSetResource> txtRecordSet;

    /**
     * The list of a record sets. (Optional)
     */
    @ResourceUpdatable
    public List<ARecordSetResource> getaRecordSet() {
        if (aRecordSet == null) {
            aRecordSet = new ArrayList<>();
        }

        return aRecordSet;
    }

    public void setaRecordSet(List<ARecordSetResource> aRecordSet) {
        this.aRecordSet = aRecordSet;
    }

    /**
     * The list of aaaa record sets. (Optional)
     */
    @ResourceUpdatable
    public List<AaaaRecordSetResource> getAaaaRecordSet() {
        if (aaaaRecordSet == null) {
            aaaaRecordSet = new ArrayList<>();
        }

        return aaaaRecordSet;
    }

    public void setAaaaRecordSet(List<AaaaRecordSetResource> aaaaRecordSet) {
        this.aaaaRecordSet = aaaaRecordSet;
    }

    /**
     * The list of caa record sets. (Optional)
     */
    @ResourceUpdatable
    public List<CaaRecordSetResource> getCaaRecordSet() {
        if (caaRecordSet == null) {
            caaRecordSet = new ArrayList<>();
        }

        return caaRecordSet;
    }

    public void setCaaRecordSet(List<CaaRecordSetResource> caaRecordSet) {
        this.caaRecordSet = caaRecordSet;
    }

    /**
     * The list of cname record sets. (Optional)
     */
    @ResourceUpdatable
    public List<CnameRecordSetResource> getCnameRecordSet() {
        if (cnameRecordSet == null) {
            cnameRecordSet = new ArrayList<>();
        }

        return cnameRecordSet;
    }

    public void setCnameRecordSet(List<CnameRecordSetResource> cnameRecordSet) {
        this.cnameRecordSet = cnameRecordSet;
    }

    /**
     * The id of the dns zone.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Determines if the dns zone is public or private. Defaults to public (true). (Optional)
     */
    @ResourceUpdatable
    public Boolean getPublicAccess() {
        if (publicAccess == null) {
            publicAccess = true;
        }

        return publicAccess;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * The list of mx record sets. (Optional)
     */
    @ResourceUpdatable
    public List<MxRecordSetResource> getMxRecordSet() {
        if (mxRecordSet == null) {
            mxRecordSet = new ArrayList<>();
        }

        return mxRecordSet;
    }

    public void setMxRecordSet(List<MxRecordSetResource> mxRecordSet) {
        this.mxRecordSet = mxRecordSet;
    }

    /**
     * The name of the dns zone. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of ptr record sets. (Optional)
     */
    @ResourceUpdatable
    public List<PtrRecordSetResource> getPtrRecordSet() {
        if (ptrRecordSet == null) {
            ptrRecordSet = new ArrayList<>();
        }

        return ptrRecordSet;
    }

    public void setPtrRecordSet(List<PtrRecordSetResource> ptrRecordSet) {
        this.ptrRecordSet = ptrRecordSet;
    }

    /**
     * A list of virtual network id's that register hostnames in a private dns zone.
     * Can be used when the access is private. (Optional)
     */
    @ResourceUpdatable
    public List<String> getRegistrationVirtualNetworkIds() {
        if (registrationVirtualNetworkIds == null) {
            registrationVirtualNetworkIds = new ArrayList<>();
        }

        return registrationVirtualNetworkIds;
    }

    public void setRegistrationVirtualNetworkIds(List<String> registrationVirtualNetworkIds) {
        this.registrationVirtualNetworkIds = registrationVirtualNetworkIds;
    }

    /**
     * A list of virtual network id's that resolve records in a private dns zone.
     * Can be used when the access is private. (Optional)
     */
    @ResourceUpdatable
    public List<String> getResolutionVirtualNetworkIds() {
        if (resolutionVirtualNetworkIds == null) {
            resolutionVirtualNetworkIds = new ArrayList<>();
        }

        return resolutionVirtualNetworkIds;
    }

    public void setResolutionVirtualNetworkIds(List<String> resolutionVirtualNetworkIds) {
        this.resolutionVirtualNetworkIds = resolutionVirtualNetworkIds;
    }

    /**
     * The name of the resource group where the dns zone is found. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The list of srv record sets. (Optional)
     */
    @ResourceUpdatable
    public List<SrvRecordSetResource> getSrvRecordSet() {
        if (srvRecordSet == null) {
            srvRecordSet = new ArrayList<>();
        }

        return srvRecordSet;
    }

    public void setSrvRecordSet(List<SrvRecordSetResource> srvRecordSet) {
        this.srvRecordSet = srvRecordSet;
    }

    /**
     * The tags associated with the dns zone. (Optional)
     */
    @ResourceUpdatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The list of txt record sets. (Optional)
     */
    @ResourceUpdatable
    public List<TxtRecordSetResource> getTxtRecordSet() {
        if (txtRecordSet == null) {
            txtRecordSet = new ArrayList<>();
        }

        return txtRecordSet;
    }

    public void setTxtRecordSet(List<TxtRecordSetResource> txtRecordSet) {
        this.txtRecordSet = txtRecordSet;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        DnsZone dnsZone = client.dnsZones().getById(getId());

        if (dnsZone == null) {
            return false;
        }

        getaRecordSet().clear();
        for (ARecordSet aRecordSet : dnsZone.aRecordSets().list()) {
            ARecordSetResource aRecordSetResource = new ARecordSetResource(aRecordSet);
            getaRecordSet().add(aRecordSetResource);
        }

        getAaaaRecordSet().clear();
        for (AaaaRecordSet aaaaRecordSet : dnsZone.aaaaRecordSets().list()) {
            AaaaRecordSetResource aaaaRecordSetResource = new AaaaRecordSetResource(aaaaRecordSet);
            getAaaaRecordSet().add(aaaaRecordSetResource);
        }

        getCaaRecordSet().clear();
        for (CaaRecordSet caaRecordSet : dnsZone.caaRecordSets().list()) {
            CaaRecordSetResource caaRecordSetResource = new CaaRecordSetResource(caaRecordSet);
            getCaaRecordSet().add(caaRecordSetResource);
        }

        getCnameRecordSet().clear();
        for (CNameRecordSet cNameRecordSet : dnsZone.cNameRecordSets().list()) {
            CnameRecordSetResource cnameRecordSetResource = new CnameRecordSetResource(cNameRecordSet);
            getCnameRecordSet().add(cnameRecordSetResource);
        }

        getMxRecordSet().clear();
        for (MXRecordSet mxRecordSet : dnsZone.mxRecordSets().list()) {
            MxRecordSetResource mxRecordSetResource = new MxRecordSetResource(mxRecordSet);
            getMxRecordSet().add(mxRecordSetResource);
        }

        getPtrRecordSet().clear();
        for (PtrRecordSet ptrRecordSet : dnsZone.ptrRecordSets().list()) {
            PtrRecordSetResource ptrRecordSetResource = new PtrRecordSetResource(ptrRecordSet);
            getPtrRecordSet().add(ptrRecordSetResource);
        }

        getSrvRecordSet().clear();
        for (SrvRecordSet srvRecordSet : dnsZone.srvRecordSets().list()) {
            SrvRecordSetResource srvRecordSetResource = new SrvRecordSetResource(srvRecordSet);
            getSrvRecordSet().add(srvRecordSetResource);
        }

        getTxtRecordSet().clear();
        for (TxtRecordSet txtRecordSet : dnsZone.txtRecordSets().list()) {
            TxtRecordSetResource txtRecordSetResource = new TxtRecordSetResource(txtRecordSet);
            getTxtRecordSet().add(txtRecordSetResource);
        }

        setId(dnsZone.id());
        setPublicAccess(dnsZone.accessType() == ZoneType.PUBLIC);
        setName(dnsZone.name());
        setRegistrationVirtualNetworkIds(dnsZone.registrationVirtualNetworkIds());
        setResolutionVirtualNetworkIds(dnsZone.resolutionVirtualNetworkIds());
        setResourceGroupName(dnsZone.resourceGroupName());
        setTags(dnsZone.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        DnsZone.DefinitionStages.WithCreate withCreate;

        withCreate = client.dnsZones().define(getName()).withExistingResourceGroup(getResourceGroupName());

        if (getPublicAccess() != null && !getPublicAccess()) {
            if (getRegistrationVirtualNetworkIds().isEmpty() && getResolutionVirtualNetworkIds().isEmpty()) {
                withCreate.withPrivateAccess(getRegistrationVirtualNetworkIds(), getResolutionVirtualNetworkIds());
            } else {
                withCreate.withPrivateAccess();
            }
        } else {
            withCreate.withPublicAccess();
        }

        DnsZone dnsZone = withCreate.withTags(getTags()).create();

        setId(dnsZone.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        DnsZone.Update update = getDnsZone(client);

        if (!getPublicAccess()) {
            if (getRegistrationVirtualNetworkIds() != null && getResolutionVirtualNetworkIds() != null) {
                update.withPrivateAccess(getRegistrationVirtualNetworkIds(), getResolutionVirtualNetworkIds());
            } else {
                update.withPrivateAccess();
            }
        } else {
            update.withPublicAccess();
        }

        update.withTags(getTags()).apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.dnsZones().deleteById(getId());
    }

    @Override
    public String toDisplayString() { return "dns zone " + getName(); }

    public DnsZone.Update getDnsZone(Azure client) {
        if (dnsZone == null) {
            dnsZone = client.dnsZones().getByResourceGroup(getResourceGroupName(), getName()).update();
        }

        return dnsZone;
    }
}
