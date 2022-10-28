package eu.clarin.cmdi.curation.pph;

import java.util.Collection;

public interface PPHService {
   
   public ProfileHeader getProfileHeader(String id);
   
   public Collection<ProfileHeader> getProfileHeaders();

}
