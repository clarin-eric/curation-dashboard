package eu.clarin.cmdi.curation.subprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.InstanceParser;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.FileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.vlo_extensions.CMDIDataImplFactory;
import eu.clarin.cmdi.curation.vlo_extensions.FacetMappingCacheFactory;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;

import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

public class FileSizeValidator extends CMDSubprocessor {
    private final static Logger _logger = LoggerFactory.getLogger(FileSizeValidator.class);
    
    private static final CMDIDataProcessor<Map<String,List<ValueSet>>> _processor = getProcessor();
    

    private static CMDIDataProcessor<Map<String,List<ValueSet>>> getProcessor() {
        try {
            
            
            final VloConfig vloConfig = new DefaultVloConfigFactory().newConfig();
            
            final LanguageCodeUtils languageCodeUtils = new LanguageCodeUtils(vloConfig);
            
            final FieldNameService fieldNameService = new FieldNameServiceImpl(vloConfig);
            
            final CMDIDataImplFactory cmdiDataFactory = new CMDIDataImplFactory(fieldNameService);
            
            final VLOMarshaller marshaller = new VLOMarshaller();
            
            final FacetMappingFactory facetMappingFactory = FacetMappingCacheFactory.getInstance();
            
            return  new CMDIParserVTDXML<Map<String,List<ValueSet>>>(
                    MetadataImporter.registerPostProcessors(vloConfig, fieldNameService, languageCodeUtils),
                    MetadataImporter.registerPostMappingFilters(fieldNameService),
                    vloConfig, facetMappingFactory, marshaller, cmdiDataFactory, fieldNameService, false);
                    
                    
        } 
        catch (IOException ex) {
            _logger.error("couldn't instatiate CMDIDataProcessor - so instance parsing won't work!");
            return null;
        }        
    }


	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception{
		report.fileReport = new FileReport();
		report.fileReport.size = entity.getSize();
		if(entity.getUrl()!=null){
			report.fileReport.location = entity.getUrl().replaceAll("/", "-");
		}else{
			report.fileReport.location = entity.getPath().toString();
		}


		if (report.fileReport.size > Configuration.MAX_FILE_SIZE) {
			addMessage(Severity.FATAL, "The file size exceeds the limit allowed (" + Configuration.MAX_FILE_SIZE + "B)");
			//don't assess when assessing collections
			if(Configuration.COLLECTION_MODE)
				throw new FileSizeException(entity.getPath().getFileName().toString(), report.fileReport.size);
		}

		InstanceParser transformer = new InstanceParser();
		try {
		    _logger.debug("parsing instance...");
			entity.setParsedInstance(transformer.parseIntance(Files.newInputStream(entity.getPath())));
			_logger.debug("...done");
		} catch (TransformerException | IOException e) {
			throw new Exception("Unable to parse CMDI instance " + entity.getPath().toString(), e);
		}
		
		

		_logger.debug("creating CMDIData object...");

        CMDIData<Map<String,List<ValueSet>>> cmdiData = _processor.process(entity.getPath().toFile(), new ResourceStructureGraph());
        
        _logger.debug("...done");
        
        entity.setCMDIData(cmdiData);


	}

	@Override
	public Score calculateScore(CMDInstanceReport report) {
		//in case that size exceeds the limit msgs will be created and it will contain a single msg
		return new Score(msgs == null? 1.0 : 0, 1.0, "file-size", msgs);
	}
}
