package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.xml.XMLMarshaller;

@XmlRootElement(name="error-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorReport implements Report<CollectionReport>{

	public String name;	
	public String error;	
	
	public ErrorReport(String name, String error) {
		this.name = name;
		this.error = error;
	}
	
	//for JAXB
	ErrorReport(){}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isValid() {
		return false;
	}
	
	@Override
	public void toXML(OutputStream os) throws Exception {
		XMLMarshaller<ErrorReport> instanceMarshaller = new XMLMarshaller<>(ErrorReport.class);
		instanceMarshaller.marshal(this, os);
		
	}

	@Override
	public void addSegmentScore(Score segmentScore) {
		throw new UnsupportedOperationException();
	}


	/*
	 * when processing collection we are adding the invalid records to the collection list
	 */
	
	@Override
	public void mergeWithParent(CollectionReport parentReport) {
		if(parentReport.invalidFiles == null)
			parentReport.invalidFiles = new ArrayList<>();
		parentReport.invalidFiles.add(name);
	}

}
