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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDIProfileReport implements Report<Report>{
    
    public String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    
    public String ID;
    public String name;
    public String url;
    public String description;
    public boolean isPublic;
    
    public int numOfComponents;
    public int numOfRequiredComponents; //cardinality > 0
    
    @XmlElementWrapper(name = "components")
    public List<String> component;
    
    public int numOfElements;
    //public int numOfUniqueElements;
    public int numOfElementsWithDatcat;
    public double ratioOfElemenetsWithDatcat;    
    public int numOfRequiredDatCat;
    
    @XmlElementWrapper(name = "datcats")
    public List<String> datcat = null;
    
    @XmlElement(name = "facets-report")
    public FacetReport facet;
    
    @XmlElementWrapper(name = "details")
    public List<Message> messages = null;

    public void addDetail(Severity lvl, String message) {
	if (messages == null)
	    messages = new LinkedList<>();
	messages.add(new Message(lvl, message));
    }
    
    public void addDatcat(String val){
	if (datcat == null)
	    datcat = new LinkedList<>();
	datcat.add(val);
    }


    @Override
    public void mergeWithParent(Report parentReport) {
	//profile should not have a parent
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

}
