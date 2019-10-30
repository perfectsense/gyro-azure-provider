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

import com.microsoft.azure.management.monitor.ComparisonOperationType;
import com.microsoft.azure.management.monitor.MetricStatisticType;
import com.microsoft.azure.management.monitor.ScaleDirection;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.monitor.ScaleType;
import com.microsoft.azure.management.monitor.TimeAggregationType;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import org.joda.time.Period;

public class ScalingRule extends Diffable implements Copyable<ScaleRule> {
    private String metricName;
    private String metricSourceId;
    private MetricStatisticType statisticType;
    private Integer statisticDuration;
    private Integer statisticFrequency;
    private TimeAggregationType timeAggregation;
    private ComparisonOperationType comparisonOperation;
    private Double threshold;
    private ScaleDirection scaleDirection;
    private ScaleType scaleType;
    private Integer cooldown;
    private Integer instanceCountChange;

    /**
     * The name of the Metric. (Required)
     */
    @Required
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * The source resource monitored by the Rule. (Required)
     */
    public String getMetricSourceId() {
        return metricSourceId;
    }

    public void setMetricSourceId(String metricSourceId) {
        this.metricSourceId = metricSourceId;
    }

    /**
     * The type of metrics statistic showing how metrics from multiple instances are combined. Valid Values are ``AVERAGE`` or ``MIN`` or ``MAX`` or ``SUM``. Defaults to ``AVERAGE``.
     */
    @Updatable
    public MetricStatisticType getStatisticType() {
        if (statisticType == null) {
            statisticType = MetricStatisticType.AVERAGE;
        }

        return statisticType;
    }

    public void setStatisticType(MetricStatisticType statisticType) {
        this.statisticType = statisticType;
    }

    /**
     * The range of time in which instance data is collected in seconds. Valid values are between ``300`` and ``43200``. Defaults ro ``600``.
     */
    @Range(min = 300, max = 43200)
    @Updatable
    public Integer getStatisticDuration() {
        if (statisticDuration == null) {
            statisticDuration = 600;
        }

        return statisticDuration;
    }

    public void setStatisticDuration(Integer statisticDuration) {
        this.statisticDuration = statisticDuration;
    }

    /**
     * The granularity at which the instances are monitored in seconds. Valid values are between ``60`` and ``43200``. Defaults ro ``600``. Defaults to ``60``.
     */
    @Range(min = 60, max = 43200)
    @Updatable
    public Integer getStatisticFrequency() {
        if (statisticFrequency == null) {
            statisticFrequency = 60;
        }

        return statisticFrequency;
    }

    public void setStatisticFrequency(Integer statisticFrequency) {
        this.statisticFrequency = statisticFrequency;
    }

    /**
     * The way in which the data collected over time is combined. Valid values are ``AVERAGE`` or ``MINIMUM`` or ``MAXIMUM`` or ``TOTAL`` or ``COUNT``. (Required)
     */
    @Required
    @Updatable
    public TimeAggregationType getTimeAggregation() {
        return timeAggregation;
    }

    public void setTimeAggregation(TimeAggregationType timeAggregation) {
        this.timeAggregation = timeAggregation;
    }

    /**
     * The comparison operator to compare the metric data and the threshold. Valid values are ``EQUALS`` or ``NOT_EQUALS`` or ``GREATER_THAN`` or ``GREATER_THAN_OR_EQUAL`` or ``LESS_THAN`` or ``LESS_THAN_OR_EQUAL``. (Required)
     */
    @Required
    @Updatable
    public ComparisonOperationType getComparisonOperation() {
        return comparisonOperation;
    }

    public void setComparisonOperation(ComparisonOperationType comparisonOperation) {
        this.comparisonOperation = comparisonOperation;
    }

    /**
     * The threshold that triggers the action. (Required)
     */
    @Required
    @Updatable
    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    /**
     * The direction of scaling, to increase or decrease. Valid values are ``NONE`` or ``INCREASE`` or ``DECREASE``. (Required)
     */
    @Required
    @Updatable
    public ScaleDirection getScaleDirection() {
        return scaleDirection;
    }

    public void setScaleDirection(ScaleDirection scaleDirection) {
        this.scaleDirection = scaleDirection;
    }

    /**
     * The type of action when the Rule fires. Valid values are ``CHANGE_COUNT`` or ``PERCENT_CHANGE_COUNT`` or ``EXACT_COUNT``. (Required)
     */
    @Required
    @Updatable
    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    /**
     * The amount of time to wait since last scaling action in minutes. Valid values are between ``1`` and ``10080``. (Required)
     */
    @Required
    @Updatable
    @Range(min = 1, max = 10080)
    public Integer getCooldown() {
        return cooldown;
    }

    public void setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * The number of instances involved in the scaling action. (Required)
     */
    @Required
    @Updatable
    public Integer getInstanceCountChange() {
        return instanceCountChange;
    }

    public void setInstanceCountChange(Integer instanceCountChange) {
        this.instanceCountChange = instanceCountChange;
    }

    @Override
    public void copyFrom(ScaleRule rule) {
        setComparisonOperation(rule.condition());
        setCooldown(rule.coolDown().toStandardMinutes().getMinutes());
        setStatisticDuration(rule.duration().toStandardSeconds().getSeconds());
        setStatisticFrequency(rule.frequency().toStandardSeconds().getSeconds());
        setStatisticType(rule.frequencyStatistic());
        setMetricName(rule.metricName());
        setMetricSourceId(rule.metricSource());
        setScaleDirection(rule.scaleDirection());
        setInstanceCountChange(rule.scaleInstanceCount());
        setScaleType(rule.scaleType());
        setThreshold(rule.threshold());
        setTimeAggregation(rule.timeAggregation());
    }

    ScaleRule.DefinitionStages.WithAttach attachRule(ScaleRule.DefinitionStages.Blank withBlank) {
        return withBlank.withMetricSource(getMetricSourceId())
            .withMetricName(getMetricName())
            .withStatistic(Period.seconds(getStatisticDuration()), Period.seconds(getStatisticFrequency()), getStatisticType())
            .withCondition(getTimeAggregation(), getComparisonOperation(), getThreshold())
            .withScaleAction(getScaleDirection(), getScaleType(), getInstanceCountChange(), Period.minutes(getCooldown()));
    }

    ScaleRule.ParentUpdateDefinitionStages.WithAttach attachRule(ScaleRule.ParentUpdateDefinitionStages.Blank withBlank) {
        return withBlank.withMetricSource(getMetricSourceId())
            .withMetricName(getMetricName())
            .withStatistic(Period.seconds(getStatisticDuration()), Period.seconds(getStatisticFrequency()), getStatisticType())
            .withCondition(getTimeAggregation(), getComparisonOperation(), getThreshold())
            .withScaleAction(getScaleDirection(), getScaleType(), getInstanceCountChange(), Period.minutes(getCooldown()));
    }

    @Override
    public String primaryKey() {
        String format = String.format("%s %s %s %s %s %s %s %s %s", getStatisticType(), getStatisticDuration(),
            getStatisticFrequency(), getTimeAggregation(), getComparisonOperation(),
            getThreshold(), getScaleType(), getCooldown(), getInstanceCountChange());
        return String.format("Rule (%s with direction %s for target %s) %s", getMetricName(), getScaleDirection(), getMetricSourceId(), format.hashCode());
    }
}
