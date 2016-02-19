/**
 * 
 */
package eu.clarin.cmdi.curation.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author dostojic
 *
 */
public class TimeUtils {
    
    public static String humanizeTime(long millis){
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

}
