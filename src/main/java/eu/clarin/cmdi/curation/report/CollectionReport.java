package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */

@XmlRootElement(name = "collection-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionReport implements Report<CollectionReport> {

    public String provider;

    public long numOfFiles = 0;
    public long size;

    public long maxFileSize;
    public long minFileSize;

    public long numOfValidFiles;
    public Double percOfValidFiles;

    @XmlElementWrapper(name = "duplicatedMDSelfLinks")
    public List<String> duplicatedMDSelfLink = null;

    // ResProxies
    public int totNumOfResProxies;
    public Double avgNumOfResProxies;
    public int totNumOfResWithMime;
    public Double avgNumOfResWithMime;
    public int totNumOfLandingPages;
    public Double avgNumOfLandingPages;
    public int totNumOfLandingPagesWithoutLink;
    public Double avgNumOfLandingPagesWithoutLink;
    public int totNumOfResources;
    public Double avgNumOfResources;
    public int totNumOfMetadata;
    public Double avgNumOfMetadata;

    // XMLValidator
    public int totNumOfXMLElements;
    public Double avgNumOfXMLElements;
    public int totNumOfXMLSimpleElements;
    public Double avgNumOfXMLSimpleElements;
    public int totNumOfXMLEmptyElement;
    public Double avgXMLEmptyElement;
    public Double avgRateOfPopulatedElements;

    // URL
    public int totNumOfLinks;
    public Double avgNumOfLinks;
    public int totNumOfUniqueLinks;
    public Double avgNumOfUniqueLinks;
    public int totNumOfResProxiesLinks;
    public Double avgNumOfResProxiesLinks;
    public int totNumOfBrokenLinks;
    public Double avgNumOfBrokenLinks;
    public Double avgPercOfValidLinks;

    // Facets
    transient public Double avgFacetCoverageByInstanceSum = 0d;
    public Double avgFacetCoverageByInstance;

    // Profiles
    public Profiles profiles = null;

    @XmlElementWrapper(name = "invalidFilesList")
    public List<String> invalidFile = null;

    @XmlElementWrapper(name = "details")
    public List<Message> messages = null;

    public void addNewInvalid(String file) {
	if (invalidFile == null)
	    invalidFile = new LinkedList<>();
	
	invalidFile.add(file);

    }

    @XmlRootElement(name = "profiles")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Profiles {

	@XmlAttribute(name = "count")
	public int totNumOfProfiles = 0;

	public List<Profile> profiles;

	public void handleProfile(String profile) {
	    if (profiles == null)
		profiles = new LinkedList<>();

	    for (Profile p : profiles)
		if (p.name.equals(profile)) {
		    p.count++;
		    return;
		}
	    Profile p = new Profile();
	    p.count = 1;
	    p.name = profile;

	    profiles.add(p);
	    totNumOfProfiles++;
	}

	public void handleProfile(Profile profile) {
	    if (profiles == null)
		profiles = new LinkedList<>();

	    for (Profile p : profiles)
		if (p.name.equals(profile.name)) {
		    p.count += profile.count;
		    return;
		}

	    profiles.add(profile);
	    totNumOfProfiles++;
	}

    }

    @XmlRootElement(name = "profile")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Profile {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public int count;
    }

    public void handleProfile(String profile) {
	if (profiles == null)
	    profiles = new Profiles();
	profiles.handleProfile(profile);
    }

    public void handleProfile(Profile profile) {
	if (profiles == null)
	    profiles = new Profiles();
	profiles.handleProfile(profile);
    }

    @Override
    public void mergeWithParent(CollectionReport parentReport) {

	parentReport.numOfValidFiles += numOfValidFiles;

	// ResProxies
	parentReport.totNumOfResProxies += totNumOfResProxies;
	parentReport.totNumOfResWithMime += totNumOfResWithMime;
	parentReport.totNumOfLandingPages += totNumOfLandingPages;
	parentReport.totNumOfLandingPagesWithoutLink += totNumOfLandingPagesWithoutLink;
	parentReport.totNumOfResources += totNumOfResources;
	parentReport.totNumOfMetadata += totNumOfMetadata;

	// XMLValidator
	parentReport.totNumOfXMLElements += totNumOfXMLElements;
	parentReport.totNumOfXMLSimpleElements += totNumOfXMLSimpleElements;
	parentReport.totNumOfXMLEmptyElement += totNumOfXMLEmptyElement;

	// URL
	parentReport.totNumOfLinks += totNumOfLinks;
	parentReport.totNumOfUniqueLinks += totNumOfUniqueLinks;
	parentReport.totNumOfResProxiesLinks += totNumOfResProxiesLinks;
	parentReport.totNumOfBrokenLinks += totNumOfBrokenLinks;

	// Facet
	parentReport.avgFacetCoverageByInstanceSum += avgFacetCoverageByInstanceSum;

	// Profiles
	for (Profile p : profiles.profiles)
	    parentReport.handleProfile(p);

	// MDSelfLinks
	if (duplicatedMDSelfLink != null && !duplicatedMDSelfLink.isEmpty()) {

	    if (parentReport.duplicatedMDSelfLink == null) {
		parentReport.duplicatedMDSelfLink = new LinkedList();
	    }

	    for (String mdSelfLink : duplicatedMDSelfLink)
		if (!parentReport.duplicatedMDSelfLink.contains(mdSelfLink))
		    parentReport.duplicatedMDSelfLink.add(mdSelfLink);
	}
	
	//invalid files
	if(invalidFile != null){
	    for(String invalid: invalidFile)
		parentReport.addNewInvalid(invalid);
	}

    }

    @Override
    public void marshal(OutputStream os) throws Exception {
	try {

	    JAXBContext jaxbContext = JAXBContext.newInstance(CollectionReport.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	    // output pretty printed
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	    jaxbMarshaller.marshal(this, os);

	} catch (JAXBException e) {
	    e.printStackTrace();
	}

    }

}
