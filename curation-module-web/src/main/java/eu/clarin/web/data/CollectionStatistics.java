package eu.clarin.web.data;

import eu.clarin.cmdi.curation.report.CollectionReport;

import java.util.Collection;
import java.util.List;

public class CollectionStatistics {

    private Double scorePercentage;
    private long fileReport_numOfFiles;
    private int headerReport_profiles_totNumOfProfiles;
    private int urlReport_totNumOfUniqueLinks;
    private int urlReport_totNumOfCheckedLinks;
    private Double urlReport_ratioOfValidLinks;
    private Double resProxyReport_avgNumOfResProxies;
    private int resProxyReport_totNumOfResProxies;
    private Double xmlValidationReport_ratioOfValidRecords;
    private Double xmlPopulatedReport_avgXMLEmptyElement;
    private Double facetReport_coverage;
    private String fileReport_provider;
    private List<Double> facetReport_facet;

    public Double getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(Double scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public long getFileReport_numOfFiles() {
        return fileReport_numOfFiles;
    }

    public void setFileReport_numOfFiles(long fileReport_numOfFiles) {
        this.fileReport_numOfFiles = fileReport_numOfFiles;
    }

    public int getHeaderReport_profiles_totNumOfProfiles() {
        return headerReport_profiles_totNumOfProfiles;
    }

    public void setHeaderReport_profiles_totNumOfProfiles(int headerReport_profiles_totNumOfProfiles) {
        this.headerReport_profiles_totNumOfProfiles = headerReport_profiles_totNumOfProfiles;
    }

    public int getUrlReport_totNumOfUniqueLinks() {
        return urlReport_totNumOfUniqueLinks;
    }

    public void setUrlReport_totNumOfUniqueLinks(int urlReport_totNumOfUniqueLinks) {
        this.urlReport_totNumOfUniqueLinks = urlReport_totNumOfUniqueLinks;
    }

    public int getUrlReport_totNumOfCheckedLinks() {
        return urlReport_totNumOfCheckedLinks;
    }

    public void setUrlReport_totNumOfCheckedLinks(int urlReport_totNumOfCheckedLinks) {
        this.urlReport_totNumOfCheckedLinks = urlReport_totNumOfCheckedLinks;
    }

    public Double getUrlReport_ratioOfValidLinks() {
        return urlReport_ratioOfValidLinks;
    }

    public void setUrlReport_ratioOfValidLinks(Double urlReport_ratioOfValidLinks) {
        this.urlReport_ratioOfValidLinks = urlReport_ratioOfValidLinks;
    }

    public Double getResProxyReport_avgNumOfResProxies() {
        return resProxyReport_avgNumOfResProxies;
    }

    public void setResProxyReport_avgNumOfResProxies(Double resProxyReport_avgNumOfResProxies) {
        this.resProxyReport_avgNumOfResProxies = resProxyReport_avgNumOfResProxies;
    }

    public int getResProxyReport_totNumOfResProxies() {
        return resProxyReport_totNumOfResProxies;
    }

    public void setResProxyReport_totNumOfResProxies(int resProxyReport_totNumOfResProxies) {
        this.resProxyReport_totNumOfResProxies = resProxyReport_totNumOfResProxies;
    }

    public Double getXmlValidationReport_ratioOfValidRecords() {
        return xmlValidationReport_ratioOfValidRecords;
    }

    public void setXmlValidationReport_ratioOfValidRecords(Double xmlValidationReport_ratioOfValidRecords) {
        this.xmlValidationReport_ratioOfValidRecords = xmlValidationReport_ratioOfValidRecords;
    }

    public Double getXmlPopulatedReport_avgXMLEmptyElement() {
        return xmlPopulatedReport_avgXMLEmptyElement;
    }

    public void setXmlPopulatedReport_avgXMLEmptyElement(Double xmlPopulatedReport_avgXMLEmptyElement) {
        this.xmlPopulatedReport_avgXMLEmptyElement = xmlPopulatedReport_avgXMLEmptyElement;
    }

    public Double getFacetReport_coverage() {
        return facetReport_coverage;
    }

    public void setFacetReport_coverage(Double facetReport_coverage) {
        this.facetReport_coverage = facetReport_coverage;
    }

    public String getFileReport_provider() {
        return fileReport_provider;
    }

    public void setFileReport_provider(String fileReport_provider) {
        this.fileReport_provider = fileReport_provider;
    }

    public List<Double> getFacetReport_facet() {
        return facetReport_facet;
    }

    public void setFacetReport_facet(List<Double> facetReport_facet) {
        this.facetReport_facet = facetReport_facet;
    }


}
