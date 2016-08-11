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
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDProfileReport implements Report<CMDProfileReport> {

	@XmlAttribute(name = "score")
	public Double score = 0.0;
	
	@XmlAttribute(name = "max-score")
	public double maxScore;

	@XmlAttribute
	public Long timeStamp = System.currentTimeMillis();
	
	@XmlElement(name = "header-section")
	public ProfileHeader header;

	@XmlElement(name = "cmd-components-section")
	public Components components;

	@XmlElement(name = "cmd-concepts-section")
	public Elements elements;

	@XmlElement(name = "facets-section")
	public FacetReport facet;
	
	@XmlElementWrapper(name="score-section")
	@XmlElement(name="score")
	public Collection<Score> segmentScores;

	@Override
	public void mergeWithParent(CMDProfileReport parentReport) {
		// profile should not have a parent
		throw new UnsupportedOperationException();
	}

	@Override
	public void toXML(OutputStream os) throws Exception {
		XMLMarshaller<CMDProfileReport> instanceMarshaller = new XMLMarshaller<>(CMDProfileReport.class);
		instanceMarshaller.marshal(this, os);
	}
	
	
	@Override
	public String getName() {
		return header.id + ": " + header.name;
	}
	

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Components {

		@XmlAttribute
		public int total;
		
		@XmlAttribute
		public int unique;

		@XmlAttribute
		public int required; // cardinality > 0

		@XmlElement(name="component")
		public Collection<Component> components;

	}

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Component {

		@XmlAttribute
		public String id;
		
		@XmlAttribute
		public String name;
		
		@XmlAttribute
		public int count;
	}

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Elements {

		@XmlAttribute
		public int total;

		@XmlAttribute
		public int required; // cardinality > 0

		@XmlAttribute
		public int withConcept;

		@XmlAttribute
		public Double percWithConcept;

		public Concepts concepts;

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Concepts {

		@XmlAttribute
		public int total;

		@XmlAttribute
		public int unique;

		@XmlAttribute
		public int required;

		@XmlElement(name="concept")
		public Collection<Datcat> concepts;

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Datcat {
		
		@XmlAttribute
		public String ccr;

		@XmlAttribute
		public int count;
	}

	@Override
	public void addSegmentScore(Score segmentScore) {
		if(segmentScores == null)
			segmentScores = new ArrayList<>();
		
		segmentScores.add(segmentScore);
		maxScore += segmentScore.maxScore;
		score += segmentScore.score;
	}

	@Override
	public boolean isValid() {
		return segmentScores.stream().filter(Score::hasFatalMsg).findFirst().orElse(null) == null;
	}

}
