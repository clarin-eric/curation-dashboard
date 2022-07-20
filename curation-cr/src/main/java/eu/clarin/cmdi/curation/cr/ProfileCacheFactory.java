package eu.clarin.cmdi.curation.cr;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.cr.conf.CRProperties;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParserFactory;
import eu.clarin.cmdi.curation.xml.SchemaResourceResolver;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

@Slf4j
class ProfileCacheFactory {

   static final long HOUR_IN_NS = 3600000000000L;

   private static final SchemaFactory schemaFactory;

   @Autowired
   private static CRProperties crProps;

   static {
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new SchemaResourceResolver());
   }

   public static LoadingCache<ProfileHeader, ProfileCacheEntry> createProfileCache(boolean isPublicProfilesCache) {
      return isPublicProfilesCache ?

            CacheBuilder.newBuilder().concurrencyLevel(4).build(new ProfileCacheLoader(isPublicProfilesCache))
            : CacheBuilder.newBuilder().concurrencyLevel(4)
                  // .expireAfterAccess(5, TimeUnit.MINUTES)//keep non public profiles 5 minutes
                  // in cache
                  .expireAfterWrite(8, TimeUnit.HOURS)// keep non public profiles 8 hours in cache
                  .ticker(new Ticker() {
                     @Override
                     public long read() {
                        return 9 * HOUR_IN_NS;
                     }
                  }) // cache tick 9 hours

                  .build(new ProfileCacheLoader(isPublicProfilesCache));
   }

   private static synchronized Schema createSchema(File schemaFile) throws SAXException {
      return schemaFactory.newSchema(schemaFile);
   }

   private static class ProfileCacheLoader extends CacheLoader<ProfileHeader, ProfileCacheEntry> {

      final boolean isPublicProfilesCache;

      public ProfileCacheLoader(boolean isPublicProfilesCache) {
         this.isPublicProfilesCache = isPublicProfilesCache;
      }

      @Override
      public ProfileCacheEntry load(ProfileHeader header)
            throws IOException, VTDException, SAXException, URISyntaxException {

         Path xsd;
         if (isPublicProfilesCache) {

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

         ProfileParser parser = ProfileParserFactory.createParser(header.getCmdiVersion());

         ParsedProfile parsedProfile = parser.parse(vg.getNav(), header);
         Schema schema = createSchema(xsd.toFile());

         // facetMapping

         return new ProfileCacheEntry(parsedProfile, schema);
      }

   }

   private static void getXSD(ProfileHeader header, Path xsd) throws URISyntaxException, IOException {
      if (header.getSchemaLocation().startsWith("file:")) {
         Files.move(Paths.get(new URI(header.getSchemaLocation())), xsd, StandardCopyOption.REPLACE_EXISTING);
      }
      else {
         FileUtils.copyURLToFile(new URL(header.getSchemaLocation()), xsd.toFile());
      }
   }

   static class ProfileCacheEntry {
      ParsedProfile parsedProfile;
      Schema schema;

      public ProfileCacheEntry(ParsedProfile parsedProfile, Schema schema) {
         this.parsedProfile = parsedProfile;
         this.schema = schema;
      }
   }

}
