package gyro.azure.containerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.resourcemanager.containerservice.models.ManagedClusterLoadBalancerProfileOutboundIPs;
import com.azure.resourcemanager.containerservice.models.ResourceReference;
import gyro.azure.Copyable;
import gyro.azure.network.PublicIpAddressResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class ClusterLoadBalancerOutboundIps extends Diffable implements Copyable<ManagedClusterLoadBalancerProfileOutboundIPs> {

    private List<PublicIpAddressResource> publicIps;

    /**
     * The list of public ips.
     */
    @Required
    @Updatable
    public List<PublicIpAddressResource> getPublicIps() {
        if (publicIps == null) {
            publicIps = new ArrayList<>();
        }

        return publicIps;
    }

    public void setPublicIps(List<PublicIpAddressResource> publicIps) {
        this.publicIps = publicIps;
    }

    @Override
    public void copyFrom(ManagedClusterLoadBalancerProfileOutboundIPs model) {
        setPublicIps(model.publicIPs().stream()
            .map(ResourceReference::id)
            .map(id -> findById(PublicIpAddressResource.class, id))
            .collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterLoadBalancerProfileOutboundIPs toOutboundIPs() {
        return new ManagedClusterLoadBalancerProfileOutboundIPs()
            .withPublicIPs(getPublicIps()
                .stream()
                .map(o -> new ResourceReference().withId(o.getId()))
                .collect(Collectors.toList()));
    }
}
