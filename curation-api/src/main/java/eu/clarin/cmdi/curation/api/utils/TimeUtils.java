/**
 *
 */
package eu.clarin.cmdi.curation.api.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * The type Time utils.
 */
public class TimeUtils {

    /**
     * Humanize to time string.
     *
     * @param millis the millis
     * @return the string
     */
    public static String humanizeToTime(long millis) {
        if (millis < 1000)
            return millis + " ms";

        if (millis < 1000 * 60)
            return TimeUnit.MILLISECONDS.toSeconds(millis) + " sec (" + millis + " ms)";

        else
            return String.format("%02d min, %02d sec (%d ms)",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
                    millis
            );
    }

    /**
     * Humanize to date string.
     *
     * @param millis the millis
     * @return the string
     */
    public static String humanizeToDate(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);

        String timestamp = zdt.toString();
        timestamp = timestamp.replaceFirst("\\[", " [");
        timestamp = timestamp.replaceFirst("T", " ");
        return timestamp;
    }

}
