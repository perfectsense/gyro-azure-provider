package gyro.azure.containerservice;

import com.azure.resourcemanager.containerservice.models.ManagedClusterManagedOutboundIpProfile;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class ClusterManagedOutboundIpProfile extends Diffable implements Copyable<ManagedClusterManagedOutboundIpProfile> {

    private Integer count;

    /**
     * The desired number of outbound IPs created/managed by Azure.
     */
    @Required
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public void copyFrom(ManagedClusterManagedOutboundIpProfile model) {
        setCount(model.count());
    }

    @Override
    public String primaryKey() {
        return getCount().toString();
    }

    protected ManagedClusterManagedOutboundIpProfile toOutboundIpProfile() {
        return new ManagedClusterManagedOutboundIpProfile().withCount(getCount());
    }
}
