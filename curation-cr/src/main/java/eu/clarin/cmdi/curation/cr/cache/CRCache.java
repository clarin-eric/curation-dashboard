package eu.clarin.cmdi.curation.cr.cache;

import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import eu.clarin.cmdi.curation.cr.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParserFactory;
import eu.clarin.cmdi.curation.cr.xml.SchemaResourceResolver;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * The type Cr cache.
 */
@Component
@Slf4j
public class CRCache {
   
   private final SchemaFactory schemaFactory;
   
   @Autowired
   private CRConfig props;
   /**
    * The Ccr service.
    */
   @Autowired
   CCRService ccrService;
   /**
    * The Pph service.
    */
   @Autowired
   PPHService pphService;

   @Autowired
   HttpUtils httpUtils;

   /**
    * Instantiates a new Cr cache.
    */
   public CRCache() {
      
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
   }

   /**
    * Gets public entry.
    *
    * @param profileHeader the profile header
    * @return the public entry
    * @throws NoProfileCacheEntryException the no profile cache entry exception
    */
   @Cacheable(value = "publicProfileCache", key = "#profileHeader.id")
   public ProfileCacheEntry getPublicEntry(ProfileHeader profileHeader) throws NoProfileCacheEntryException, CRServiceStorageException {
      return getProfileCacheEntry(profileHeader);
   }

   /**
    * Gets private entry.
    *
    * @param profileHeader the profile header
    * @return the private entry
    * @throws NoProfileCacheEntryException the no profile cache entry exception
    */
   @Cacheable(value = "privateProfileCache", key = "#profileHeader.id", condition = "#profileHeader.reliable")
   public ProfileCacheEntry getPrivateEntry(ProfileHeader profileHeader) throws NoProfileCacheEntryException, CRServiceStorageException {
      return getProfileCacheEntry(profileHeader);
   }
   
   private ProfileCacheEntry getProfileCacheEntry(ProfileHeader profileHeader) throws NoProfileCacheEntryException, CRServiceStorageException {
      
      String fileName = profileHeader.getSchemaLocation().replaceAll("[/.:]", "_");
      
      Path xsd = props.getXsdCache()
                  .resolve(isPublicCache(profileHeader)?"":"private_profile")
                  .resolve(fileName + ".xsd");
      

      // try to load it from the disk

      log.info("profile {} is public. Loading schema from {}", profileHeader.getId(), xsd);

      
         // if not download it

      try {
         if (Files.notExists(xsd) || Files.size(xsd) == 0 || !isPublicCache(profileHeader)) {// keep public profiles on disk
            // create directories if they don't exist
            if(Files.notExists(xsd.getParent())) {
               Files.createDirectories(xsd.getParent());
            }

            log.debug("XSD for the {} is not in the local cache, it will be downloaded", profileHeader.getSchemaLocation());

            if (profileHeader.getSchemaLocation().startsWith("file:")) {
               Files.copy(Paths.get(new URI(profileHeader.getSchemaLocation())), xsd, StandardCopyOption.REPLACE_EXISTING);
            }
            else {
               try(InputStream in = httpUtils.getURLConnection(profileHeader.getSchemaLocation()).getInputStream()){

                  Files.copy(in, xsd, StandardCopyOption.REPLACE_EXISTING);
               }
            }
         }
      }
      catch (MalformedURLException e) {
         
         log.debug("the schema location URL '{}' is malformed", profileHeader.getSchemaLocation());
         throw new NoProfileCacheEntryException();
         
      }
      catch (IOException e) {
         
         log.debug("can't access xsd file '{}'", xsd);
         throw new CRServiceStorageException(e);
      }
      catch (URISyntaxException e) {
         
         log.debug("can't copy URI '{}' is malformed", profileHeader.getSchemaLocation());
         throw new NoProfileCacheEntryException();
      }



      VTDGen vg = new VTDGen();


      try {
         vg.setDoc(Files.readAllBytes(xsd));

         vg.parse(true);


      }
      catch (IOException e) {
         
         log.error("can't read file '{}'", xsd);
         throw new CRServiceStorageException(e);
      
      }
      catch (ParseException e) {
         
         log.error("can't parse file '{}'", xsd);
         throw new NoProfileCacheEntryException();
         
      }




      ProfileParser parser = ProfileParserFactory.createParser(profileHeader.getCmdiVersion(), ccrService);

      ParsedProfile parsedProfile = parser.parse(vg.getNav(), profileHeader);
      Schema schema = null;
      
      schemaFactory.setResourceResolver(new SchemaResourceResolver());
            
      try {
         schema = schemaFactory.newSchema(xsd.toFile());
      }
      catch (SAXException e) {
         
         log.error("can't create Schema object from file '{}'", xsd);
         throw new NoProfileCacheEntryException(); 
      
      }


      return new ProfileCacheEntry(parsedProfile, schema);
      
   }


   /**
    * Is public cache boolean.
    *
    * @param profileHeader the header
    * @return the boolean
    */
   public boolean isPublicCache(ProfileHeader profileHeader) {
      return profileHeader.isPublic() && profileHeader.isReliable() && (profileHeader.getCmdiVersion().equals("1.x") || profileHeader.getCmdiVersion().equals("1.2"));
   }  
}
