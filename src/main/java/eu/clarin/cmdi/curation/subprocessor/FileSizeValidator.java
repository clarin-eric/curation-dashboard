package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator implements ProcessingActivity {

    @Override
    public Severity process(CurationEntity entity) {
	if (entity.getSize() > Config.MAX_SIZE_OF_FILE){
	    entity.addDetail(Severity.FATAL, "The file size exceeds the limit allowed (" + Config.MAX_SIZE_OF_FILE + "B)");
	    return Severity.FATAL;
	}

	return Severity.NONE;
    }

}
