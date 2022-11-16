package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.curation.api.configuration.CurationConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.NoCMDIDataProcessorException;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.instance_parser.InstanceParser;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.api.vlo_extension.CMDIDataImplFactory;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FileSizeValidator extends AbstractSubprocessor {

   private static final Pattern _pattern = Pattern.compile("xmlns(:.+?)?=\"http(s)?://www.clarin.eu/cmd/(1)?");

   private CMDIDataProcessor<Map<String, List<ValueSet>>> processor;
   
   private CurationConfig conf;


   @Autowired
   public FileSizeValidator(CurationConfig conf, FacetsMappingCacheFactory fac) {
      
      this.conf = conf;
      
      try {
         final VloConfig vloConfig = new DefaultVloConfigFactory().newConfig();

         final LanguageCodeUtils languageCodeUtils = new LanguageCodeUtils(vloConfig);

         final FieldNameService fieldNameService = new FieldNameServiceImpl(vloConfig);

         final CMDIDataImplFactory cmdiDataFactory = new CMDIDataImplFactory(fieldNameService);

         final VLOMarshaller marshaller = new VLOMarshaller();


         this.processor = new CMDIParserVTDXML<>(
               MetadataImporter.registerPostProcessors(vloConfig, fieldNameService, languageCodeUtils),
               MetadataImporter.registerPostMappingFilters(fieldNameService), vloConfig, fac,
               marshaller, cmdiDataFactory, fieldNameService, false);

      }
      catch (IOException ex) {
         log.error("couldn't instatiate CMDIDataProcessor - so instance parsing won't work!");
         throw new NoCMDIDataProcessorException(ex);
      }
   }

   private boolean isLatestVersion(Path path){
      String line = null;
      Matcher matcher;

      try (BufferedReader reader = Files.newBufferedReader(path)) {

         while ((line = reader.readLine()) != null)
            if ((matcher = _pattern.matcher(line)).find())
               return matcher.group(3) != null;
      }
      catch (IOException ex) {
         log.error("can't identitfy cmdi version by namespace", ex);
      }
      return false;
   }

   @Override
   public synchronized void process(CMDInstance entity, CMDInstanceReport report) throws SubprocessorException{

      // convert cmdi 1.1 to 1.2 if necessary

      if (!isLatestVersion(entity.getPath())) {
         Path newPath = null;
         
         try {
            newPath = Files.createTempFile(null, ".xml");
         }
         catch (IOException e) {
            
            log.error("can't create temporary outputfile for CMD1.1 to CMD1.x transformation");
            throw new RuntimeException(e);
         }

         TransformerFactory factory = TransformerFactory.newInstance();
         Source xslt = new StreamSource(InstanceParser.class.getResourceAsStream("/xslt/cmd-record-1_1-to-1_2.xsl"));

         Transformer transformer;
         try {
            transformer = factory.newTransformer(xslt);
            transformer.transform(new StreamSource(entity.getPath().toFile()), new StreamResult(newPath.toFile()));
         }
         catch (TransformerConfigurationException e) {
            
            log.error("can't create Transformer object from resource '/xslt/cmd-record-1_1-to-1_2.xsl' - make sure that the resource is in the classpath!");
            throw new RuntimeException(e);
         }
         catch (TransformerException e) {
            
            log.error("can't transfrom input file '{}'", entity.getPath());
            throw new SubprocessorException(e);
            
         }
         

         this.addMessage(Severity.INFO, "tranformed cmdi version 1.1 into version 1.2");

         entity.setPath(newPath);
         try {
            entity.setSize(Files.size(newPath));
         }
         catch (IOException e) {

            log.error("can't get size from temporary transfromer output file '{}'", newPath);
            throw new SubprocessorException(e);
            
         }
      }
      
      report.fileReport = new CMDInstanceReport.FileReport();
      report.fileReport.size = entity.getSize();
      
      //from instance upload
      if (entity.getUrl() != null) {
         report.fileReport.location = FileNameEncoder.encode(entity.getUrl());
      }
      else {
         Path filePath = entity.getPath();
         
         //file in the data directory
         if (filePath.startsWith(conf.getDirectory().getDataRoot())) {
            report.fileReport.location = conf.getDirectory().getDataRoot().relativize(filePath).toString();
         }
         //otherwise
         else {
            report.fileReport.location = filePath.toString();
         }

      }

      if (report.fileReport.size > conf.getMaxFileSize()) {
         this.addMessage(Severity.FATAL, "The file size exceeds the limit allowed (" + conf.getMaxFileSize()+ "B)");
         // don't assess when assessing collections
         if ("collection".equalsIgnoreCase(conf.getMode())) {
            throw new SubprocessorException();
         }
      }

      CMDIData<Map<String, List<ValueSet>>> cmdiData = null;

      try {
         cmdiData = processor.process(entity.getPath().toFile(), new ResourceStructureGraph());
      }
      catch (TransformerException e) {

         log.debug("can't transform file '{}'", entity.getPath());
         throw new SubprocessorException(e);
         
      }
      catch (Exception e) {
         
         log.debug("can't create CMDIData object from file '{}'", entity.getPath());
         throw new SubprocessorException(e);
      
      }

      entity.setCmdiData(cmdiData);

      // create xpath/value pairs only in instance mode
      if (!"collection".equalsIgnoreCase(conf.getMode())) {

         InstanceParser transformer = new InstanceParser();

         log.debug("parsing instance...");
         try {
            entity.setParsedInstance(transformer.parseIntance(Files.newInputStream(entity.getPath())));
         }
         catch (TransformerException e) {
            
            log.error("can't transform CMD instance file '{}'", entity.getPath());
            throw new SubprocessorException(e);
         
         }
         catch (IOException e) {
            
            log.error("can't read CMD file '{}'", entity.getPath());
            throw new SubprocessorException(e);
         
         }
         log.debug("...done");
      }
   }

   @Override
   public synchronized Score calculateScore(CMDInstanceReport report) {
      // in case that size exceeds the limit msgs will be created and it will contain
      // a single msg
      return new Score(this.getMessages().size() == 0 ? 1.0 : 0, 1.0, "file-size", this.getMessages());
   }
}
