package eu.clarin.cmdi.curation.cr;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.cache.CRCache;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.validation.Schema;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
public class CRServiceImpl implements CRService {

    public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
    public static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
    public static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);
    public final Pattern CR_REST_PATTERN;

    private final CRConfig crConfig;
    private final CRCache crCache;



    /*
     * Profile is considered to be public if schema resides in Component Registry
     * and the ID is in the list of public profiles
     */

    public CRServiceImpl(CRConfig crConfig, CRCache crCache) {

        this.crConfig = crConfig;

        this.crCache = crCache;

        String CR_REST = crConfig.getRestApi().replaceFirst("http(s)?", "(http|https)").replaceFirst("/1\\..+", "/.+");

        this.CR_REST_PATTERN = Pattern.compile(CR_REST);
    }




    @Override
    public ProfileHeader createProfileHeader(String schemaLocation, boolean isCacheable) throws NoProfileCacheEntryException, CRServiceStorageException, CCRServiceNotAvailableException {

        return this.crCache.getEntry(schemaLocation, isCacheable).getParsedProfile().header();
    }

    @Override
    public ParsedProfile getParsedProfile(String schemaLocation, boolean isCacheable) throws NoProfileCacheEntryException, CCRServiceNotAvailableException, CRServiceStorageException {

        return crCache.getEntry(schemaLocation, isCacheable).getParsedProfile();

    }

    @Override
    public Schema getSchema(String schemaLocation, boolean isCacheable) throws NoProfileCacheEntryException, CCRServiceNotAvailableException, CRServiceStorageException {

        return crCache.getEntry(schemaLocation, isCacheable).getSchema();
    }

    @Override
    public boolean isSchemaCRResident(String schemaLocation) {
        return CR_REST_PATTERN.matcher(schemaLocation).matches();
    }

    @Override
    public Stream<String> getPublicSchemaLocations() {

        return this.crCache.getPublicSchemaLocations().stream();
    }

    public String getIdFromSchemaLocation(String schemaLocation) {
        Matcher matcher = PROFILE_ID_PATTERN.matcher(schemaLocation);

        return matcher.find() ? matcher.group() : null;
    }
}
