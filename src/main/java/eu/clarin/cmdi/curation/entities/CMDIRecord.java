package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDIProcessor;

public class CMDIRecord extends CurationEntity{

    public final static Set<String> uniqueMDSelfLinks = Collections.synchronizedSet(new HashSet<>());
    public final static Collection<String> duplicateMDSelfLinks = Collections
	    .synchronizedCollection(new LinkedList<>());
    
    //Header
    String profile = null;
    
    
    //ResProxy
    int numOfResProxies;
    int numOfResWithMime;
    int numOfLandingPages;
    int numOfLandingPagesWithoutLink;
    int numOfResources;
    int numOfMetadata;
    
    
    //XMLValidator
    int numOfXMLElements;
    int numOfXMLSimpleElements;
    int numOfXMLEmptyElement;
    //double populatedElementsPer;
    
    //URL
    int numOfLinks;
    int numOfUniqueLinks;
    int numOfResProxiesLinks;
    int numOfBrokenLinks;
    //double perc 
    
    //facets
    int numOfFacets;
    int numOfCoveredFacetsProfile;
    //int numOfCoveredFacetsInstance; //extracted from values
    
    Map<String, List<String>> facetValues = new HashMap<>();
    
    Collection<CMDIUrlNode> links = new LinkedList<>();
    
    
    

    public CMDIRecord(Path path) {
	super(path);
    }

    public CMDIRecord(Path path, long size) {
	super(path, size);
    }
    

    @Override
    protected AbstractProcessor getProcessor() {
	return new CMDIProcessor();
    }

    public String getProfile() {
	return profile;
    }

    public void setProfile(String profile) {
	this.profile = profile;
    }

    public Collection<CMDIUrlNode> getLinks() {
	return links;
    }

    public void setLinks(Collection<CMDIUrlNode> links) {
	this.links = links;
    }
    

    public int getNumOfResWithMime() {
        return numOfResWithMime;
    }

    public void setNumOfResWithMime(int numOfResWithMime) {
        this.numOfResWithMime = numOfResWithMime;
    }

    public int getNumOfLandingPages() {
        return numOfLandingPages;
    }

    public void setNumOfLandingPages(int numOfLandingPages) {
        this.numOfLandingPages = numOfLandingPages;
    }

    public int getNumOfLandingPagesWithoutLink() {
        return numOfLandingPagesWithoutLink;
    }

    public void setNumOfLandingPagesWithoutLink(int numOfLandingPagesWithoutLink) {
        this.numOfLandingPagesWithoutLink = numOfLandingPagesWithoutLink;
    }

    public int getNumOfResources() {
        return numOfResources;
    }

    public void setNumOfResources(int numOfResources) {
        this.numOfResources = numOfResources;
    }

    public int getNumOfMetadata() {
        return numOfMetadata;
    }

    public void setNumOfMetadata(int numOfMetadata) {
        this.numOfMetadata = numOfMetadata;
    }

    public int getNumOfXMLElements() {
        return numOfXMLElements;
    }

    public void setNumOfXMLElements(int numOfXMLElements) {
        this.numOfXMLElements = numOfXMLElements;
    }

    public int getNumOfXMLSimpleElements() {
        return numOfXMLSimpleElements;
    }

    public void setNumOfXMLSimpleElements(int numOfXMLSimpleElements) {
        this.numOfXMLSimpleElements = numOfXMLSimpleElements;
    }

    public int getNumOfXMLEmptyElement() {
        return numOfXMLEmptyElement;
    }

    public void setNumOfXMLEmptyElement(int numOfXMLEmptyElement) {
        this.numOfXMLEmptyElement = numOfXMLEmptyElement;
    }

    public int getNumOfLinks() {
        return numOfLinks;
    }

    public void setNumOfLinks(int numOfLinks) {
        this.numOfLinks = numOfLinks;
    }

    public int getNumOfUniqueLinks() {
        return numOfUniqueLinks;
    }

    public void setNumOfUniqueLinks(int numOfUniqueLinks) {
        this.numOfUniqueLinks = numOfUniqueLinks;
    }

    public int getNumOfResProxies() {
        return numOfResProxies;
    }

    public void setNumOfResProxies(int numOfResProxies) {
        this.numOfResProxies = numOfResProxies;
    }

    public int getNumOfBrokenLinks() {
        return numOfBrokenLinks;
    }

    public void setNumOfBrokenLinks(int numOfBrokenLinks) {
        this.numOfBrokenLinks = numOfBrokenLinks;
    }


    public int getNumOfResProxiesLinks() {
        return numOfResProxiesLinks;
    }

    public void setNumOfResProxiesLinks(int numOfResProxiesLinks) {
        this.numOfResProxiesLinks = numOfResProxiesLinks;
    }

    public int getNumOfFacets() {
        return numOfFacets;
    }

    public void setNumOfFacets(int numOfFacets) {
        this.numOfFacets = numOfFacets;
    }

    public int getNumOfCoveredFacetsProfile() {
        return numOfCoveredFacetsProfile;
    }

    public void setNumOfCoveredFacetsProfile(int numOfCoveredFacetsProfile) {
        this.numOfCoveredFacetsProfile = numOfCoveredFacetsProfile;
    }

    public int getNumOfCoveredFacetsInstance() {
        return facetValues.size();
    }


    public Map<String, List<String>> getFacetValues() {
        return facetValues;
    }

    public void setFacetValues(Map<String, List<String>> facetValues) {
        this.facetValues = facetValues;
    }

    public static Set<String> getUniquemdselflinks() {
        return uniqueMDSelfLinks;
    }

    public static Collection<String> getDuplicatemdselflinks() {
        return duplicateMDSelfLinks;
    }

}
