package eu.clarin.cmdi.curation.vlo_extensions;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.commons.collections.CollectionUtils;

import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

/*
 * This is an extension of the FacetMapping class which is only necessary in the curation module. The goal of the extension is 
 * to add some facets to the key/values map in the CMDIData object which are required for processing but usually not defined in 
 * the facetMapping.xml
 * 
 * Currently these are the attributes schemaLocation and noNamespaceSchemaLocation which are processed to investigate the profileId but not
 * stored in the CMDIData object. Further on the value of the mdProfile element, where only the post-processed value is stored in the 
 * CMDIData object. 
 * 
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class FacetMappingExt extends FacetMapping {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final LinkedHashMap<String, FacetConfiguration> _facetsMapExt = new LinkedHashMap<String, FacetConfiguration>();
    
    FacetMapping facetMapping;
    

    static {
        FacetConfiguration facetConfig; 
        
        facetConfig = new FacetConfiguration(null, "curation_schemaLocation");
        facetConfig.setPattern(new Pattern("/cmd:CMD/@*[local-name()='schemaLocation']"));
        _facetsMapExt.put("curation_schemaLocation", facetConfig);
        
        facetConfig = new FacetConfiguration(null, "curation_noNamespaceSchemaLocation");
        facetConfig.setPattern(new Pattern("/cmd:CMD/@*[local-name()='noNamespaceSchemaLocation']"));
        _facetsMapExt.put("curation_noNamespaceSchemaLocation", facetConfig);       
        
        facetConfig = new FacetConfiguration(null, "curation_mdProfile");
        facetConfig.setPattern(new Pattern("/cmd:CMD/cmd:Header/cmd:MdProfile/text()"));
        _facetsMapExt.put("curation_mdProfile", facetConfig);       
        
        
    }
    
    public FacetMappingExt(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Collection<FacetConfiguration> getFacetConfigurations() {

        return CollectionUtils.union(this.facetMapping.getFacetConfigurations(), _facetsMapExt.values());

    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getFacetConfigurationNames() {
        return CollectionUtils.union(this.facetMapping.getFacetConfigurationNames(), _facetsMapExt.keySet());
    }

    @Override
    public FacetConfiguration getFacetConfiguration(String facetName) {
        return _facetsMapExt.containsKey(facetName)? _facetsMapExt.get(facetName):this.facetMapping.getFacetConfiguration(facetName);
    }

}
