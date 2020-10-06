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

import com.microsoft.azure.management.monitor.DayOfWeek;
import com.microsoft.azure.management.monitor.Recurrence;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.Set;
import java.util.stream.Collectors;

public class ScalingRecurrentSchedule extends Diffable implements Copyable<Recurrence> {
    private String timeZone;
    private String startTime;
    private Set<String> dayOfWeeks;

    /**
     * The time zone of the recurrent policy.
     */
    @Required
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * The start time of the recurrent policy.
     */
    @Required
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * The set of weekdays for the recurrent policy. Valid values are ``MONDAY`` or ``TUESDAY`` or ``WEDNESDAY`` or ``THURSDAY`` or ``FRIDAY`` or ``SATURDAY`` or ``SUNDAY``.
     */
    @Required
    @Updatable
    @ValidStrings({"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
    public Set<String> getDayOfWeeks() {
        return dayOfWeeks;
    }

    public void setDayOfWeeks(Set<String> dayOfWeeks) {
        this.dayOfWeeks = dayOfWeeks;
    }

    @Override
    public void copyFrom(Recurrence recurrence) {
        setTimeZone(recurrence.schedule().timeZone());
        setDayOfWeeks(recurrence.schedule().days().stream().map(String::toUpperCase).collect(Collectors.toSet()));
        String hours = recurrence.schedule().hours().get(0).toString();
        String minutes = recurrence.schedule().minutes().get(0).toString();
        setStartTime((hours.length() == 1 ? "0" + hours : hours) + ":" + (minutes.length() == 1 ? "0" + minutes : minutes));
    }

    DayOfWeek[] toDayOfWeeks() {
        return getDayOfWeeks().stream().map(DayOfWeek::valueOf).toArray(DayOfWeek[]::new);
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getStartTime(), getTimeZone());
    }
}
