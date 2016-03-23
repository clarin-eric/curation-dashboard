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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.xml.ScoreAdapter;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDIProfileReport implements Report<Report> {
	
	public static final double MAX_SCORE = 3;

	@XmlAttribute(name = "max-score")
	public final double maxScore = MAX_SCORE;

	public String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

	@XmlJavaTypeAdapter(ScoreAdapter.class)
	public Double score;

	public String ID;
	public String name;
	public String url;
	public String description;
	public boolean isPublic;

	@XmlElement(name = "CMDI-Components")
	Components components;

	@XmlElement(name = "CMDI-Elements")
	Elements elements;

	@XmlElement(name = "facets-report")
	public FacetReport facet;

	@XmlElementWrapper(name = "details")
	public List<Message> messages = null;

	public void addDetail(Severity lvl, String message) {
		if (messages == null)
			messages = new LinkedList<>();
		messages.add(new Message(lvl, message));
	}

	@Override
	public void mergeWithParent(Report parentReport) {
		// profile should not have a parent
		throw new UnsupportedOperationException();

	}

	@Override
	public void marshal(OutputStream os) throws Exception {
		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(CMDIProfileReport.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, os);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	@Override
	public double getMaxScore() {
		return MAX_SCORE;
	};

	public double calculateScore() {
		score = 0.0;

		if (isPublic)
			score++;

		score += elements.percWithDatacategory; // * factor for datcats

		score += facet.profile.coverage; // * factor for facet coverage
		
		return score;
	}

	public void createComponentsReport(int total, int required, int unique) {
		components = new Components();
		components.total = total;
		components.required = required;
		components.unique = unique;

	}

	public void createElementsReport(int total, int unique, int required, int withDatacategory,
			Map<String, Integer> datcats, int requiredDatcats) {
		elements = new Elements();
		elements.total = total;
		elements.unique = unique;
		elements.required = required;
		elements.withDatacategory = withDatacategory;

		elements.percWithDatacategory = (double) withDatacategory / total;

		elements.datacategories = new Datacategories();

		elements.datacategories.total = elements.withDatacategory;
		elements.datacategories.unique = datcats.size();
		elements.datacategories.required = requiredDatcats;

		for (Entry<String, Integer> datcat : datcats.entrySet())
			elements.datacategories.addDatcat(datcat.getKey(), datcat.getValue());
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

		int withDatacategory;

		double percWithDatacategory;

		public Datacategories datacategories;

	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Datacategories {

		@XmlAttribute
		int total;

		@XmlAttribute
		int unique;

		@XmlAttribute
		int required;

		public List<Datcat> datcat;

		public void addDatcat(String url, int count) {

			Datcat d = new Datcat();
			d.url = url;
			d.count = count;

			if (datcat == null)
				datcat = new LinkedList<>();

			datcat.add(d);

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
