package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.cache.CRCache;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.validation.Schema;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
public class CRServiceImpl implements CRService {

    public static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
    public static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);

    private final CRConfig crConfig;
    private final CRCache crCache;



    /*
     * Profile is considered to be public if schema resides in Component Registry
     * and the ID is in the list of public profiles
     */

    public CRServiceImpl(CRConfig crConfig, CRCache crCache) {

        this.crConfig = crConfig;

        this.crCache = crCache;
    }




    @Override
    public ProfileHeader createProfileHeader(String schemaLocation) throws CRServiceStorageException, CCRServiceNotAvailableException, PPHCacheException, NoCRCacheEntryException {

        return getParsedProfile(schemaLocation).header();
    }

    @Override
    public ParsedProfile getParsedProfile(String schemaLocation) throws CRServiceStorageException, CCRServiceNotAvailableException, PPHCacheException, NoCRCacheEntryException {

        if(crCache.getEntry(schemaLocation) == null){
            throw new NoCRCacheEntryException();
        }

        return crCache.getEntry(schemaLocation).parsedProfile();
    }

    @Override
    public Schema getSchema(String schemaLocation) throws CCRServiceNotAvailableException, CRServiceStorageException, PPHCacheException, NoCRCacheEntryException {

        if(crCache.getEntry(schemaLocation) == null){
            throw new NoCRCacheEntryException();
        }
        return crCache.getEntry(schemaLocation).schema();
    }

    @Override
    public boolean isPublicSchema(String schemaLocation) throws PPHCacheException {

        return this.crCache.getPublicSchemaLocations().contains(schemaLocation);
    }

    @Override
    public Stream<String> getPublicSchemaLocations() throws PPHCacheException {

        return this.crCache.getPublicSchemaLocations().stream();
    }
    @Override
    public String getIdFromSchemaLocation(String schemaLocation) {
        Matcher matcher = PROFILE_ID_PATTERN.matcher(schemaLocation);

        return matcher.find() ? matcher.group() : null;
    }
}
