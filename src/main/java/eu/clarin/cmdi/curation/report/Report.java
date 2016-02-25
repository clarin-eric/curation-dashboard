/**
 * 
 */
package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import eu.clarin.cmdi.curation.main.Config;

/**
 * @author dostojic
 *
 */
public interface Report<R extends Report> {
    
    public void mergeWithParent(R parentReport);
    
    public void marshal(OutputStream os) throws Exception;
    
    public void calculateScore();
    public double getMaxScore();
    
    public default String formatScore(double score, double maxScore){
	NumberFormat formatter = new DecimalFormat(Config.SCORE_NUMERIC_DISPLAY_FORMAT());
	return formatter.format(score) + "/" + formatter.format(maxScore);
    }

}
