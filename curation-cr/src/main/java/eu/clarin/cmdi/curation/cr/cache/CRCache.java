package eu.clarin.cmdi.curation.cr.cache;

import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.*;
import eu.clarin.cmdi.curation.cr.xml.SchemaResourceResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.Objects;
import java.util.Set;

/**
 * The type Cr cache.
 */
@Component
@Slf4j
public class CRCache {

   private final ProfileCache profileCache;

   private final PPHCache pphCache;

   private final ApplicationContext ctx;

    public CRCache(ProfileCache profileCache, PPHCache pphCache, ApplicationContext ctx) {
        this.profileCache = profileCache;
        this.pphCache = pphCache;
        this.ctx = ctx;
    }

    /**
    * Gets public entry.
    *
    * @param schemaLocation the schemaURL
    * @return the public entry
    * @throws CCRServiceNotAvailableException the ccr service is not available
    */
   @Cacheable(value = "crCache", key = "#schemaLocation", sync = true)
   public ProfileCacheEntry getEntry(String schemaLocation) throws CRServiceStorageException, CCRServiceNotAvailableException, PPHCacheException {

      String schemaString = this.profileCache.getProfileAsString(schemaLocation);

      if(Objects.isNull(schemaString)){

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

      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new SchemaResourceResolver());

      Schema schema = null;

      try {
         schema = schemaFactory.newSchema(new StreamSource(new StringReader(schemaString)));
      }
      catch (SAXException e) {

         log.info("can't create Schema object from file '{}'", schemaLocation);
         return null;
      }

      return new ProfileCacheEntry(parsedProfile, schema);
   }

   public Set<String> getPublicSchemaLocations() throws PPHCacheException {

      return this.pphCache.getProfileHeadersMap().keySet();
   }
}
