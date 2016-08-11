package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.report.CollectionReport.Facet;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

/**
 * @author dostojic
 *
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDInstanceReport implements Report<CollectionReport> {
		
	@XmlAttribute
	public Double score = 0.0;	
	
	@XmlAttribute(name = "ins-score")
	public Double instanceScore = 0.0;
	
	@XmlAttribute(name = "pfl-score")
	public Double profileScore = 0.0;
	
	@XmlAttribute(name = "max-score")
	public double maxScore;

	@XmlAttribute
	public Long timeStamp = System.currentTimeMillis();


	// for passing values
	public transient int numOfLinks = 0;
	public transient int numOfResProxiesLinks = 0;
	public transient int numOfUniqueLinks = 0;

	// sub reports **************************************

	// Header
	@XmlElement(name = "profile-section")
	public ProfileHeader header;

	// file
	@XmlElement(name = "file-section")
	public FileReport fileReport;

	// ResProxy
	@XmlElement(name = "resProxy-section")
	public ResProxyReport resProxyReport;

	// XMLValidator
	@XmlElement(name = "xml-validation-section")
	public XMLReport xmlReport;

	// URL
	@XmlElement(name = "url-validation-section")
	public URLReport urlReport;

	// facets
	@XmlElement (name = "facets-section")
	public FacetReport facets;
	
	//scores
	@XmlElementWrapper(name="score-section")
	@XmlElement(name="score")
	public Collection<Score> segmentScores;
	
	@Override
	public String getName() {
		if(fileReport.location.contains(".xml")){
			String normalisedPath = fileReport.location.replace('\\', '/');
			return normalisedPath.substring(normalisedPath.lastIndexOf('/') + 1, normalisedPath.lastIndexOf('.'));
		}
		else
			return fileReport.location;
	}

	@Override
	public void mergeWithParent(CollectionReport parentReport) {

		parentReport.score += score;
		if(score > parentReport.insMaxScore)
			parentReport.insMaxScore = score;
		
		if(score < parentReport.insMinScore)
			parentReport.insMinScore = score;
		
		parentReport.maxScoreInstance = maxScore;

		// ResProxies
		parentReport.resProxyReport.totNumOfResProxies += resProxyReport.numOfResProxies;
			 
		parentReport.resProxyReport.totNumOfResourcesWithMime += resProxyReport.numOfResourcesWithMime;
		parentReport.resProxyReport.totNumOfResProxiesWithReferences += resProxyReport.numOfResProxiesWithReferences;

		// XMLValidator
		parentReport.xmlReport.totNumOfXMLElements += xmlReport.numOfXMLElements;
		parentReport.xmlReport.totNumOfXMLSimpleElements += xmlReport.numOfXMLSimpleElements;
		parentReport.xmlReport.totNumOfXMLEmptyElement += xmlReport.numOfXMLEmptyElement;

		// URL
		parentReport.urlReport.totNumOfLinks += urlReport.numOfLinks;
		parentReport.urlReport.totNumOfUniqueLinks += urlReport.numOfUniqueLinks;
		parentReport.urlReport.totNumOfResProxiesLinks += urlReport.numOfResProxiesLinks;
		parentReport.urlReport.totNumOfBrokenLinks += urlReport.numOfBrokenLinks;

		// Facet		
		facets.facets.forEach(facet -> {
			Facet parFacet = parentReport.facetReport.facet.stream().filter(f -> f.name.equals(facet.name)).findFirst().orElse(null);
			//text is always covered
			if(parFacet.name.equals("text") || facet.values != null)
				parFacet.cnt++;			
		});

		parentReport.handleProfile(header.id, profileScore);

	}

	@Override
	public void toXML(OutputStream os) throws Exception {
		XMLMarshaller<CMDInstanceReport> instanceMarshaller = new XMLMarshaller<>(CMDInstanceReport.class);
		instanceMarshaller.marshal(this, os);
	}

	@Override
	public boolean isValid() {
		return segmentScores.stream().filter(Score::hasFatalMsg).findFirst().orElse(null) == null;
	}
	
	@Override
	public void addSegmentScore(Score segmentScore) {
		if(segmentScores == null)
			segmentScores = new ArrayList<>();
		
		segmentScores.add(segmentScore);
		maxScore += segmentScore.maxScore;
		score += segmentScore.score;
		if(!segmentScore.segment.equals("profiles-score"))
			instanceScore += segmentScore.score;
		
	}


	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class FileReport {
		public String location;
		public long size;

		public FileReport() {};
	}


	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ResProxyReport {
		public int numOfResProxies;
		public int numOfResourcesWithMime;
		public Double percOfResourcesWithMime;
		public int numOfResProxiesWithReferences;
		public Double percOfResProxiesWithReferences;

		@XmlElementWrapper(name = "resourceTypes")
		public Collection<ResourceType> resourceType;

		public ResProxyReport() {}

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ResourceType {
		@XmlAttribute
		public String type;

		@XmlAttribute
		public int count;

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class XMLReport {
		public int numOfXMLElements;
		public int numOfXMLSimpleElements;
		public int numOfXMLEmptyElement;
		public Double percOfPopulatedElements;
		
		public XMLReport() {}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class URLReport {
		public int numOfLinks;
		public int numOfUniqueLinks;
		public int numOfResProxiesLinks;
		public int numOfBrokenLinks;
		public Double percOfValidLinks;

		public URLReport() {}

	}

}
