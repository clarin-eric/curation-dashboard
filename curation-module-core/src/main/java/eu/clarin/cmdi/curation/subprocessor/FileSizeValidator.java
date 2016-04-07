package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator extends CMDSubprocessor {

    @Override
    public boolean process(CMDInstance entity, CMDInstanceReport report) {	
	if (entity.getSize() > Configuration.MAX_FILE_SIZE){
	    report.sizeExceeded = true;
	    report.isValid = false;
	    addMessage(Severity.ERROR, "The file size exceeds the limit allowed (" + Configuration.MAX_FILE_SIZE + "B)");
	}
	
	report.addFileReport(entity.getPath().toString(), entity.getSize(), msgs);
	
	return true;
    }
}
