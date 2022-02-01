package gyro.azure.containerservice;

import com.azure.resourcemanager.containerservice.models.Expander;
import com.azure.resourcemanager.containerservice.models.ManagedClusterPropertiesAutoScalerProfile;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class ClusterPropertiesAutoScalerProfile extends Diffable implements Copyable<ManagedClusterPropertiesAutoScalerProfile> {

    private String expander;
    private String balanceSimilarNodeGroups;
    private String maxEmptyBulkDelete;
    private String maxGracefulTerminationSec;
    private String maxTotalUnreadyPercentage;
    private String newPodScaleUpDelay;
    private String okTotalUnreadyCount;
    private String scaleDownDelayAfterAdd;
    private String scaleDownDelayAfterDelete;
    private String scaleDownDelayAfterFailure;
    private String scaleDownUnreadyTime;
    private String scaleDownUnneededTime;
    private String scaleDownUtilizationThreshold;
    private String scanInterval;
    private String skipNodesWithSystemPods;
    private String skipNodesWithLocalStorage;

    /**
     * The expander for the autoscaler profile.
     */
    @Required
    @Updatable
    @ValidStrings({"least-waste", "most-pods", "priority", "random"})
    public String getExpander() {
        return expander;
    }

    public void setExpander(String expander) {
        this.expander = expander;
    }

    /**
     * The balance similar node groups for the autoscaler profile.
     */
    @Updatable
    public String getBalanceSimilarNodeGroups() {
        return balanceSimilarNodeGroups;
    }

    public void setBalanceSimilarNodeGroups(String balanceSimilarNodeGroups) {
        this.balanceSimilarNodeGroups = balanceSimilarNodeGroups;
    }

    /**
     * The max empty bulk delete for the autoscaler profile.
     */
    @Updatable
    public String getMaxEmptyBulkDelete() {
        return maxEmptyBulkDelete;
    }

    public void setMaxEmptyBulkDelete(String maxEmptyBulkDelete) {
        this.maxEmptyBulkDelete = maxEmptyBulkDelete;
    }

    /**
     * The max graceful termination sec for the autoscaler profile.
     */
    @Updatable
    public String getMaxGracefulTerminationSec() {
        return maxGracefulTerminationSec;
    }

    public void setMaxGracefulTerminationSec(String maxGracefulTerminationSec) {
        this.maxGracefulTerminationSec = maxGracefulTerminationSec;
    }

    /**
     * The max total unready percentage for the autoscaler profile.
     */
    @Updatable
    public String getMaxTotalUnreadyPercentage() {
        return maxTotalUnreadyPercentage;
    }

    public void setMaxTotalUnreadyPercentage(String maxTotalUnreadyPercentage) {
        this.maxTotalUnreadyPercentage = maxTotalUnreadyPercentage;
    }

    /**
     * The new pod scale up delay for the autoscaler profile.
     */
    @Updatable
    public String getNewPodScaleUpDelay() {
        return newPodScaleUpDelay;
    }

    public void setNewPodScaleUpDelay(String newPodScaleUpDelay) {
        this.newPodScaleUpDelay = newPodScaleUpDelay;
    }

    /**
     * The ok total unready count for the autoscaler profile.
     */
    @Updatable
    public String getOkTotalUnreadyCount() {
        return okTotalUnreadyCount;
    }

    public void setOkTotalUnreadyCount(String okTotalUnreadyCount) {
        this.okTotalUnreadyCount = okTotalUnreadyCount;
    }

    /**
     * The scale down delay after add for the autoscaler profile. Values must be an integer followed by an 'm'. No unit of time other than minutes (m) is supported.
     */
    @Updatable
    public String getScaleDownDelayAfterAdd() {
        return scaleDownDelayAfterAdd;
    }

    public void setScaleDownDelayAfterAdd(String scaleDownDelayAfterAdd) {
        this.scaleDownDelayAfterAdd = scaleDownDelayAfterAdd;
    }

    /**
     * The scale down delay after delete for the autoscaler profile. Values must be an integer followed by an 'm'. No unit of time other than minutes (m) is supported.
     */
    @Updatable
    public String getScaleDownDelayAfterDelete() {
        return scaleDownDelayAfterDelete;
    }

    public void setScaleDownDelayAfterDelete(String scaleDownDelayAfterDelete) {
        this.scaleDownDelayAfterDelete = scaleDownDelayAfterDelete;
    }

    /**
     * The scale down delay after failure for the autoscaler profile. Values must be an integer followed by an 'm'. No unit of time other than minutes (m) is supported.
     */
    @Updatable
    public String getScaleDownDelayAfterFailure() {
        return scaleDownDelayAfterFailure;
    }

    public void setScaleDownDelayAfterFailure(String scaleDownDelayAfterFailure) {
        this.scaleDownDelayAfterFailure = scaleDownDelayAfterFailure;
    }

    /**
     * The scale down unready time for the autoscaler profile.
     */
    @Updatable
    public String getScaleDownUnreadyTime() {
        return scaleDownUnreadyTime;
    }

    public void setScaleDownUnreadyTime(String scaleDownUnreadyTime) {
        this.scaleDownUnreadyTime = scaleDownUnreadyTime;
    }

    /**
     * The scale down unneeded time for the autoscaler profile.
     */
    @Updatable
    public String getScaleDownUnneededTime() {
        return scaleDownUnneededTime;
    }

    public void setScaleDownUnneededTime(String scaleDownUnneededTime) {
        this.scaleDownUnneededTime = scaleDownUnneededTime;
    }

    /**
     * The scale down utilization threshold for the autoscaler profile.
     */
    @Updatable
    public String getScaleDownUtilizationThreshold() {
        return scaleDownUtilizationThreshold;
    }

    public void setScaleDownUtilizationThreshold(String scaleDownUtilizationThreshold) {
        this.scaleDownUtilizationThreshold = scaleDownUtilizationThreshold;
    }

    /**
     * The scan interval for the autoscaler profile.
     */
    @Updatable
    public String getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(String scanInterval) {
        this.scanInterval = scanInterval;
    }

    /**
     * The skip nodes with system pods for the autoscaler profile.
     */
    @Updatable
    public String getSkipNodesWithSystemPods() {
        return skipNodesWithSystemPods;
    }

    public void setSkipNodesWithSystemPods(String skipNodesWithSystemPods) {
        this.skipNodesWithSystemPods = skipNodesWithSystemPods;
    }

    /**
     * The skip nodes with local storage for the autoscaler profile.
     */
    @Updatable
    public String getSkipNodesWithLocalStorage() {
        return skipNodesWithLocalStorage;
    }

    public void setSkipNodesWithLocalStorage(String skipNodesWithLocalStorage) {
        this.skipNodesWithLocalStorage = skipNodesWithLocalStorage;
    }

    @Override
    public void copyFrom(ManagedClusterPropertiesAutoScalerProfile model) {
        setExpander(model.expander().toString());
        setBalanceSimilarNodeGroups(model.balanceSimilarNodeGroups());
        setMaxEmptyBulkDelete(model.maxEmptyBulkDelete());
        setMaxGracefulTerminationSec(model.maxGracefulTerminationSec());
        setMaxTotalUnreadyPercentage(model.maxTotalUnreadyPercentage());
        setNewPodScaleUpDelay(model.newPodScaleUpDelay());
        setOkTotalUnreadyCount(model.okTotalUnreadyCount());
        setScaleDownDelayAfterAdd(model.scaleDownDelayAfterAdd());
        setScaleDownDelayAfterDelete(model.scaleDownDelayAfterDelete());
        setScaleDownDelayAfterFailure(model.scaleDownDelayAfterFailure());
        setScaleDownUnreadyTime(model.scaleDownUnreadyTime());
        setScaleDownUnneededTime(model.scaleDownUnneededTime());
        setScaleDownUtilizationThreshold(model.scaleDownUtilizationThreshold());
        setScanInterval(model.scanInterval());
        setSkipNodesWithSystemPods(model.skipNodesWithSystemPods());
        setSkipNodesWithLocalStorage(model.skipNodesWithLocalStorage());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterPropertiesAutoScalerProfile toAutoScalerProfile() {
        ManagedClusterPropertiesAutoScalerProfile profile = new ManagedClusterPropertiesAutoScalerProfile();

        if (getExpander() != null) {
            profile.withExpander(Expander.fromString(getExpander()));
        }

        if (getBalanceSimilarNodeGroups() != null) {
            profile.withBalanceSimilarNodeGroups(getBalanceSimilarNodeGroups());
        }

        if (getMaxEmptyBulkDelete() != null) {
            profile.withMaxEmptyBulkDelete(getMaxEmptyBulkDelete());
        }

        if (getMaxGracefulTerminationSec() != null) {
            profile.withMaxGracefulTerminationSec(getMaxGracefulTerminationSec());
        }

        if (getMaxTotalUnreadyPercentage() != null) {
            profile.withMaxTotalUnreadyPercentage(getMaxTotalUnreadyPercentage());
        }

        if (getNewPodScaleUpDelay() != null) {
            profile.withNewPodScaleUpDelay(getNewPodScaleUpDelay());
        }

        if (getOkTotalUnreadyCount() != null) {
            profile.withOkTotalUnreadyCount(getOkTotalUnreadyCount());
        }

        if (getScaleDownDelayAfterAdd() != null) {
            profile.withScaleDownDelayAfterAdd(getScaleDownDelayAfterAdd());
        }

        if (getScaleDownDelayAfterDelete() != null) {
            profile.withScaleDownDelayAfterDelete(getScaleDownDelayAfterDelete());
        }

        if (getScaleDownDelayAfterFailure() != null) {
            profile.withScaleDownDelayAfterFailure(getScaleDownDelayAfterFailure());
        }

        if (getScaleDownUnreadyTime() != null) {
            profile.withScaleDownUnreadyTime(getScaleDownUnreadyTime());
        }

        if (getScaleDownUnneededTime() != null) {
            profile.withScaleDownUnneededTime(getScaleDownUnneededTime());
        }

        if (getScaleDownUtilizationThreshold() != null) {
            profile.withScaleDownUtilizationThreshold(getScaleDownUtilizationThreshold());
        }

        if (getScanInterval() != null) {
            profile.withScanInterval(getScanInterval());
        }

        if (getSkipNodesWithSystemPods() != null) {
            profile.withSkipNodesWithSystemPods(getSkipNodesWithSystemPods());
        }

        if (getSkipNodesWithLocalStorage() != null) {
            profile.withSkipNodesWithLocalStorage(getSkipNodesWithLocalStorage());
        }

        return profile;
    }
}
