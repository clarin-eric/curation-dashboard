/**
 * 
 */
package eu.clarin.cmdi.curation.cr;

import java.io.File;

import javax.xml.validation.Schema;

/**
 * @author dostojic
 *
 */
public interface IComponentRegistryService {
    
    public ProfileSpec getProfile(final String profile) throws Exception;
    
    public boolean isPublic(final String profile) throws Exception;
    
    public Schema getSchema(final String profile) throws Exception;

    public File getLocalFile(String profile) throws Exception;
}
