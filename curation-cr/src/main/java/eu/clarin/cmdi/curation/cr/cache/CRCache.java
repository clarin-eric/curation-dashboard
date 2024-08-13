package eu.clarin.cmdi.curation.cr.cache;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import eu.clarin.cmdi.curation.cr.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.*;
import eu.clarin.cmdi.curation.cr.xml.SchemaResourceResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * The type Cr cache.
 */
@Component
@Slf4j
public class CRCache {
   
   private final SchemaFactory schemaFactory;
   
   @Autowired
   private CRConfig crConfig;
   @Autowired
   private HttpUtils httpUtils;
   @Autowired
   private PPHCache pphCache;
   @Autowired
   private ApplicationContext ctx;



   /**
    * Instantiates a new Cr cache.
    */
   public CRCache() {
      
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
   }

   /**
    * Gets public entry.
    *
    * @param schemaLocation the schemaURL
    * @return the public entry
    * @throws CCRServiceNotAvailableException the ccr service is not available
    */
   @Cacheable(value = "crCache", key ="#schemaLocation", condition = "#schemaLocation.startsWith('http')", sync = true)
   public ProfileCacheEntry getEntry(String schemaLocation) throws CRServiceStorageException, CCRServiceNotAvailableException, PPHCacheException {

      String fileName = schemaLocation.replaceAll("[/.:]", "_");

      Path xsd = crConfig.getCrCache().resolve(fileName + ".xsd");

      // if not download it

      try {
         if (Files.notExists(xsd) || Files.size(xsd) == 0) {

            if(Files.notExists(crConfig.getCrCache())) {

               try {

                  Files.createDirectories(crConfig.getCrCache());
               }
               catch (IOException e) {

                  log.error("could create cr cache directory '{}'", crConfig.getCrCache());
                  throw new CRServiceStorageException(e);
               }
            }

            log.debug("XSD for the {} is not in the local cache, it will be downloaded", schemaLocation);

            try(InputStream in = httpUtils.getURLConnection(schemaLocation).getInputStream()) {

               Files.copy(in, xsd, StandardCopyOption.REPLACE_EXISTING);
            }
         }
      }
      catch (MalformedURLException e) {

         log.debug("the schema location URL '{}' is malformed", schemaLocation);
         return null;
      }
      catch (IOException e) {

         log.debug("can't access xsd file '{}'", schemaLocation);
         return null;
      }



      ParsedProfile parsedProfile = null;

      try {
         VTDGen vg = new VTDGen();

         vg.parseFile(xsd.toString(), true);

         VTDNav nv = vg.getNav();

         for(int i=0; i<nv.getTokenCount(); i++ ){
            if(nv.matchRawTokenString(i, "xmlns:cmd")){
               ProfileParser parser =  switch(nv.toNormalizedString(i+1)){
                  case "http://www.clarin.eu/cmd/1", "https://www.clarin.eu/cmd/1" -> this.ctx.getBean(CMDI1_2_ProfileParser.class);
                  case "http://www.clarin.eu/cmd", "https://www.clarin.eu/cmd" -> this.ctx.getBean(CMDI1_1_ProfileParser.class);
                  default -> throw new UnsupportedOperationException();
               };

               parsedProfile = parser.parse(nv, schemaLocation, this.pphCache.getProfileHeadersMap().get(schemaLocation));

               break;
            }
         }
      }
      catch (NavException | IllegalArgumentException e) {

         log.info("can't process schema '{}'", schemaLocation);
         return null;
      }


      Schema schema = null;
      
      schemaFactory.setResourceResolver(new SchemaResourceResolver());
            
      try {
         schema = schemaFactory.newSchema(xsd.toFile());
      }
      catch (SAXException e) {
         
         log.info("can't create Schema object from file '{}'", xsd);
         return null;
      }

      return new ProfileCacheEntry(parsedProfile, schema);
   }

   public Set<String> getPublicSchemaLocations() throws PPHCacheException {

      return this.pphCache.getProfileHeadersMap().keySet();
   }
}
