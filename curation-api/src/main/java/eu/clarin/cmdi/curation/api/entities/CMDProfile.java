package eu.clarin.cmdi.curation.api.entities;

import eu.clarin.cmdi.curation.api.processor.CMDProfileProcessor;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */

@Component
@Scope("prototype")
public class CMDProfile {

    private String schemaLocation;
    private String cmdiVersion;
    protected Path path = null;
    
    @Autowired
    CMDProfileProcessor processor;

    public CMDProfile(String schemaLocation, String cmdiVersion) {
        this.schemaLocation = schemaLocation;
        this.cmdiVersion = cmdiVersion;
    }

    public CMDProfileReport generateReport() {
        return processor.process(this);
    }

    public String getCmdiVersion() {
        return cmdiVersion;
    }

    public String getSchemaLocation() {
        return this.schemaLocation;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Profile: " + (path != null ? path.toString() : schemaLocation);
    }

}
