package eu.clarin.cmdi.curation.api.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProfileScoreCache {
   
   @Cacheable(value = "profileScores", condition = "#header.isPublic")
   public Double getScore(ProfileHeader header) {
      
      CMDProfile profile = new CMDProfile(header.getSchemaLocation(), header.getCmdiVersion());       
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
