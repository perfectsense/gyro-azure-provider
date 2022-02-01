package gyro.azure.containerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.resourcemanager.containerservice.models.ManagedClusterLoadBalancerProfileOutboundIpPrefixes;
import com.azure.resourcemanager.containerservice.models.ResourceReference;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class ClusterLoadBalancerOutboundIpPrefixes extends Diffable implements Copyable<ManagedClusterLoadBalancerProfileOutboundIpPrefixes> {

    private List<String> publicIpPrefixes;

    /**
     * The count of public ip prefixes.
     */
    @Required
    @Updatable
    public List<String> getPublicIpPrefixes() {
        if (publicIpPrefixes == null) {
            publicIpPrefixes = new ArrayList<>();
        }

        return publicIpPrefixes;
    }

    public void setPublicIpPrefixes(List<String> publicIpPrefixes) {
        this.publicIpPrefixes = publicIpPrefixes;
    }

    @Override
    public void copyFrom(ManagedClusterLoadBalancerProfileOutboundIpPrefixes model) {
        setPublicIpPrefixes(model.publicIpPrefixes().stream().map(ResourceReference::id).collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterLoadBalancerProfileOutboundIpPrefixes toOutboundIpPrefixes() {
        return new ManagedClusterLoadBalancerProfileOutboundIpPrefixes()
            .withPublicIpPrefixes(getPublicIpPrefixes()
                .stream()
                .map(o -> new ResourceReference().withId(o))
                .collect(Collectors.toList()));
    }
}
