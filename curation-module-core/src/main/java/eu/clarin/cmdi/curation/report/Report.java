/**
 * 
 */
package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;

/**
 * @author dostojic
 *
 */
public interface Report<R extends Report> {
	
	public String getName();
	
	public boolean isValid();
	
	public void addSegmentScore(Score segmentScore);
	
	public void toXML(OutputStream os) throws Exception;
	
	public void mergeWithParent(R parentReport);
}
