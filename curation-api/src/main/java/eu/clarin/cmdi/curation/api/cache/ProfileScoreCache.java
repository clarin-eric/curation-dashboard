package eu.clarin.cmdi.curation.api.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProfileScoreCache {
   
   @Autowired
   ApplicationContext ctx;
   
   @Cacheable(value = "profileScores", condition = "#header.isPublic")
   public Double getScore(ProfileHeader header) {
      
      CMDProfile profile = ctx.getBean(CMDProfile.class,header.getSchemaLocation(), header.getCmdiVersion());       
      log.trace("Calculating and caching score for {}", profile);
      
      try {
         CMDProfileReport report;
         report = profile.generateReport();
         return report.score;
      }
      catch (Exception ex) {
         
         log.error("", ex);
      
      }
      
      return Double.NaN;
      
   }

}
