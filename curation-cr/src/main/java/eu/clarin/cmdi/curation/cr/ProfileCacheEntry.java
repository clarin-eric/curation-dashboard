package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.Data;

import javax.xml.validation.Schema;

@Data
public class ProfileCacheEntry {
   
   private final ParsedProfile parsedProfile;
   
   private final Schema schema;
}
