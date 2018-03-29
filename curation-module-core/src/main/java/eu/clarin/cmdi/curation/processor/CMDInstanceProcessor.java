package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.CollectionInstanceFacetProcessor;
import eu.clarin.cmdi.curation.subprocessor.CollectionInstanceHeaderProcessor;
import eu.clarin.cmdi.curation.subprocessor.CollectionInstanceResourceProxyProcessor;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.InstanceFacetProcessor;
import eu.clarin.cmdi.curation.subprocessor.InstanceHeaderProcessor;
import eu.clarin.cmdi.curation.subprocessor.InstanceResourceProxyProcessor;
import eu.clarin.cmdi.curation.subprocessor.InstanceXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.URLValidator;

public class CMDInstanceProcessor extends AbstractProcessor<CMDInstanceReport> {

    @Override	
    protected Collection<ProcessingStep> createPipeline() {
		return (Configuration.COLLECTION_MODE)?
				Arrays.asList(
					new FileSizeValidator(),
					new CollectionInstanceHeaderProcessor(),
					new CollectionInstanceResourceProxyProcessor(),
					//new URLValidator(),//todo
					new InstanceXMLValidator(),
					new CollectionInstanceFacetProcessor())
				:
				Arrays.asList(
					new FileSizeValidator(),
					new InstanceHeaderProcessor(),
					new InstanceResourceProxyProcessor(),
					new URLValidator(),
					new InstanceXMLValidator(),
					new InstanceFacetProcessor()
				);
				
			
    }

    @Override
    protected CMDInstanceReport createReport() {
    	return new CMDInstanceReport();
    }

}
