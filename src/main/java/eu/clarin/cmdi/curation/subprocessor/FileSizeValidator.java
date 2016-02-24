package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator extends CMDISubprocessor {

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {	
	if (entity.getSize() > Config.MAX_SIZE_OF_FILE()){
	    report.sizeExceeded = true;
	    report.isValid = false;
	    addMessage(Severity.ERROR, "The file size exceeds the limit allowed (" + Config.MAX_SIZE_OF_FILE() + "B)");
	}
	
	report.addFileReport(entity.getPath().toString(), entity.getSize(), msgs);
	
	return true;
    }
}
