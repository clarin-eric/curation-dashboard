package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Score;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceXMLValidator extends CMDSubprocessor {

    static final Logger _logger = LoggerFactory.getLogger(InstanceXMLValidator.class);

    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
        //todo
    }

    @Override
    public Score calculateScore(CMDInstanceReport report) {
        //todo
        return null;
    }
}
