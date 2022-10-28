package eu.clarin.cmdi.curation.pph;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.clarin.cmdi.curation.pph.cache.PPHCache;
import eu.clarin.cmdi.curation.pph.conf.PHProperties;

@Service
public class PPHServiceImpl implements PPHService {

   @Autowired
   PHProperties props;
   @Autowired
   PPHCache pphCache;


   @Override
   public Collection<ProfileHeader> getProfileHeaders() {

      return pphCache.getProfileHeadersMap(props.getCrRestUrl(), props.getCrQuery()).values();

   }


   @Override
   public ProfileHeader getProfileHeader(String id) {
      
      return pphCache.getProfileHeadersMap(props.getCrRestUrl(), props.getCrQuery()).get(id);
   
   }
}
