package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.cache.ProfileReportCache;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */

@Component
@Scope(value="prototype")
@RequiredArgsConstructor
@Data
public class CMDProfile{

    private String schemaLocation;

    private boolean isCacheable;

    private final ProfileReportCache profileReportCache;

    public CMDProfileReport generateReport() throws MalFunctioningProcessorException {
        return profileReportCache.getProfileReport(this);
    }
}
