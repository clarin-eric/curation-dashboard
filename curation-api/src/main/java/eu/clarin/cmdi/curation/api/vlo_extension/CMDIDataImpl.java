package eu.clarin.cmdi.curation.api.vlo_extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIDataBaseImpl;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

import static eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults.DEFAULT_LANGUAGE;

/*
 * Generally the CMDIData object contains all the data read by VTD from a certain CMDI file which will stored in the solr database. 
 * This curation implementation of the CMDIData object stores some additional in formation in the object which we need only in curation, like
 * the VTD-index of the origin value, the information whether the facet is derived, whether it uses value mapping, the origin- and the target
 * facet name 
 * 
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class CMDIDataImpl extends CMDIDataBaseImpl<Map<String,List<ValueSet>>> {
    private final Map<String,List<ValueSet>> facetValuesMap;

    public CMDIDataImpl(FieldNameService fieldNameService) {
        super(fieldNameService);
        facetValuesMap = new HashMap<String,List<ValueSet>>();
    }

    @Override
    public void addDocField(ValueSet valueSet, boolean caseInsensitive) {
       
        this.facetValuesMap.computeIfAbsent(valueSet.getTargetFacetName(), list -> new ArrayList<ValueSet>()).add(valueSet);         
    }

    @Override
    public void addDocField(String fieldName, Object value, boolean caseInsensitive) {

       this.facetValuesMap.computeIfAbsent(fieldName, list -> new ArrayList<ValueSet>()).add(
             
             new ValueSet(
                   -1, 
                   new FacetDefinition(null, "unknown"),
                   new TargetFacet(new FacetDefinition(null, fieldName), value.toString()),
                   Pair.of(value.toString(), DEFAULT_LANGUAGE),
                   false,
                   false
                )
          );  
    }

    @Override
    public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
       
       this.facetValuesMap.putIfAbsent(valueSet.getTargetFacetName(), List.of(valueSet));
    }

    @Override
    public Collection<Object> getDocField(String name) {

        return null;
    }

    @Override
    public Map<String, List<ValueSet>> getDocument() {
        return this.facetValuesMap;
    }

    @Override
    public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
        this.facetValuesMap.put(valueSet.getTargetFacetName(), Arrays.asList(valueSet));
        
    }

    @Override
    public void replaceDocField(String name, Object value, boolean caseInsensitive) {

        
    }

    @Override
    public void removeField(String name) {
        this.facetValuesMap.remove(name);
        
    }

    @Override
    public boolean hasField(String name) {
        return this.facetValuesMap.containsKey(name);
    }

    @Override
    public Collection<Object> getFieldValues(String name) {

        return this.facetValuesMap.get(name).stream().map(Object.class::cast).collect(Collectors.toList());
    }

}
