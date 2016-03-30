package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.xml.XMLMarshaller;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDProfileReport implements Report<CMDProfileReport> {
	
	public static final double MAX_SCORE = 3;
	
	public transient boolean isValid = true;

	@XmlAttribute(name = "max-score")
	public final double maxScore = MAX_SCORE;

	public Long timeStamp = System.currentTimeMillis();

	public Double score = 0.0;	

	public String ID;
	public String name;
	public String url;
	public String description;
	public boolean isPublic;

	@XmlElement(name = "cmd-components-section")
	Components components;

	@XmlElement(name = "cmd-concepts-section")
	Elements elements;

	@XmlElement(name = "facets-section")
	public FacetReport facet;

	@XmlElementWrapper(name = "details")
	public List<Message> messages = null;

	public void addDetail(Severity lvl, String message) {
		if (messages == null)
			messages = new LinkedList<>();
		messages.add(new Message(lvl, message));
	}

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
	public double getMaxScore() {
		return MAX_SCORE;
	};

	public double calculateScore() {
		
		if(!isValid)
			return score;

		if (isPublic)
			score++;

		score += elements.percWithConcept; // * factor for concepts

		score += facet.profile.coverage; // * factor for facet coverage
		
		return score;
	}
	
	@Override
	public boolean isValid() {
		return isValid;
	}

	public void createComponentsReport(int total, int required, int unique) {
		components = new Components();
		components.total = total;
		components.required = required;
		components.unique = unique;

	}

	public void createElementsReport(int total, int unique, int required, int withConcept,
			Map<String, Integer> datcats, int requiredDatcats) {
		elements = new Elements();
		elements.total = total;
		elements.unique = unique;
		elements.required = required;
		elements.withConcept = withConcept;

		elements.percWithConcept = (double) withConcept / total;

		elements.concepts = new Concepts();

		elements.concepts.total = elements.withConcept;
		elements.concepts.unique = datcats.size();
		elements.concepts.required = requiredDatcats;

		for (Entry<String, Integer> datcat : datcats.entrySet())
			elements.concepts.addConcept(datcat.getKey(), datcat.getValue());
	}

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Components {

		int total;

		int unique;

		int required; // cardinality > 0

		// List<Component> component;

	}

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Component {

		@XmlAttribute
		String name;

		@XmlAttribute
		String id;
	}

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Elements {

		int total;

		int unique;

		int required; // cardinality > 0

		int withConcept;

		Double percWithConcept;

		public Concepts concepts;

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Concepts {

		@XmlAttribute
		int total;

		@XmlAttribute
		int unique;

		@XmlAttribute
		int required;

		public List<Datcat> concept;

		public void addConcept(String url, int count) {

			Datcat d = new Datcat();
			d.url = url;
			d.count = count;

			if (concept == null)
				concept = new LinkedList<>();

			concept.add(d);

		}

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Datcat {
		@XmlAttribute
		String url;

		@XmlAttribute
		int count;
	}

}
