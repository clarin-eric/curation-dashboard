package eu.clarin.cmdi.curation.pph;

import eu.clarin.cmdi.curation.pph.exception.PPHServiceNotAvailableException;

import java.util.Collection;

/**
 * The interface Pph service.
 */
public interface PPHService {

   /**
    * Gets profile header.
    *
    * @param profileId the profile id
    * @return the profile header
    */
   public ProfileHeader getProfileHeader(String profileId) throws PPHServiceNotAvailableException;

   /**
    * Gets profile headers.
    *
    * @return the profile headers
    */
   public Collection<ProfileHeader> getProfileHeaders() throws PPHServiceNotAvailableException;

}
