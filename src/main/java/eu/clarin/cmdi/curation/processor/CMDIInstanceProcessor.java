package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.InstanceFacetHandler;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.InstanceHeaderValidatior;
import eu.clarin.cmdi.curation.subprocessor.InstanceXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.URLValidator;

public class CMDIInstanceProcessor extends AbstractProcessor<CMDIInstanceReport> {

    @Override
    protected Collection<ProcessingStep> createPipeline() {
	return Arrays.asList(
		new FileSizeValidator(),
		new InstanceHeaderValidatior(),
		//new ResourceProxyValidator(),
		new InstanceXMLValidator(),
		new URLValidator(),
		new InstanceFacetHandler());
    }

    @Override
    protected CMDIInstanceReport createReport() {
	return new CMDIInstanceReport();
    }

}
