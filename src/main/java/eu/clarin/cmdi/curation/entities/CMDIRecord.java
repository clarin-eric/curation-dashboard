package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDIProcessor;
import eu.clarin.cmdi.curation.report.Report;

public class CMDIRecord extends CurationEntity {

    public final static Set<String> uniqueMDSelfLinks = Collections.synchronizedSet(new HashSet<>());
    public final static Collection<String> duplicateMDSelfLinks = Collections
	    .synchronizedCollection(new LinkedList<>());

    
    private String profile = null;

    Collection<CMDIUrlNode> values;

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

    public Collection<CMDIUrlNode> getValues() {
	return values;
    }

    public void setValues(Collection<CMDIUrlNode> values) {
	this.values = values;
    }
    
    /* (non-Javadoc)
     * @see eu.clarin.cmdi.curation.entities.CurationEntity#addReport(eu.clarin.cmdi.curation.report.Report)
     */
    @Override
    public void addReport(Report report) {
        if(Config.PRINT_COLLECTION_DETAILS)
            reports.add(report);
    }

}
