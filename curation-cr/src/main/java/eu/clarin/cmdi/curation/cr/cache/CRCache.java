package eu.clarin.cmdi.curation.cr.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.cr.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.conf.CRProperties;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParserFactory;
import eu.clarin.cmdi.curation.cr.xml.SchemaResourceResolver;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CRCache {
   
   private final SchemaFactory schemaFactory;
   
   @Autowired
   private CRProperties props;
   @Autowired
   CCRService ccrService;
   @Autowired
   PPHService pphService;
   
   public CRCache() {
      
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
   }
   
   @Cacheable(value = "publicProfileCache", key = "#header.id")
   public ProfileCacheEntry getPublicEntry(ProfileHeader header) throws NoProfileCacheEntryException {
      return getProfileCacheEntry(header);
   }
   
   @Cacheable(value = "privateProfileCache", key = "#header.id")
   public ProfileCacheEntry getPrivateEntry(ProfileHeader header) throws NoProfileCacheEntryException {
      return getProfileCacheEntry(header);
   }
   
   private ProfileCacheEntry getProfileCacheEntry(ProfileHeader header) throws NoProfileCacheEntryException {
      
      String fileName = header.getSchemaLocation().replaceAll("[/.:]", "_");
      
      Path xsd = props.getXsdCache()
                  .resolve(isPublicCache(header)?"":"private_profiles")
                  .resolve(fileName + ".xsd");
      

      // try to load it from the disk

      log.info("profile {} is public. Loading schema from {}", header.getId(), xsd);

      
         // if not download it

      try {
         if (Files.notExists(xsd) || Files.size(xsd) == 0 || !isPublicCache(header)) {// keep public profiles on disk
            // create directories if they don't exist
            if(Files.notExists(xsd.getParent())) {
               Files.createDirectories(xsd.getParent());
            }
            
            Files.createFile(xsd);
            log.debug("XSD for the {} is not in the local cache, it will be downloaded", header.getSchemaLocation());

            if (header.getSchemaLocation().startsWith("file:")) {
               Files.move(Paths.get(new URI(header.getSchemaLocation())), xsd, StandardCopyOption.REPLACE_EXISTING);
            }
            else {
               FileUtils.copyURLToFile(new URL(header.getSchemaLocation()), xsd.toFile());
            }
         }
      }
      catch (MalformedURLException e) {
         
         log.debug("the schema location URL '{}' is malformed", header.getSchemaLocation());
         throw new NoProfileCacheEntryException();
         
      }
      catch (IOException e) {
         
         log.debug("can't access xsd file '{}'", xsd);
         throw new CRServiceStorageException(e);
      }
      catch (URISyntaxException e) {
         
         log.debug("can't copy URI '{}' is malformed", header.getSchemaLocation());
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




      ProfileParser parser = ProfileParserFactory.createParser(header.getCmdiVersion(), ccrService);

      ParsedProfile parsedProfile = parser.parse(vg.getNav(), header);
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

   
   public boolean isPublicCache(ProfileHeader header) {
      return header.isPublic() && header.getCmdiVersion().equals("1.x") || header.getCmdiVersion().equals("1.2");
   }  
}
