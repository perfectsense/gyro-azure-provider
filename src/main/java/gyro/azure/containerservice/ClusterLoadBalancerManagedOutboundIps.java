package gyro.azure.containerservice;

import com.azure.resourcemanager.containerservice.models.ManagedClusterLoadBalancerProfileManagedOutboundIPs;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class ClusterLoadBalancerManagedOutboundIps extends Diffable implements Copyable<ManagedClusterLoadBalancerProfileManagedOutboundIPs> {

    private Integer count;

    /**
     * The count of managed outbound ips.
     */
    @Required
    @Updatable
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public void copyFrom(ManagedClusterLoadBalancerProfileManagedOutboundIPs model) {
        setCount(model.count());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterLoadBalancerProfileManagedOutboundIPs toManagedOutboundIps() {
        return new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(getCount());
    }
}
