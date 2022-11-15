package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.cache.ProfileScoreCache;
import eu.clarin.cmdi.curation.api.configuration.CurationConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.CRServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InstanceHeaderProcessor extends AbstractSubprocessor {

   @Autowired
   private CurationConfig conf;
   @Autowired
   private VloConfig vloConf;
   @Autowired
   ProfileScoreCache pCache;
   @Autowired
   private CRService crService;

   boolean missingSchema = false;
   boolean schemaInCR = false;
   boolean missingMdprofile = false;
   boolean invalidMdprofile = false;
   boolean missingMdCollectionDisplayName = false;
   boolean missingMdSelfLink = false;
   

   @Override
   public synchronized void process(CMDInstance entity, CMDInstanceReport report) throws SubprocessorException {
      Map<String, List<ValueSet>> keyValuesMap = entity.getCmdiData().getDocument();

      String schemaLocation = keyValuesMap.containsKey("curation_schemaLocation")
            && !keyValuesMap.get("curation_schemaLocation").isEmpty()
                  ? keyValuesMap.get("curation_schemaLocation").get(0).getValue()
                  : null;

      String profileIdFromSchema = null;

      if (schemaLocation != null) {
         String[] schemaLocationArray = schemaLocation.split(" ");
         schemaLocation = schemaLocationArray[schemaLocationArray.length - 1];

         profileIdFromSchema = extractProfile(schemaLocation);
      }
      else {
         schemaLocation = keyValuesMap.containsKey("curation_noNamespaceSchemaLocation")
               && !keyValuesMap.get("curation_noNamespaceSchemaLocation").isEmpty()
                     ? keyValuesMap.get("curation_noNamespaceSchemaLocation").get(0).getValue()
                     : null;
      }

      String mdProfile = keyValuesMap.containsKey("curation_mdProfile")
            && !keyValuesMap.get("curation_mdProfile").isEmpty()
                  ? keyValuesMap.get("curation_mdProfile").get(0).getValue()
                  : null;

      String mdCollectionDisplayName = keyValuesMap.containsKey("collection")
            && !keyValuesMap.get("collection").isEmpty() ? keyValuesMap.get("collection").get(0).getValue() : null;

      String mdSelfLink = keyValuesMap.containsKey("_selfLink") && !keyValuesMap.get("_selfLink").isEmpty()
            ? keyValuesMap.get("_selfLink").get(0).getValue()
            : null;

      missingSchema = (schemaLocation == null);
      missingMdprofile = (mdProfile == null);
      missingMdCollectionDisplayName = (mdCollectionDisplayName == null);
      missingMdSelfLink = (mdSelfLink == null);

      if (missingSchema && missingMdprofile) {
         log.debug("Unable to process " + entity + ", both schema and profile are not specified");
         throw new SubprocessorException();
      }
      if (missingSchema) {
         schemaLocation = vloConf.getComponentRegistryProfileSchema(mdProfile);
         addMessage(Severity.ERROR, "Attribute schemaLocation is missing. " + mdProfile + " is assumed");
      }
      else
         schemaInCR = crService.isSchemaCRResident(schemaLocation);

      // now obsolete
      /*
       * if (cmdVersion != null && !cmdVersion.isEmpty() && !cmdVersion.equals("1.2"))
       * addMessage(Severity.WARNING,
       * "Current CMD version is 1.2 but this recordName is using " + cmdVersion);
       */

      if (!missingMdprofile) {
         if (!keyValuesMap.get("curation_mdProfile").get(0).getValue().matches(CRServiceImpl.PROFILE_ID_FORMAT)) {
            invalidMdprofile = true;
            addMessage(Severity.ERROR,
                  "Format for value in the element /cmd:CMD/cmd:Header/cmd:MdProfile must be: clarin.eu:cr1:p_xxxxxxxxxxxxx!");
//				mdprofile = extractProfile(mdprofile);
         }

         if (profileIdFromSchema != null && !mdProfile.equals(profileIdFromSchema)) {
            invalidMdprofile = true;
            addMessage(Severity.ERROR, "ProfileId from CMD/Header/MdProfile: " + mdProfile
                  + " and from schemaLocation: " + profileIdFromSchema + " must match!");
            mdProfile = profileIdFromSchema; // it is important to continue with ID from schemalocation otherwise cache
                                             // will create extra entry
         }
      }

      if (missingMdprofile) {
         addMessage(Severity.ERROR, "Value for CMD/Header/MdProfile is missing or invalid");
      }

      if (missingMdCollectionDisplayName)
         addMessage(Severity.ERROR, "Value for CMD/Header/MdCollectionDisplayName is missing");

      if (missingMdSelfLink) {
         addMessage(Severity.ERROR, "Value for CMD/Header/MdSelfLink is missing");
      }
      else if ("collection".equalsIgnoreCase(conf.getMode())) {// collect mdSelfLinks when assessing collection
         if (!CMDInstance.mdSelfLinks.add(mdSelfLink))
            CMDInstance.duplicateMDSelfLink.add(mdSelfLink);
      }

      // at this point profile will be processed and cached
      report.header = crService.createProfileHeader(schemaLocation, "1.x", false);
      report.fileReport.collection = mdCollectionDisplayName;

      report.profileScore = pCache.getScore(report.header);

      report.addSegmentScore(new Score(report.profileScore, pCache.getScore(report.header), "profiles-score", null));

   }

   public synchronized Score calculateScore(CMDInstanceReport report) {
      double score = 0;

      // schema exists
      if (!missingSchema) {
         score++; // * weight
         if (schemaInCR)// schema comes from Component Registry
            score++;
         else
            addMessage(Severity.INFO, "Schema not from component registry. Using default schema "
                  + vloConf.getComponentRegistryProfileSchema(report.header.getId()));
      }

      // mdprofile exists and in correct format
      if (!missingMdprofile && !invalidMdprofile)
         score++;
      if (!missingMdCollectionDisplayName)
         score++;
      if (!missingMdSelfLink)
         score++;

      return new Score(score, 5.0, "cmd-header-schema", this.getMessages());
   }

   private String extractProfile(String str) {
      Matcher m = CRServiceImpl.PROFILE_ID_PATTERN.matcher(str);
      return m.find() ? m.group() : null;

   }

}