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
    
    public double calculateScore();
    
    public double getMaxScore();

}
