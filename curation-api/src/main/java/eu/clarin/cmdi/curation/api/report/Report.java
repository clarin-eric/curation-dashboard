/**
 * 
 */
package eu.clarin.cmdi.curation.api.report;

import java.io.OutputStream;

import eu.clarin.cmdi.curation.api.xml.XMLMarshaller;

/**

 *
 */
public abstract class Report<R extends Report<?>> {


	public abstract String getName();
	
	public abstract boolean isValid();
	
	public abstract void addSegmentScore(Score segmentScore);
	
	public abstract void addReport(R parentReport);
	
   public void toXML(OutputStream os) {
      XMLMarshaller<?> instanceMarshaller = new XMLMarshaller<>(this.getClass());
      instanceMarshaller.marshal(this, os);
   };
}
