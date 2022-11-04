package eu.clarin.cmdi.curation.api.subprocessor.ext;

import eu.clarin.cmdi.curation.api.entities.CMDProfile;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractMessageCollection;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.vlo.config.VloConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(value="prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProfileHeaderHandler extends AbstractMessageCollection{

   @Autowired
   private PPHService pphService;
   @Autowired
   private VloConfig vloConf;


   public void process(CMDProfile entity, CMDProfileReport report) throws SubprocessorException {

      boolean isLocalFile = false;

      if (entity.getSchemaLocation() == null && entity.getSchemaLocation().isEmpty()) {
         
         log.error("no schema location in CMD file '{}' ", entity.getPath());
         throw new SubprocessorException();
         
      }


      if (entity.getSchemaLocation().startsWith(vloConf.getComponentRegistryRESTURL()))
         report.header = pphService.getProfileHeaders().stream()
               .filter(h -> h.getSchemaLocation().equals(entity.getSchemaLocation())).findFirst().orElse(null);

      if (report.header == null) {
         report.header = new ProfileHeader();
         report.header.setSchemaLocation(entity.getSchemaLocation());
         report.header.setCmdiVersion(entity.getCmdiVersion());

         report.header.setPublic(false);
      }

      report.header.setLocalFile(isLocalFile);

      if (!report.header.isPublic())
         addMessage(Severity.ERROR, "Profile is not public");

      //TODO: verify the intention
      /*
       * if (!crService.isNameUnique(report.header.getName()))
       * addMessage(Severity.WARNING, "The name: " + report.header.getName() +
       * " of the profile is not unique");
       * 
       * if (!crService.isDescriptionUnique(report.header.getDescription()))
       * addMessage(Severity.WARNING, "The description: " +
       * report.header.getDescription() + " of the profile is not unique");
       */
   }

   public Score calculateScore(CMDProfileReport report) {
      double score = report.header.isPublic() ? 1.0 : 0;
      return new Score(score, 1.0, "header-section", this.getMessages());
   }
}
