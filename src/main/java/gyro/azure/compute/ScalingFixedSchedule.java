package gyro.azure.compute;

import com.microsoft.azure.management.monitor.TimeWindow;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class ScalingFixedSchedule extends Diffable implements Copyable<TimeWindow> {
    private String startTime;
    private String endTime;
    private String timeZone;

    /**
     * The start time of the fixed scaling. (Required)
     */
    @Required
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * The end time of the fixed scaling. (Required)
     */
    @Required
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * The time zone of the fixed scaling. (Required)
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
