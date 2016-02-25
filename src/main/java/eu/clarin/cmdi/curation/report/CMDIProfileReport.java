/**
 * 
 */
package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDIProfileReport implements Report<Report> {

    static final double MAX_SCORE = 3;
    
    public String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());    
    
    @XmlElement(name = "score")
    public String overallScore;

    public String ID;
    public String name;
    public String url;
    public String description;
    public boolean isPublic;

    Components components;
    
    public int numOfElements;
    public double ratioOfElemenetsWithDatcat;
    
    Datacategories datacategories;
    

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
    
    public void calculateScore(){
	double score = 0;
	
	if(isPublic)
	    score++;
	
	score += ratioOfElemenetsWithDatcat; //* factor for datcats
	
	score += facet.profile.coverage; // * factor for facet coverage
	
	overallScore = formatScore(score, MAX_SCORE);
    }
    
    public void addComponent(String componentName, String componentId, boolean required){
	if(components == null){
	    components = new Components();
	    components.component = new LinkedList<>();
	}
	
	components.total++;
	
	if(required)
	    components.required++;
	
	Component c = new Component();
	c.name = componentName;
	c.id = componentId;
	c.required = required;
	
	components.component.add(c);
	
    }
    
    public void addDataCategory(String name, boolean required){
	if(datacategories == null){
	    datacategories = new Datacategories();
	    datacategories.datcat = new LinkedList<>();
	}
	
	datacategories.total++;	
	if(required)
	    datacategories.required++;
	
	for(Datcat d: datacategories.datcat)
	    if(d.name.equals(name)){
		d.count++;
		if(required)
		    d.required = true;
		return;
	    }
	//not in the list
	
	datacategories.unique++;
	
	Datcat d = new Datcat();
	d.name = name;
	d.count = 1;
	if(required)
	    d.required = true;
	datacategories.datcat.add(d);
    }
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Datacategories {
	
	@XmlAttribute
	int total;
	
	@XmlAttribute
	int unique;
	
	@XmlAttribute
	int required;
	
	public List<Datcat> datcat;	
	
    }
    

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Datcat {
	@XmlAttribute
	String name;

	@XmlAttribute
	int count;
	
	@XmlAttribute
	boolean required;

    }

    @XmlRootElement()
    @XmlAccessorType(XmlAccessType.FIELD)
    static class Components {
	@XmlAttribute
	int total;
	
	@XmlAttribute
	int required; // cardinality > 0
	
	List<Component> component;

    }
    
    @XmlRootElement()
    @XmlAccessorType(XmlAccessType.FIELD)
    static class Component{
	
	@XmlAttribute
	String name;
	
	@XmlAttribute
	String id;
	
	@XmlAttribute
	boolean required;
	
	
    }

}
