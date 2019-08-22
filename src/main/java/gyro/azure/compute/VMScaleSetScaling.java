package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a scale set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::scale-set-scaling scale-set-scaling-example
 *         name: "scale-set-scaling-example"
 *         resource-group: $(azure::resource-group scale-set-resource-group-example)
 *         enabled: "false"
 *         scale-set: $(azure::scale-set scale-set-example)
 *
 *         profile
 *             name: "profile-example-fixed-schedule"
 *             type: "FIXED_SCHEDULE"
 *             default-instance-count: 2
 *             fixed-schedule
 *                 start-time: "2019-08-23T00:00:00.000Z"
 *                 end-time: "2019-08-23T23:59:00.000Z"
 *                 time-zone: "Eastern Standard Time"
 *             end
 *         end
 *
 *         profile
 *             name: "profile-example-recurrent-schedule"
 *             type: "RECURRENT_SCHEDULE"
 *             default-instance-count: 1
 *             recurrent-schedule
 *                 time-zone: "Eastern Standard Time"
 *                 start-time: "12:23"
 *                 day-of-weeks: [
 *                     "MONDAY", "TUESDAY", "WEDNESDAY"
 *                 ]
 *             end
 *         end
 *
 *         profile
 *             name: "profile-example-metric"
 *             type: "METRIC"
 *             default-instance-count: 1
 *             max-instance-count: 1
 *             min-instance-count: 1
 *             rule
 *                 metric-name: "Percentage CPU"
 *                 metric-source-id: $(azure::scale-set scale-set-example).id
 *                 statistic-duration: 1200
 *                 statistic-frequency: 60
 *                 statistic-type: "AVERAGE"
 *                 time-aggregation: "AVERAGE"
 *                 comparison-operation: "GREATER_THAN"
 *                 threshold: 70
 *                 scale-direction: "INCREASE"
 *                 scale-type: "CHANGE_COUNT"
 *                 instance-count-change: 1
 *                 cooldown: 5
 *             end
 *
 *             rule
 *                 metric-name: "Outbound Flows"
 *                 metric-source-id: $(azure::scale-set scale-set-example).id
 *                 statistic-duration: 1200
 *                 statistic-frequency: 60
 *                 statistic-type: "AVERAGE"
 *                 time-aggregation: "AVERAGE"
 *                 comparison-operation: "GREATER_THAN"
 *                 threshold: 70
 *                 scale-direction: "INCREASE"
 *                 scale-type: "CHANGE_COUNT"
 *                 instance-count-change: 1
 *                 cooldown: 5
 *             end
 *         end
 *     end
 */
@Type("scale-set-scaling")
public class VMScaleSetScaling extends AzureResource implements Copyable<AutoscaleSetting> {

    private String name;
    private ResourceGroupResource resourceGroup;
    private VMScaleSetResource scaleSet;
    private Set<ScalingProfile> profile;
    private Boolean adminEmailNotificationEnabled;
    private Boolean enabled;
    private Boolean coAdminEmailNotificationEnabled;
    private Set<String> customEmailsNotifications;
    private String webhookNotification;
    private Map<String, String> tags;
    private String id;

    /**
     * The name of the Scaling. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Resource Group under which the Scaling would reside. (Required)
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The Scale Set Scaling would target. (Required)
     */
    @Required
    public VMScaleSetResource getScaleSet() {
        return scaleSet;
    }

    public void setScaleSet(VMScaleSetResource scaleSet) {
        this.scaleSet = scaleSet;
    }

    /**
     * The set of profiles for the Scaling. (Required)
     */
    @Required
    @Updatable
    public Set<ScalingProfile> getProfile() {
        if (profile == null) {
            profile = new HashSet<>();
        }

        return profile;
    }

    public void setProfile(Set<ScalingProfile> profile) {
        this.profile = profile;
    }

    /**
     * Enable admin email notification. Defaults to ``false``.
     */
    @Updatable
    public Boolean getAdminEmailNotificationEnabled() {
        if (adminEmailNotificationEnabled == null) {
            adminEmailNotificationEnabled = false;
        }

        return adminEmailNotificationEnabled;
    }

    public void setAdminEmailNotificationEnabled(Boolean adminEmailNotificationEnabled) {
        this.adminEmailNotificationEnabled = adminEmailNotificationEnabled;
    }

    /**
     * Enable Scaling. Defaults to ``true``.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Enable co admin email notification. Defaults to ``false``.
     */
    @Updatable
    public Boolean getCoAdminEmailNotificationEnabled() {
        if (coAdminEmailNotificationEnabled == null) {
            coAdminEmailNotificationEnabled = false;
        }

        return coAdminEmailNotificationEnabled;
    }

