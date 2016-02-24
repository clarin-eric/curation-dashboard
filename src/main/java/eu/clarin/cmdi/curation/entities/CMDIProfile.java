package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDIProfileProcessor;

/**
 * @author dostojic
 *
 */

public class CMDIProfile extends CurationEntity {

    private String profile;
    

    public CMDIProfile(String profile){
	super(null);
	this.profile = profile;
    }

    public CMDIProfile(Path path) {
	super(path);
    }

    public CMDIProfile(Path path, long size) {
	super(path, size);
    }    

    public String getProfile() {
	return profile;
    }

    public void setProfile(String profile) {
	this.profile = profile;
    }

    @Override
    protected AbstractProcessor getProcessor() {
	return new CMDIProfileProcessor();
    }

}
