package eu.clarin.cmdi.curation.report;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Concept{
	
	@XmlAttribute public String	uri;
	@XmlAttribute public String prefLabel;
	@XmlAttribute public String status;		
	
	@XmlAttribute public Integer count;
	
	public Concept(){}
	
	public Concept(String uri, String prefLabel, String status) {
		this.uri = uri;
		this.prefLabel = prefLabel;
		this.status = status;
		this.count = 1;
	}
			
}
