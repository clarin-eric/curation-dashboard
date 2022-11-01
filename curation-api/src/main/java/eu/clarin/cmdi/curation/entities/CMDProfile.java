package eu.clarin.cmdi.curation.entities;

import eu.clarin.cmdi.curation.processor.CMDProfileProcessor;
import eu.clarin.cmdi.curation.report.CMDProfileReport;

import java.nio.file.Path;

/**
 *
 */

public class CMDProfile {

    private String schemaLocation;
    private String cmdiVersion;
    protected Path path = null;

    public CMDProfile(String schemaLocation, String cmdiVersion) {
        this.schemaLocation = schemaLocation;
        this.cmdiVersion = cmdiVersion;
    }

    public CMDProfileReport generateReport() {
        return new CMDProfileProcessor().process(this);
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
