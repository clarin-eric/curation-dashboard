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

	public void setParentName(String parentName);

	public String getParentName();

	public String getName();
	
	public boolean isValid();
	
	public void addSegmentScore(Score segmentScore);
	
	public void toXML(OutputStream os) throws Exception;
	
	public void mergeWithParent(R parentReport);
}
