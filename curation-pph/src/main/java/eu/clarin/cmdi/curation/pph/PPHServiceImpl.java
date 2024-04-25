package eu.clarin.cmdi.curation.pph;

import eu.clarin.cmdi.curation.pph.cache.PPHCache;
import eu.clarin.cmdi.curation.pph.conf.PPHConfig;
import eu.clarin.cmdi.curation.pph.exception.PPHServiceNotAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * The type Pph service.
 */
@Service
public class PPHServiceImpl implements PPHService {

   /**
    * The Props.
    */
   @Autowired
   PPHConfig props;
   /**
    * The Pph cache.
    */
   @Autowired
   PPHCache pphCache;


   /**
    * Gets profile headers.
    *
    * @return the profile headers
    */
   @Override
   public Collection<ProfileHeader> getProfileHeaders() throws PPHServiceNotAvailableException {

      return pphCache.getProfileHeadersMap(props.getRestApi(), props.getQuery()).values();

   }


   /**
    * Gets profile header.
    *
    * @param profileId the profile id
    * @return the profile header identified by the given profile id
    */
   @Override
   public ProfileHeader getProfileHeader(String profileId) throws PPHServiceNotAvailableException {
      
      return pphCache.getProfileHeadersMap(props.getRestApi(), props.getQuery()).get(profileId);
   
   }
}
