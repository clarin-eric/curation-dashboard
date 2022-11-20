package eu.clarin.cmdi.curation.pph;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.clarin.cmdi.curation.pph.cache.PPHCache;
import eu.clarin.cmdi.curation.pph.conf.PPHConfig;

@Service
public class PPHServiceImpl implements PPHService {

   @Autowired
   PPHConfig props;
   @Autowired
   PPHCache pphCache;


   @Override
   public Collection<ProfileHeader> getProfileHeaders() {

      return pphCache.getProfileHeadersMap(props.getRestApi(), props.getQuery()).values();

   }


   @Override
   public ProfileHeader getProfileHeader(String id) {
      
      return pphCache.getProfileHeadersMap(props.getRestApi(), props.getQuery()).get(id);
   
   }
}
