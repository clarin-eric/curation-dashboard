package eu.clarin.cmdi.curation.cr.cache;

import com.ximpleware.NavException;
import com.ximpleware.ParseException;
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
import java.io.*;
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
   @Autowired
   private CRConfig crConfig;
   @Autowired
   private HttpUtils httpUtils;
   @Autowired
   private PPHCache pphCache;
   @Autowired
   private ApplicationContext ctx;

   /**
    * Gets public entry.
    *
    * @param schemaLocation the schemaURL
    * @return the public entry
    * @throws CCRServiceNotAvailableException the ccr service is not available
    */
   @Cacheable(value = "crCache", key ="#schemaLocation", sync = true)
   public ProfileCacheEntry getEntry(String schemaLocation) throws CRServiceStorageException, CCRServiceNotAvailableException, PPHCacheException {

      String schemaString;

      try(BufferedReader reader = new BufferedReader(new InputStreamReader(httpUtils.getURLConnection(schemaLocation).getInputStream()))) {

         StringBuffer buffer = new StringBuffer();

         reader.lines().forEach(buffer::append);
         schemaString = buffer.toString();
      }
      catch (IOException e) {

         log.error("couldn't get schema '{}'", schemaLocation);
         return null;
      }


      ParsedProfile parsedProfile = null;

      try {
         VTDGen vg = new VTDGen();

         vg.setDoc(schemaString.getBytes());
         vg.parse(true);

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
      catch (NavException | IllegalArgumentException | ParseException e) {

         log.info("can't process schema '{}'", schemaLocation);
         return null;
      }

//      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//      schemaFactory.setResourceResolver(new SchemaResourceResolver());
//
//      Schema schema = null;
//
//      try {
//         schema = schemaFactory.newSchema(xsd.toFile());
//      }
//      catch (SAXException e) {
//
//         log.info("can't create Schema object from file '{}'", xsd);
//         return null;
//      }

      return new ProfileCacheEntry(parsedProfile, schemaString);
   }

   public Set<String> getPublicSchemaLocations() throws PPHCacheException {

      return this.pphCache.getProfileHeadersMap().keySet();
   }
}
