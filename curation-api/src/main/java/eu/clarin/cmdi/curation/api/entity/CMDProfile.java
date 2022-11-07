package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.processor.CMDProfileProcessor;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import lombok.Data;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */

@Component
@Scope(value="prototype")
@Data
public class CMDProfile{

    private String schemaLocation;
    private String cmdiVersion;
    private Path path = null;
    
    @Autowired
    CMDProfileProcessor processor;
    

    public CMDProfile(String schemaLocation, String cmdiVersion) {
        this.schemaLocation = schemaLocation;
        this.cmdiVersion = cmdiVersion;
    }

    public CMDProfileReport generateReport() {
        return processor.process(this);
    }
}
