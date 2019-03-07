/**
 * 
 */
package eu.clarin.cmdi.curation.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**

 *
 */
public class TimeUtils {
    
    public static String humanizeToTime(long millis){
	if(millis < 1000)
	    return millis + " ms";
	
	if(millis < 1000 * 60)
	    return TimeUnit.MILLISECONDS.toSeconds(millis) + " sec (" + millis + " ms)";
	
	else
	    return String.format("%02d min, %02d sec (%d ms)", 
		    TimeUnit.MILLISECONDS.toMinutes(millis),
		    TimeUnit.MILLISECONDS.toSeconds(millis) - 
		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
		    millis
		);
    }

    public static String humanizeToDate(long millis){
		Instant instant = Instant.ofEpochMilli( millis );
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );

		return zdt.toString();
	}

}
