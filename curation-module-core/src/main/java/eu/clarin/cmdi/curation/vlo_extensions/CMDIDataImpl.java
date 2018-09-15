package eu.clarin.cmdi.curation.vlo_extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataBaseImpl;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

/*
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<Object> getDocField(String name) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        
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
