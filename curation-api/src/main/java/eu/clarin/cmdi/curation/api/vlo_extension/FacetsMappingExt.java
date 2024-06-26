package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * The type Facets mapping ext.
 */
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
public class FacetsMappingExt extends FacetsMapping {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final LinkedHashMap<String, FacetDefinition> _facetsMapExt = new LinkedHashMap<String, FacetDefinition>();
    
    private FacetsMapping facetsMapping;
    

    static {
        FacetDefinition facetConfig; 
        
        facetConfig = new FacetDefinition(null, "curation_schemaLocation");
        facetConfig.setPattern(new Pattern("/cmd:CMD/@*[local-name()='schemaLocation']"));
        _facetsMapExt.put("curation_schemaLocation", facetConfig);
        
        facetConfig = new FacetDefinition(null, "curation_noNamespaceSchemaLocation");
        facetConfig.setPattern(new Pattern("/cmd:CMD/@*[local-name()='noNamespaceSchemaLocation']"));
        _facetsMapExt.put("curation_noNamespaceSchemaLocation", facetConfig);       
        
        facetConfig = new FacetDefinition(null, "curation_mdProfile");
        facetConfig.setPattern(new Pattern("/cmd:CMD/cmd:Header/cmd:MdProfile/text()"));
        _facetsMapExt.put("curation_mdProfile", facetConfig);       
    }

    /**
     * Instantiates a new Facets mapping ext.
     *
     * @param facetsMapping the facets mapping
     */
    public FacetsMappingExt(FacetsMapping facetsMapping) {
       super(facetsMapping.getFacetsConfigurations());
       this.facetsMapping = facetsMapping;
    }


    /**
     * Gets facet definitions.
     *
     * @return the facet definitions
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<FacetDefinition> getFacetDefinitions() {

        return CollectionUtils.union(this.facetsMapping.getFacetDefinitions(), _facetsMapExt.values());
    }

    /**
     * Gets facet configuration names.
     *
     * @return the facet configuration names
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getFacetConfigurationNames() {
        return CollectionUtils.union(this.facetsMapping.getFacetConfigurationNames(), _facetsMapExt.keySet());
    }

    /**
     * Gets facet definition.
     *
     * @param facetName the facet name
     * @return the facet definition
     */
    @Override
    public FacetDefinition getFacetDefinition(String facetName) {
        return _facetsMapExt.containsKey(facetName)? _facetsMapExt.get(facetName):this.facetsMapping.getFacetDefinition(facetName);
    }
}
