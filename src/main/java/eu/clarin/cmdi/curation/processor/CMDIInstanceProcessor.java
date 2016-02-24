package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.CMDIInstanceFacets;
import eu.clarin.cmdi.curation.subprocessor.CMDIInstanceHeaderValidator;
import eu.clarin.cmdi.curation.subprocessor.InstanceXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.URLValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.ResourceProxyValidator;

public class CMDIInstanceProcessor extends AbstractProcessor<CMDIInstanceReport> {

    @Override
    protected Collection<ProcessingStep> createPipeline() {
	return Arrays.asList(
		new FileSizeValidator(),
		new CMDIInstanceHeaderValidator(),
		new ResourceProxyValidator(),
		new InstanceXMLValidator(),
		new URLValidator(),
		new CMDIInstanceFacets());
    }

    @Override
    protected CMDIInstanceReport createReport() {
	return new CMDIInstanceReport();
    }

}
