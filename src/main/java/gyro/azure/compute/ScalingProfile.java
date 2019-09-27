package gyro.azure.compute;

import com.microsoft.azure.management.monitor.AutoscaleProfile;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ScalingProfile extends Diffable implements Copyable<AutoscaleProfile> {
    public enum ProfileType {FIXED, RECURRENT_SCHEDULE, FIXED_SCHEDULE, METRIC}
    private String name;
    private ProfileType type;
    private Integer defaultInstanceCount;
    private Integer maxInstanceCount;
    private Integer minInstanceCount;
    private ScalingRecurrentSchedule recurrentSchedule;
    private ScalingFixedSchedule fixedSchedule;
    private Set<ScalingRule> rule;

    /**
     * The name of the profile.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the profile. Valid values are ``FIXED`` or ``RECURRENT_SCHEDULE`` or ``FIXED_SCHEDULE`` or ``METRIC``. (Required)
     */
    @Required
    @Updatable
    public ProfileType getType() {
        return type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    /**
     * The default instance count for the profile. (Required)
     */
    @Required
    @Updatable
    public Integer getDefaultInstanceCount() {
        return defaultInstanceCount;
    }

    public void setDefaultInstanceCount(Integer defaultInstanceCount) {
        this.defaultInstanceCount = defaultInstanceCount;
    }

    /**
     * The max instance count for the profile. Required when 'profile-type' is set to ``METRIC``.
     */
    @Updatable
    public Integer getMaxInstanceCount() {
        return maxInstanceCount;
    }

    public void setMaxInstanceCount(Integer maxInstanceCount) {
        this.maxInstanceCount = maxInstanceCount;
    }

    /**
     * The min instance count for the profile. Required when 'profile-type' is set to ``METRIC``.
     */
    @Updatable
    public Integer getMinInstanceCount() {
        return minInstanceCount;
    }

    public void setMinInstanceCount(Integer minInstanceCount) {
        this.minInstanceCount = minInstanceCount;
    }

    /**
     * The recurrent schedule configuration for the profile. Required when 'profile-type' is set to ``RECURRENT_SCHEDULE``.
     *
     * @subresource gyro.azure.compute.ScalingRecurrentSchedule
     */
    @Updatable
    public ScalingRecurrentSchedule getRecurrentSchedule() {
        return recurrentSchedule;
    }

    public void setRecurrentSchedule(ScalingRecurrentSchedule recurrentSchedule) {
        this.recurrentSchedule = recurrentSchedule;
    }

    /**
     * The fixed schedule configuration for the profile. Required when 'profile-type' is set to ``FIXED_SCHEDULE``.
     *
     * @subresource gyro.azure.compute.ScalingFixedSchedule
     */
    @Updatable
    public ScalingFixedSchedule getFixedSchedule() {
        return fixedSchedule;
    }

    public void setFixedSchedule(ScalingFixedSchedule fixedSchedule) {
        this.fixedSchedule = fixedSchedule;
    }

    /**
     * The set of scaling schedule configuration for the profile. Required when 'profile-type' is set to ``METRIC``.
     *
     * @subresource gyro.azure.compute.ScalingRule
     */
    @Updatable
    public Set<ScalingRule> getRule() {
        if (rule == null) {
            rule = new HashSet<>();
        }

        return rule;
    }

    public void setRule(Set<ScalingRule> rule) {
        this.rule = rule;
    }

    @Override
    public void copyFrom(AutoscaleProfile profile) {
        setDefaultInstanceCount(profile.defaultInstanceCount());
        setName(profile.name());
        setMaxInstanceCount(profile.maxInstanceCount());
        setMinInstanceCount(profile.minInstanceCount());
        if (profile.fixedDateSchedule() != null) {
            setType(ProfileType.FIXED_SCHEDULE);
            ScalingFixedSchedule schedule = newSubresource(ScalingFixedSchedule.class);
            schedule.copyFrom(profile.fixedDateSchedule());
            setFixedSchedule(schedule);
        } else if (profile.recurrentSchedule() != null) {
            setType(ProfileType.RECURRENT_SCHEDULE);
            ScalingRecurrentSchedule schedule = newSubresource(ScalingRecurrentSchedule.class);
            schedule.copyFrom(profile.recurrentSchedule());
            setRecurrentSchedule(schedule);
        } else if (profile.rules() != null && !profile.rules().isEmpty()){
            setType(ProfileType.METRIC);
            setRule(profile.rules().stream().map(o -> {
                ScalingRule rule = newSubresource(ScalingRule.class);
                rule.copyFrom(o);
                return rule;
            }).collect(Collectors.toSet()));
        } else {
            setType(ProfileType.FIXED);
        }
    }

    @Override
    public String primaryKey() {
        return getName();
    }
}
