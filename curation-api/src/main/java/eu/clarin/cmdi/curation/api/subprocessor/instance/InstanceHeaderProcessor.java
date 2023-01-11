package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.CRServiceImpl;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InstanceHeaderProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private CRService crService;   

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report){
      
      InstanceHeaderReport instanceHeaderReport = new InstanceHeaderReport();
      report.setInstanceHeaderReport(instanceHeaderReport);
      
      Map<String, List<ValueSet>> keyValuesMap = instance.getCmdiData().getDocument();

      if(keyValuesMap.containsKey("curation_schemaLocation") && !keyValuesMap.get("curation_schemaLocation").isEmpty()) {
         String[] schemaLocationArray = keyValuesMap.get("curation_schemaLocation").get(0).getValue().split(" ");
         instanceHeaderReport.setSchemaLocation(schemaLocationArray[schemaLocationArray.length - 1]);
      }
      else if(keyValuesMap.containsKey("curation_noNamespaceSchemaLocation") && !keyValuesMap.get("curation_noNamespaceSchemaLocation").isEmpty()) {
                     instanceHeaderReport.setSchemaLocation(keyValuesMap.get("curation_noNamespaceSchemaLocation").get(0).getValue());
      }

      if(keyValuesMap.containsKey("curation_mdProfile") && !keyValuesMap.get("curation_mdProfile").isEmpty()) {
         instanceHeaderReport.setMdProfile(keyValuesMap.get("curation_mdProfile").get(0).getValue());
      }

      if(keyValuesMap.containsKey("collection") && !keyValuesMap.get("collection").isEmpty()) {
          instanceHeaderReport.setMdCollectionDisplayName(keyValuesMap.get("collection").get(0).getValue());
      }

      if(keyValuesMap.containsKey("_selfLink") && !keyValuesMap.get("_selfLink").isEmpty()) {
         instanceHeaderReport.setMdSelfLink(keyValuesMap.get("_selfLink").get(0).getValue());
      }



      
      if (instanceHeaderReport.getSchemaLocation() == null) {
         
         if(instanceHeaderReport.getMdProfile() == null) {
            
            log.debug("Unable to process " + instance.getPath() + ", both schema and profile are not specified");
            report.getInstanceHeaderReport().getScoring().addMessage(Severity.FATAL, "Unable to process " + instance.getPath().getFileName() + ", both schema and profile are not specified");
            return;
            
         }
         else {
            
            report.getInstanceHeaderReport().getScoring().addMessage(Severity.ERROR, "Attribute schemaLocation is missing. " + instanceHeaderReport.getMdProfile() + " is assumed");
            
          }

      }
      else {
         
         if(!crService.isSchemaCRResident(instanceHeaderReport.getSchemaLocation())) {
            report.getInstanceHeaderReport().getScoring().addMessage(Severity.ERROR, "Schema not registered");
         }
         
         if (instanceHeaderReport.getMdProfile() == null) {
            
            report.getInstanceHeaderReport().getScoring().addMessage(Severity.ERROR, "Value for CMD/Header/MdProfile is missing or invalid");
         
         }
         else {
            
            if (instanceHeaderReport.getMdProfile().matches(CRServiceImpl.PROFILE_ID_FORMAT)) {
               
               String profileIdFromSchema = extractProfile(instanceHeaderReport.getSchemaLocation());
               
               if(!instanceHeaderReport.getMdProfile().equals(profileIdFromSchema)) {

                  instanceHeaderReport.getScoring().addMessage(Severity.ERROR, "ProfileId from CMD/Header/MdProfile: " + instanceHeaderReport.getMdProfile()
                        + " and from schemaLocation: " + profileIdFromSchema + " must match!");

               }          
            }           
            else {

               report.getInstanceHeaderReport().getScoring().addMessage(Severity.ERROR,
                     "Format for value in the element /cmd:CMD/cmd:Header/cmd:MdProfile must be: clarin.eu:cr1:p_xxxxxxxxxxxxx!");
            
            }

         }
      }

      if (instanceHeaderReport.getMdCollectionDisplayName() == null) {
         report.getInstanceHeaderReport().getScoring().addMessage(Severity.ERROR, "Value for CMD/Header/MdCollectionDisplayName is missing");
      }


      if (instanceHeaderReport.getMdSelfLink() == null) {
         report.getInstanceHeaderReport().getScoring().addMessage(Severity.ERROR, "Value for CMD/Header/MdSelfLink is missing");
      }
      /*
       * else if ("collection".equalsIgnoreCase(conf.getMode()) ||
       * "all".equalsIgnoreCase(conf.getMode())) {// collect mdSelfLinks when
       * assessing collection if (!CMDInstance.mdSelfLinks.add(mdSelfLink))
       * CMDInstance.duplicateMDSelfLink.add(mdSelfLink); }
       */

      // at this point profile will be processed and cached
      
      String schemaLocation = instanceHeaderReport.getSchemaLocation()!=null?instanceHeaderReport.getSchemaLocation():instanceHeaderReport.getMdProfile();
      report.setHeaderReport(new ProfileHeaderReport(crService.createProfileHeader(schemaLocation, "1.x", false)));


   }

   private String extractProfile(String str) {
      Matcher m = CRServiceImpl.PROFILE_ID_PATTERN.matcher(str);
      return m.find() ? m.group() : null;

   }
}
