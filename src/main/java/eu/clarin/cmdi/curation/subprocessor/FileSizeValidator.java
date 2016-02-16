package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator extends CMDISubprocessor {

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	report.size = entity.getSize();
	report.path = entity.getPath().toString();
	if (report.size > Config.MAX_SIZE_OF_FILE){
	    report.addDetail(Severity.FATAL, "The file size exceeds the limit allowed (" + Config.MAX_SIZE_OF_FILE + "B)");
	    return false;
	}

	return true;
    }
}
