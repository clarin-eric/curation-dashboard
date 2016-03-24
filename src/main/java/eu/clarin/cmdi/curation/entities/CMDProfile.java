package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDProfileProcessor;

/**
 * @author dostojic
 *
 */

public class CMDProfile extends CurationEntity {

    private String profile;
    

    public CMDProfile(String profile){
	super(null);
	this.profile = profile;
    }

    public CMDProfile(Path path) {
	super(path);
    }

    public CMDProfile(Path path, long size) {
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
	return new CMDProfileProcessor();
    }

}
