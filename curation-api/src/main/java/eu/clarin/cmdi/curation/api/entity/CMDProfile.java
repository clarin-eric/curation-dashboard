package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.cache.ProfileReportCache;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import lombok.Data;
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

    private boolean isCacheable;

    
    @Autowired
    ProfileReportCache profileReportCache;

    public CMDProfile(String schemaLocation, boolean isCacheable) {

        this.schemaLocation = schemaLocation;

        this.isCacheable = isCacheable;
    }

    public CMDProfileReport generateReport() throws MalFunctioningProcessorException {
        return profileReportCache.getProfileReport(this);
    }
}
