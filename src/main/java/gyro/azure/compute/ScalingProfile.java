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

package gyro.azure.compute;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.resourcemanager.monitor.models.AutoscaleProfile;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class ScalingProfile extends Diffable implements Copyable<AutoscaleProfile> {

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
     * The type of the profile.
     */
    @Required
    @Updatable
    @ValidStrings({ "FIXED", "RECURRENT_SCHEDULE", "FIXED_SCHEDULE", "METRIC" })
    public ProfileType getType() {
        return type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    /**
     * The default instance count for the profile.
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
        } else if (profile.rules() != null && !profile.rules().isEmpty()) {
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

    public enum ProfileType {
        FIXED,
        RECURRENT_SCHEDULE,
        FIXED_SCHEDULE,
        METRIC
    }
}
