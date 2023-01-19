package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Issue;
import eu.clarin.cmdi.curation.api.report.Issue.Severity;
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
      
      report.instanceHeaderReport  = new InstanceHeaderReport();
      
      Map<String, List<ValueSet>> keyValuesMap = instance.getCmdiData().getDocument();

      if(keyValuesMap.containsKey("curation_schemaLocation") && !keyValuesMap.get("curation_schemaLocation").isEmpty()) {
         String[] schemaLocationArray = keyValuesMap.get("curation_schemaLocation").get(0).getValue().split(" ");
         report.instanceHeaderReport.schemaLocation = (schemaLocationArray[schemaLocationArray.length - 1]);
      }
      else if(keyValuesMap.containsKey("curation_noNamespaceSchemaLocation") && !keyValuesMap.get("curation_noNamespaceSchemaLocation").isEmpty()) {
                     report.instanceHeaderReport.schemaLocation = keyValuesMap.get("curation_noNamespaceSchemaLocation").get(0).getValue();
      }

      if(keyValuesMap.containsKey("curation_mdProfile") && !keyValuesMap.get("curation_mdProfile").isEmpty()) {
         report.instanceHeaderReport.mdProfile = keyValuesMap.get("curation_mdProfile").get(0).getValue();
      }

      if(keyValuesMap.containsKey("collection") && !keyValuesMap.get("collection").isEmpty()) {
          report.instanceHeaderReport.mdCollectionDisplayName = keyValuesMap.get("collection").get(0).getValue();
      }

      if(keyValuesMap.containsKey("_selfLink") && !keyValuesMap.get("_selfLink").isEmpty()) {
         report.instanceHeaderReport.mdSelfLink = keyValuesMap.get("_selfLink").get(0).getValue();
      }



      
      if (report.instanceHeaderReport.schemaLocation == null) {
         
         if(report.instanceHeaderReport.mdProfile == null) {
            
            log.debug("Unable to process " + instance.getPath() + ", both schema and profile are not specified");
            report.messages.add(new Issue(Severity.FATAL, "Unable to process " + instance.getPath().getFileName() + ", both schema and profile are not specified"));
            return;
            
         }
         else {
            
            report.messages.add(new Issue(Severity.ERROR, "Attribute schemaLocation is missing. " + report.instanceHeaderReport.mdProfile + " is assumed"));
            
          }

      }
      else {
         
         if(!crService.isSchemaCRResident(report.instanceHeaderReport.schemaLocation)) {
            report.messages.add(new Issue(Severity.ERROR, "Schema not registered"));
         }
         
         if (report.instanceHeaderReport.mdProfile == null) {
            
            report.messages.add(new Issue(Severity.ERROR, "Value for CMD/Header/MdProfile is missing or invalid"));
         
         }
         else {
            
            if (report.instanceHeaderReport.mdProfile.matches(CRServiceImpl.PROFILE_ID_FORMAT)) {
               
               String profileIdFromSchema = extractProfile(report.instanceHeaderReport.schemaLocation);
               
               if(!report.instanceHeaderReport.mdProfile.equals(profileIdFromSchema)) {

                  report.messages.add(new Issue(Severity.ERROR, "ProfileId from CMD/Header/MdProfile: " + report.instanceHeaderReport.mdProfile
                        + " and from schemaLocation: " + profileIdFromSchema + " must match!"));

               }          
            }           
            else {

               report.messages.add(new Issue(Severity.ERROR,
                     "Format for value in the element /cmd:CMD/cmd:Header/cmd:MdProfile must be: clarin.eu:cr1:p_xxxxxxxxxxxxx!"));
            
            }

         }
      }

      if (report.instanceHeaderReport.mdCollectionDisplayName == null) {
         report.messages.add(new Issue(Severity.ERROR, "Value for CMD/Header/MdCollectionDisplayName is missing"));
      }


      if (report.instanceHeaderReport.mdSelfLink == null) {
         report.messages.add(new Issue(Severity.ERROR, "Value for CMD/Header/MdSelfLink is missing"));
      }
      /*
       * else if ("collection".equalsIgnoreCase(conf.getMode()) ||
       * "all".equalsIgnoreCase(conf.getMode())) {// collect mdSelfLinks when
       * assessing collection if (!CMDInstance.mdSelfLinks.add(mdSelfLink))
       * CMDInstance.duplicateMDSelfLink.add(mdSelfLink); }
       */

      // at this point profile will be processed and cached
      
      String schemaLocation = report.instanceHeaderReport.schemaLocation!=null?report.instanceHeaderReport.schemaLocation:report.instanceHeaderReport.mdProfile;
      report.headerReport = (new ProfileHeaderReport(crService.createProfileHeader(schemaLocation, "1.x", false)));


   }

   private String extractProfile(String str) {
      Matcher m = CRServiceImpl.PROFILE_ID_PATTERN.matcher(str);
      return m.find() ? m.group() : null;

   }
}
