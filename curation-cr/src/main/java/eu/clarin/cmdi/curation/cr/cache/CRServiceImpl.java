package eu.clarin.cmdi.curation.cr.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.CCRServiceFactory;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.PublicProfiles;
import eu.clarin.cmdi.curation.cr.conf.CRProperties;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParserFactory;
import eu.clarin.cmdi.curation.xml.SchemaResourceResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CRServiceImpl implements CRService {
   
   public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
   public static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
   public static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);

   private final CRProperties crProps;
   private final CCRService ccrService;

   public final Pattern CR_REST_PATTERN;



   private final Collection<ProfileHeader> publicProfiles;

   /*
    * Profile is considered to be public if schema resides in Component Registry
    * and the ID is in the list of public profiles
    */
   
   private final SchemaFactory schemaFactory;
   
   @Autowired
   public CRServiceImpl(CRProperties crProps, CCRServiceFactory ccrServiceFac) {
      
      this.crProps = crProps;
      this.ccrService = ccrServiceFac.getCCRService();
      
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new SchemaResourceResolver());   
      
      String CR_REST = crProps.getCrRestUrl().replaceFirst("http(s)?", "(http|https)")
            .replaceFirst("/1\\..+", "/.+");
      
      this.CR_REST_PATTERN = Pattern.compile(CR_REST);
      
      if(!Files.exists(crProps.getXsdCache(), LinkOption.NOFOLLOW_LINKS)) {
         try {
            Files.createDirectories(crProps.getXsdCache());
         }
         catch (IOException e) {
            
            log.error("can't create xsd_cache directory {}", crProps.getXsdCache());

         }
      }
      
      publicProfiles = PublicProfiles.createPublicProfiles(crProps.getCrRestUrl(), crProps.getCrQuery());
   }
   
   private synchronized Schema createSchema(File schemaFile) throws SAXException {
      return schemaFactory.newSchema(schemaFile);
   }

   @Override
   public ProfileHeader createProfileHeader(String schemaLocation, String cmdiVersion, boolean isLocalFile) {
      ProfileHeader header = null;
      if (!isLocalFile && schemaLocation.startsWith(crProps.getCrRestUrl()))
         header = publicProfiles.stream().filter(h -> schemaLocation.contains(h.getId())).findFirst().orElse(null);

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
            try {
               reader = new InputStreamReader(new URL(schemaLocation).openStream());
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
   public ParsedProfile getParsedProfile(ProfileHeader header) throws ExecutionException, IOException, URISyntaxException, VTDException, SAXException {
      
      return (isPublicCache(header)? getPublicEntry(header):getPrivateEntry(header)).getParsedProfile();
      
   }
   
   private boolean isPublicCache(ProfileHeader header) {
      return header.isPublic() && header.getCmdiVersion().equals("1.x") || header.getCmdiVersion().equals("1.2");
   }

   @Override
   public Schema getSchema(ProfileHeader header) throws ExecutionException, IOException, URISyntaxException, VTDException, SAXException {
      return (isPublicCache(header)? getPublicEntry(header):getPrivateEntry(header)).getSchema();
   }

   @Override
   public boolean isNameUnique(String name) {
      return publicProfiles.stream().filter(h -> h.getName().equals(name)).count() <= 1;
   }

   @Override
   public boolean isDescriptionUnique(String description) {
      return publicProfiles.stream().filter(h -> h.getName().equals(description)).count() <= 1;
   }

   @Override
   public boolean isSchemaCRResident(String schemaLocation) {
      return CR_REST_PATTERN.matcher(schemaLocation).matches();
   }

   @Override
   public Collection<ProfileHeader> getPublicProfiles() {
      return publicProfiles;
   }

   public String getIdFromSchemaLocation(String schemaLocation) {
      Matcher matcher = PROFILE_ID_PATTERN.matcher(schemaLocation);

      return matcher.find() ? matcher.group() : null;
   }
   
   @Cacheable("publicProfile")
   private void getCacheEntry(ProfileHeader header) {
      
   }
   
   private static void getXSD(ProfileHeader header, Path xsd) throws URISyntaxException, IOException {
      if (header.getSchemaLocation().startsWith("file:")) {
         Files.move(Paths.get(new URI(header.getSchemaLocation())), xsd, StandardCopyOption.REPLACE_EXISTING);
      }
      else {
         FileUtils.copyURLToFile(new URL(header.getSchemaLocation()), xsd.toFile());
      }
   }
   
   @Cacheable("publicProfileCache")
   private ProfileCacheEntry getPublicEntry(ProfileHeader header) throws IOException, URISyntaxException, VTDException, SAXException {
      return getProfileCacheEntry(header);
   }
   
   @Cacheable("privateProfileCache")
   private ProfileCacheEntry getPrivateEntry(ProfileHeader header) throws IOException, URISyntaxException, VTDException, SAXException {
      return getProfileCacheEntry(header);
   }
   
   private ProfileCacheEntry getProfileCacheEntry(ProfileHeader header) throws IOException, URISyntaxException, VTDException, SAXException {
      Path xsd;
      if (isPublicCache(header)) {

         // String fileName = header.id.substring(CRService.PROFILE_PREFIX.length());
         String fileName = header.getSchemaLocation().replaceAll("[/.:]", "_");
         xsd = crProps.getXsdCache().resolve(fileName + ".xsd");
         // try to load it from the disk

         log.info("profile {} is public. Loading schema from {}", header.getId(), xsd);

         if (!Files.exists(xsd)) {// keep public profiles on disk
            // if not download it
            Files.createFile(xsd);

            log.debug("XSD for the {} is not in the local cache, it will be downloaded", header.getSchemaLocation());

            FileUtils.copyURLToFile(new URL(header.getSchemaLocation()), new File(header.getSchemaLocation()));
         }

      }
      else {// non-public profiles are not cached on disk

         log.debug("schema {} is not public. Schema will be downloaded in temp folder", header.getSchemaLocation());

         // keep private schemas on disk

         // String fileName = header.id.substring(CRService.PROFILE_PREFIX.length());
         String fileName = header.getSchemaLocation().replaceAll("[/.:]", "_");
         xsd = crProps.getXsdCache().resolve("private_profiles");
         xsd = xsd.resolve(fileName + ".xsd");
         // try to load it from the disk

         log.debug("Loading schema for non public profile {} from {}", header.getSchemaLocation(), xsd);

         if (!Files.exists(xsd)) {
            // if not download it
            Files.createFile(xsd);

            log.debug("XSD for the {} is not in the local cache, it will be downloaded", header.getId());

            getXSD(header, xsd);
         }
      }

      VTDGen vg = new VTDGen();

      if (Files.readAllBytes(xsd).length == 0) {
         Files.deleteIfExists(xsd);
         getXSD(header, xsd);
         if (Files.readAllBytes(xsd).length == 0) {
            throw new VTDException("xsd path is empty");
         }
      }
      vg.setDoc(Files.readAllBytes(xsd));
      vg.parse(true);

      ProfileParser parser = ProfileParserFactory.createParser(header.getCmdiVersion(), ccrService);

      ParsedProfile parsedProfile = parser.parse(vg.getNav(), header);
      Schema schema = createSchema(xsd.toFile());

      // facetMapping

      return new ProfileCacheEntry(parsedProfile, schema);
      
   }   
}
