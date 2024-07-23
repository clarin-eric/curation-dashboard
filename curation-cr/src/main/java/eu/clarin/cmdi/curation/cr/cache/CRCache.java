package eu.clarin.cmdi.curation.cr.cache;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import eu.clarin.cmdi.curation.cr.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDI1_1_ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDI1_2_ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParser;
import eu.clarin.cmdi.curation.cr.xml.SchemaResourceResolver;
import jakarta.annotation.PostConstruct;
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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;

/**
 * The type Cr cache.
 */
@Component
@Slf4j
public class CRCache {
   
   private final SchemaFactory schemaFactory;


   private final HashSet<String> publicSchemaLocations;
   
   @Autowired
   private CRConfig crConfig;


   /**
    * The Ccr service.
    */
   @Autowired
   CCRService ccrService;
   @Autowired
   HttpUtils httpUtils;
   @Autowired
   ApplicationContext ctx;



   /**
    * Instantiates a new Cr cache.
    */
   public CRCache() {
      
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      publicSchemaLocations = new HashSet<>();

   }

   @PostConstruct
   public void loadPublicProfiles() {

      VTDGen vg = new VTDGen();

      vg.parseHttpUrl(crConfig.getRestApi() + crConfig.getQuery(), false);

      VTDNav vn = vg.getNav();

      int i=0;

      try {
         while(i<vn.getTokenCount()){

            if(vn.matchRawTokenString(i, "id")){

               this.publicSchemaLocations.add(crConfig.getRestApi() + "/" + vn.toNormalizedString(++i) + "/xsd");
            }
            i++;
         }
      }
      catch (NavException e) {

         log.error("can't load public profiles from location '{}'", crConfig.getRestApi() + crConfig.getQuery());
      }
   }

   /**
    * Gets public entry.
    *
    * @param schemaLocation the schemaURL
    * @return the public entry
    * @throws NoProfileCacheEntryException the no profile cache entry exception
    */
   @Cacheable(value = "profileCache", key ="#schemaLocation", condition = "#schemaLocation.startsWith('http')", sync = true)
   public ProfileCacheEntry getEntry(String schemaLocation) throws CCRServiceNotAvailableException, CRServiceStorageException {

      String fileName = schemaLocation.replaceAll("[/.:]", "_");

      Path xsd = Paths.get(System.getProperty("java.io.tmpdir"), fileName + ".xsd");

      // if not download it

      try {
         if (Files.notExists(xsd) || Files.size(xsd) == 0) {

            log.debug("XSD for the {} is not in the local cache, it will be downloaded", schemaLocation);

            try(InputStream in = httpUtils.getURLConnection(schemaLocation).getInputStream()) {

               Files.copy(in, xsd, StandardCopyOption.REPLACE_EXISTING);
            }
         }
      }
      catch (MalformedURLException e) {

         log.debug("the schema location URL '{}' is malformed", schemaLocation);


      }
      catch (IOException e) {

         log.debug("can't access xsd file '{}'", xsd);
         throw new CRServiceStorageException(e);
      }

      VTDGen vg = new VTDGen();

      vg.parseFile(xsd.toString(), true);


      VTDNav nv = vg.getNav();

      ParsedProfile parsedProfile = null;

      try {
         for(int i=0; i<nv.getTokenCount(); i++ ){
            if(nv.matchRawTokenString(i, "xmlns:cmd")){
               ProfileParser parser =  switch(nv.toNormalizedString(i+1)){
                  case "http://www.clarin.eu/cmd/1" -> this.ctx.getBean(CMDI1_2_ProfileParser.class);
                  case "http://www.clarin.eu/cmd" -> this.ctx.getBean(CMDI1_1_ProfileParser.class);
                  default -> throw new UnsupportedOperationException();
               };

               parsedProfile = parser.parse(nv, schemaLocation, this.publicSchemaLocations.contains(schemaLocation));

               break;
            }
         }
      }
      catch (NavException e) {

         log.info("can't process schema '{}'", schemaLocation);
      }


      Schema schema = null;
      
      schemaFactory.setResourceResolver(new SchemaResourceResolver());
            
      try {
         schema = schemaFactory.newSchema(xsd.toFile());
      }
      catch (SAXException e) {
         
         log.info("can't create Schema object from file '{}'", xsd);
      }

      return new ProfileCacheEntry(parsedProfile, schema);
   }

   public HashSet<String> getPublicSchemaLocations() {
      return publicSchemaLocations;
   }
}
