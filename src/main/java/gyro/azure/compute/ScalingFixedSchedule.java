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

import com.azure.resourcemanager.monitor.models.TimeWindow;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class ScalingFixedSchedule extends Diffable implements Copyable<TimeWindow> {

    private String startTime;
    private String endTime;
    private String timeZone;

    /**
     * The start time of the fixed scaling.
     */
    @Required
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * The end time of the fixed scaling.
     */
    @Required
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * The time zone of the fixed scaling.
     */
    @Required
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public void copyFrom(TimeWindow timeWindow) {
        setStartTime(timeWindow.start().toString());
        setEndTime(timeWindow.end().toString());
        setTimeZone(timeWindow.timeZone());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s", getStartTime(), getEndTime(), getTimeZone());
    }
}
