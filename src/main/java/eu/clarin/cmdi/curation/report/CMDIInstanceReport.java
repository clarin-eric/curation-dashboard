package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDIInstanceReport implements Report<CollectionReport> {

    // file
    public String path;
    public long size;

    // Header
    public String profile = null;

    // status
    public boolean isValid = true;

    // ResProxy
    public int numOfResProxies;
    public int numOfResWithMime;
    public int numOfLandingPages;
    public int numOfLandingPagesWithoutLink;
    public int numOfResources;
    public int numOfMetadata;

    // XMLValidator
    public int numOfXMLElements;
    public int numOfXMLSimpleElements;
    public int numOfXMLEmptyElement;
    public Double percOfPopulatedElements;

    // URL
    public int numOfLinks;
    public int numOfUniqueLinks;
    public int numOfResProxiesLinks;
    public int numOfBrokenLinks;
    public Double percOfValidLinks;

    // facets
    public FacetReport facets;

    @XmlElementWrapper(name = "details")
    public List<Message> messages = null;

    public void addDetail(Severity lvl, String message) {
	if (lvl.compareTo(Severity.FATAL) == 0)
	    isValid = false;

	if (messages == null)
	    messages = new LinkedList<>();
	messages.add(new Message(lvl, message));
    }

    @Override
    public void mergeWithParent(CollectionReport parentReport) {
	if (!isValid){
	    parentReport.addNewInvalid(path);
	    return;
	}
	    

	parentReport.numOfValidFiles++;

	// ResProxies
	parentReport.totNumOfResProxies += numOfResProxies;
	parentReport.totNumOfResWithMime += numOfResWithMime;
	parentReport.totNumOfLandingPages += numOfLandingPages;
	parentReport.totNumOfLandingPagesWithoutLink += numOfLandingPagesWithoutLink;
	parentReport.totNumOfResources += numOfResources;
	parentReport.totNumOfMetadata += numOfMetadata;

	// XMLValidator
	parentReport.totNumOfXMLElements += numOfXMLElements;
	parentReport.totNumOfXMLSimpleElements += numOfXMLSimpleElements;
	parentReport.totNumOfXMLEmptyElement += numOfXMLEmptyElement;

	// URL
	parentReport.totNumOfLinks += numOfLinks;
	parentReport.totNumOfUniqueLinks += numOfUniqueLinks;
	parentReport.totNumOfResProxiesLinks += numOfResProxiesLinks;
	parentReport.totNumOfBrokenLinks += numOfBrokenLinks;

	// Facet
	if (facets != null && facets.instance != null)
	    parentReport.avgFacetCoverageByInstanceSum += facets.instance.coverage;

	parentReport.handleProfile(profile);

    }

    @Override
    public void marshal(OutputStream os) throws Exception {
	try {

	    JAXBContext jaxbContext = JAXBContext.newInstance(CMDIInstanceReport.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	    // output pretty printed
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	    jaxbMarshaller.marshal(this, os);

	} catch (JAXBException e) {
	    e.printStackTrace();
	}

    }
}