    public void setCoAdminEmailNotificationEnabled(Boolean coAdminEmailNotificationEnabled) {
        this.coAdminEmailNotificationEnabled = coAdminEmailNotificationEnabled;
    }

    /**
     * A set of custom emails to send notification to.
     */
    @Updatable
    public Set<String> getCustomEmailsNotifications() {
        if (customEmailsNotifications == null) {
            customEmailsNotifications = new HashSet<>();
        }

        return customEmailsNotifications;
    }

    public void setCustomEmailsNotifications(Set<String> customEmailsNotifications) {
        this.customEmailsNotifications = customEmailsNotifications;
    }

    /**
     * Set the service address to receive the notification.
     */
    @Updatable
    public String getWebhookNotification() {
        return webhookNotification;
    }

    public void setWebhookNotification(String webhookNotification) {
        this.webhookNotification = webhookNotification;
    }

    /**
     * A set of tags for the Scaling.
     */
    @Updatable
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
     * The ID of the scaling.
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
    public void copyFrom(AutoscaleSetting autoscaleSetting) {
        setAdminEmailNotificationEnabled(autoscaleSetting.adminEmailNotificationEnabled());
        setEnabled(autoscaleSetting.autoscaleEnabled());
        setCoAdminEmailNotificationEnabled(autoscaleSetting.coAdminEmailNotificationEnabled());
        setCustomEmailsNotifications(new HashSet<>(autoscaleSetting.customEmailsNotification()));
        setScaleSet(findById(VMScaleSetResource.class, autoscaleSetting.targetResourceId()));
        setWebhookNotification(autoscaleSetting.webhookNotification());
        setId(autoscaleSetting.id());
        setName(autoscaleSetting.name());
        setResourceGroup(findById(ResourceGroupResource.class, autoscaleSetting.resourceGroupName()));
        setTags(autoscaleSetting.tags());
        setProfile(autoscaleSetting.profiles().values().stream().map(o -> {
            ScalingProfile profile = newSubresource(ScalingProfile.class);
            profile.copyFrom(o);
            return profile;
        }).collect(Collectors.toSet()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        AutoscaleSetting autoscaleSetting = client.autoscaleSettings().getById(getId());

        if (autoscaleSetting == null) {
            return false;
        }

        copyFrom(autoscaleSetting);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        AutoscaleSetting.DefinitionStages.DefineAutoscaleSettingResourceProfiles basicStage = client.autoscaleSettings()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName())
            .withTargetResource(getScaleSet().getId());

        AutoscaleSetting.DefinitionStages.WithCreate finalStage = null;

        for (ScalingProfile profile : getProfile()) {
            AutoscaleProfile.DefinitionStages.Blank blankProfileStage = null;
            if (finalStage == null) {
                blankProfileStage  = basicStage.defineAutoscaleProfile(profile.getName());
            } else {
                blankProfileStage = finalStage.defineAutoscaleProfile(profile.getName());
            }

            if (profile.getType().equals(ScalingProfile.ProfileType.FIXED)) {
                finalStage = blankProfileStage.withFixedInstanceCount(profile.getDefaultInstanceCount()).attach();
            } else if (profile.getType().equals(ScalingProfile.ProfileType.FIXED_SCHEDULE)) {
                finalStage = blankProfileStage.withScheduleBasedScale(profile.getDefaultInstanceCount())
                    .withFixedDateSchedule(profile.getFixedSchedule().getTimeZone(), new DateTime(profile.getFixedSchedule().getStartTime()), new DateTime(profile.getFixedSchedule().getEndTime()))
                    .attach();
            } else if (profile.getType().equals(ScalingProfile.ProfileType.RECURRENT_SCHEDULE)) {
                finalStage = blankProfileStage.withScheduleBasedScale(profile.getDefaultInstanceCount())
                    .withRecurrentSchedule(profile.getRecurrentSchedule().getTimeZone(), profile.getRecurrentSchedule().getStartTime(), profile.getRecurrentSchedule().toDayOfWeeks())
                    .attach();
            } else {
                AutoscaleProfile.DefinitionStages.WithScaleRule withScaleRule = blankProfileStage.withMetricBasedScale(profile.getMinInstanceCount(), profile.getMaxInstanceCount(), profile.getDefaultInstanceCount());
                AutoscaleProfile.DefinitionStages.WithScaleRuleOptional scaleRuleStage = null;
                for (ScalingRule rule : profile.getRule()) {
                    ScaleRule.DefinitionStages.Blank blank;
                    if (scaleRuleStage == null) {
                        blank = withScaleRule.defineScaleRule();
                    } else {
                        blank = scaleRuleStage.defineScaleRule();
                    }
                    scaleRuleStage = rule.attachRule(blank).attach();
                }

                finalStage = scaleRuleStage.attach();
            }
        }

        if (getAdminEmailNotificationEnabled()) {
            finalStage = finalStage.withAdminEmailNotification();
        }

        if (!getEnabled()) {
            finalStage = finalStage.withAutoscaleDisabled();
        }

        if (getCoAdminEmailNotificationEnabled()) {
            finalStage = finalStage.withCoAdminEmailNotification();
        }

        if (!ObjectUtils.isBlank(getWebhookNotification())) {
            finalStage = finalStage.withWebhookNotification(getWebhookNotification());
        }

        if (!getCustomEmailsNotifications().isEmpty()) {
            finalStage = finalStage.withCustomEmailsNotification(getCustomEmailsNotifications().toArray(new String[0]));
        }

        AutoscaleSetting autoscaleSetting = finalStage.create();

        setId(autoscaleSetting.id());

        state.save();

        if (!getTags().isEmpty()) {
            autoscaleSetting.update().withTags(getTags()).apply();
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Azure client = createClient();

        AutoscaleSetting autoscaleSetting = client.autoscaleSettings().getById(getId());

        AutoscaleSetting.Update update;

        if (getAdminEmailNotificationEnabled()) {
            update = autoscaleSetting.update().withAdminEmailNotification();
        } else {
            update = autoscaleSetting.update().withoutAdminEmailNotification();
        }

        if (!getEnabled()) {
            update = update.withAutoscaleDisabled();
        } else {
            update = update.withAutoscaleEnabled();
        }

        if (getCoAdminEmailNotificationEnabled()) {
            update = update.withCoAdminEmailNotification();
        } else {
            update = update.withoutCoAdminEmailNotification();
        }

        if (!ObjectUtils.isBlank(getWebhookNotification())) {
            update = update.withWebhookNotification(getWebhookNotification());
        } else {
            update = update.withoutWebhookNotification();
        }

        if (!getCustomEmailsNotifications().isEmpty()) {
            update = update.withCustomEmailsNotification(getCustomEmailsNotifications().toArray(new String[0]));
        } else {
            update = update.withoutCustomEmailsNotification();
        }

        if (changedFieldNames.contains("profile")) {
            for (ScalingProfile profile : ((VMScaleSetScaling) current).getProfile()) {
                if (getProfile().stream().noneMatch(o -> o.getName().equals(profile.getName()) && o.getType().equals(profile.getType()) && o.getType().equals(ScalingProfile.ProfileType.FIXED))) {
                    update = update.withoutAutoscaleProfile(profile.getName());
                }
            }

            for (ScalingProfile profile : getProfile()) {
                if (profile.getType().equals(ScalingProfile.ProfileType.FIXED)) {
                    continue;
                }

                AutoscaleProfile.UpdateDefinitionStages.Blank profileStage = update.defineAutoscaleProfile(profile.getName());
                if (profile.getType().equals(ScalingProfile.ProfileType.FIXED_SCHEDULE)) {
                    update = profileStage
                        .withScheduleBasedScale(profile.getDefaultInstanceCount())
                        .withFixedDateSchedule(profile.getFixedSchedule().getTimeZone(), new DateTime(profile.getFixedSchedule().getStartTime()), new DateTime(profile.getFixedSchedule().getEndTime()))
                        .attach();
                } else if (profile.getType().equals(ScalingProfile.ProfileType.RECURRENT_SCHEDULE)) {
                    update = profileStage
                        .withScheduleBasedScale(profile.getDefaultInstanceCount())
                        .withRecurrentSchedule(profile.getRecurrentSchedule().getTimeZone(), profile.getRecurrentSchedule().getStartTime(), profile.getRecurrentSchedule().toDayOfWeeks())
                        .attach();
                } else {
                    AutoscaleProfile.UpdateDefinitionStages.WithScaleRule withScaleRule = profileStage.withMetricBasedScale(profile.getMinInstanceCount(), profile.getMaxInstanceCount(), profile.getDefaultInstanceCount());
                    AutoscaleProfile.UpdateDefinitionStages.WithScaleRuleOptional scaleRuleStage = null;
                    for (ScalingRule rule : profile.getRule()) {
                        if (scaleRuleStage == null) {
                            scaleRuleStage = rule.attachRule(withScaleRule.defineScaleRule()).attach();
                        } else {
                            scaleRuleStage = rule.attachRule(scaleRuleStage.defineScaleRule()).attach();
                        }
                    }

                    update = scaleRuleStage.attach();
                }
            }
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.autoscaleSettings().deleteById(getId());
    }
}
