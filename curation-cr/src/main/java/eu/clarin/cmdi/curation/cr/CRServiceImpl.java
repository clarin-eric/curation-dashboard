package eu.clarin.cmdi.curation.cr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.validation.Schema;

import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.pph.exception.PPHServiceNotAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.cr.cache.CRCache;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CRServiceImpl implements CRService {

   public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
   public static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
   public static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);

   private final CRConfig props;

   public final Pattern CR_REST_PATTERN;

   @Autowired
   PPHService pphService;
   @Autowired
   CRCache crCache;

   /*
    * Profile is considered to be public if schema resides in Component Registry
    * and the ID is in the list of public profiles
    */

   @Autowired
   public CRServiceImpl(CRConfig props) {

      this.props = props;

      String CR_REST = props.getRestApi().replaceFirst("http(s)?", "(http|https)").replaceFirst("/1\\..+", "/.+");

      this.CR_REST_PATTERN = Pattern.compile(CR_REST);

      if (!Files.exists(props.getXsdCache(), LinkOption.NOFOLLOW_LINKS)) {
         try {
            Files.createDirectories(props.getXsdCache());
         }
         catch (IOException e) {

            log.error("can't create xsd_cache directory {}", props.getXsdCache());

         }
      }
   }

   @Override
   public ProfileHeader createProfileHeader(String schemaLocation, String cmdiVersion, boolean isLocalFile) {
      ProfileHeader header = null;
      if (!isLocalFile && schemaLocation.startsWith(props.getRestApi())) {
         try {
            header = pphService.getProfileHeader(getIdFromSchemaLocation(schemaLocation));
         }
         catch (PPHServiceNotAvailableException e) {
            throw new RuntimeException(e);
         }
      }

      if (header == null) {
         header = new ProfileHeader();
         header.setSchemaLocation(schemaLocation);
         header.setId(getIdFromSchemaLocation(schemaLocation));
         header.setCmdiVersion(cmdiVersion);
         header.setPublic(false);

         if (header.getId() == null) { // when the id can't be extracted from the schema location we have to get it
                                       // from the file content
            CharBuffer buffer = CharBuffer.allocate(1000);

            InputStreamReader reader;
            try(InputStream in = new URL(schemaLocation).openStream()) {
               reader = new InputStreamReader(in);
               reader.read(buffer);
               String content = buffer.rewind().toString();

               Matcher matcher = PROFILE_ID_PATTERN.matcher(content);

               if (matcher.find())
                  header.setId(matcher.group());

               if (!content.contains("http://www.clarin.eu/cmd/1"))
                  header.setCmdiVersion("1.1");
            }
            catch (MalformedURLException ex) {
               log.error("Schema location " + schemaLocation + " is no valid URL.");
            }
            catch (IOException ex) {
               log.error("Couldn't read from schema location: " + schemaLocation);
            }
         }
      }
      header.setLocalFile(isLocalFile);
      return header;
   }

   @Override
   public ParsedProfile getParsedProfile(ProfileHeader header) throws NoProfileCacheEntryException, CRServiceStorageException {

      return getEntry(header).getParsedProfile();

   }

   private synchronized ProfileCacheEntry getEntry(ProfileHeader header) throws NoProfileCacheEntryException, CRServiceStorageException {
      
      if(header.getId() == null || header.getId().isEmpty()) {
         header.setId(createProfileHeader(header.getSchemaLocation(), header.getCmdiVersion(), header.isLocalFile()).getId());
      }

      return (crCache.isPublicCache(header) ? crCache.getPublicEntry(header) : crCache.getPrivateEntry(header));

   }

   @Override
   public Schema getSchema(ProfileHeader header) throws NoProfileCacheEntryException, CRServiceStorageException {

      return getEntry(header).getSchema();

   }

   @Override
   public boolean isSchemaCRResident(String schemaLocation) {
      return CR_REST_PATTERN.matcher(schemaLocation).matches();
   }

   public String getIdFromSchemaLocation(String schemaLocation) {
      Matcher matcher = PROFILE_ID_PATTERN.matcher(schemaLocation);

      return matcher.find() ? matcher.group() : null;
   }
}
