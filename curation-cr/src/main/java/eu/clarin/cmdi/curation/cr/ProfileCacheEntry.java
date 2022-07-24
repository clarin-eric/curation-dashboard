package eu.clarin.cmdi.curation.cr;

import javax.xml.validation.Schema;

import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.Data;

@Data
public class ProfileCacheEntry {
   
   private final ParsedProfile parsedProfile;
   
   private final Schema schema;
}
