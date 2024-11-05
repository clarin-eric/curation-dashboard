package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;

import javax.xml.validation.Schema;
import java.io.Serializable;


public record ProfileCacheEntry(
        ParsedProfile parsedProfile,
        Schema schema
) implements Serializable {
}
