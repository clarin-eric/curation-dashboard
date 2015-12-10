package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator implements CurationStep{

	@Override
	public boolean apply(CurationEntity entity) {
		if(entity.getSize() > Config.MAX_SIZE_OF_FILE){
			entity.addMessage(new Message(Severity.FATAL, "The file size of " + entity.getPath() + " exceeds the limit allowed (" + Config.MAX_SIZE_OF_FILE + "B)"));
			return false;
		}
		return true;
	}

}
