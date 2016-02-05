/**
 * 
 */
package eu.clarin.cmdi.curation.component_registry;

import java.util.Collection;

import javax.xml.validation.Schema;

import eu.clarin.cmdi.curation.entities.CMDIProfile;

/**
 * @author dostojic
 *
 */
public interface IComponentRegistryService {

    public CMDIProfile getProfile(final String profile) throws Exception;

    public Collection<CMDIProfile> getPublicProfiles() throws Exception;

    public Schema getSchema(final String profile) throws Exception;

    //public void downloadAllPublicSchemas(Collection<String> profileIds) throws Exception;
}
