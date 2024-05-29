package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.subprocessor.instance.*;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CMDInstanceProcessor {
   
   @Autowired
   private ApplicationContext ctx;
   
   private Collection<AbstractSubprocessor<CMDInstance,CMDInstanceReport>> subprocessors;

   
   @PostConstruct
   private void init() {
      
      subprocessors =   
         Stream.of(FileSizeValidator.class, InstanceHeaderProcessor.class, ResourceProxyProcessor.class, XmlValidator.class, UrlValidator.class, InstanceFacetProcessor.class)
            .map(abstactSubprocessorClass -> ctx.getBean(abstactSubprocessorClass)).collect(Collectors.toList());       
   }

   public CMDInstanceReport process(CMDInstance instance) throws MalFunctioningProcessorException {

      final CMDInstanceReport instanceReport = new CMDInstanceReport();


      for(AbstractSubprocessor subprocessor : this.subprocessors) {

          if(!instanceReport.isProcessable) break;

          subprocessor.process(instance, instanceReport);
      }

      return instanceReport;
   }
}
