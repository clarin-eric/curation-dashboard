package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIDataBaseImpl;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults.DEFAULT_LANGUAGE;

/**
 * The type Cmdi data.
 */
/*
 * Generally the CMDIData object contains all the data read by VTD from a certain CMDI file which will stored in the solr database. 
 * This curation implementation of the CMDIData object stores some additional in formation in the object which we need only in curation, like
 * the VTD-index of the origin value, the information whether the facet is derived, whether it uses value mapping, the origin- and the target
 * facet name 
 * 
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class CMDIDataImpl extends CMDIDataBaseImpl<Map<String, List<ValueSet>>> {

   private final Map<String, List<ValueSet>> facetValuesMap;

   /**
    * Instantiates a new Cmdi data.
    *
    * @param fieldNameService the field name service
    */
   public CMDIDataImpl(FieldNameService fieldNameService) {
      super(fieldNameService);
      facetValuesMap = new HashMap<String, List<ValueSet>>();
   }

   /**
    * Add doc field.
    *
    * @param valueSet        the value set
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void addDocField(ValueSet valueSet, boolean caseInsensitive) {

      final String fieldName = valueSet.getTargetFacetName();
      final String value = valueSet.getValue();

      if (fieldNameService.getFieldName(FieldKey.ID).equals(fieldName)) {
         setId(value.trim());
      }
      else {
         
         this.facetValuesMap.computeIfAbsent(valueSet.getTargetFacetName(), list -> new ArrayList<ValueSet>())
               .add(valueSet);
      }
   }

   /**
    * Add doc field.
    *
    * @param fieldName       the field name
    * @param value           the value
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void addDocField(String fieldName, Object value, boolean caseInsensitive) {

      this.facetValuesMap.computeIfAbsent(fieldName, list -> new ArrayList<ValueSet>()).add(

            new ValueSet(-1, new FacetDefinition(null, "unknown"),
                  new TargetFacet(new FacetDefinition(null, fieldName), value.toString()),
                  Pair.of(value.toString(), DEFAULT_LANGUAGE), false, false));
   }

   /**
    * Add doc field if null.
    *
    * @param valueSet        the value set
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {

      this.facetValuesMap.putIfAbsent(valueSet.getTargetFacetName(), List.of(valueSet));
   }

   /**
    * Gets doc field.
    *
    * @param name the name
    * @return the doc field
    */
   @Override
   public Collection<Object> getDocField(String name) {

      Collection<ValueSet> valueSetList;

      return (valueSetList = this.facetValuesMap.get(name)) == null ? null
            : valueSetList.stream().map(ValueSet::getValue).collect(Collectors.toList());
   }

   /**
    * Gets document.
    *
    * @return the document
    */
   @Override
   public Map<String, List<ValueSet>> getDocument() {
      return this.facetValuesMap;
   }

   /**
    * Replace doc field.
    *
    * @param valueSet        the value set
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
      this.facetValuesMap.put(valueSet.getTargetFacetName(), Arrays.asList(valueSet));

   }

   /**
    * Replace doc field.
    *
    * @param name            the name
    * @param value           the value
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void replaceDocField(String name, Object value, boolean caseInsensitive) {

   }

   /**
    * Remove field.
    *
    * @param name the name
    */
   @Override
   public void removeField(String name) {
      this.facetValuesMap.remove(name);

   }

   /**
    * Has field boolean.
    *
    * @param name the name
    * @return the boolean
    */
   @Override
   public boolean hasField(String name) {
      return this.facetValuesMap.containsKey(name);
   }

   /**
    * Gets field values.
    *
    * @param name the name
    * @return the field values
    */
   @Override
   public Collection<Object> getFieldValues(String name) {

      return this.facetValuesMap.get(name).stream().map(Object.class::cast).collect(Collectors.toList());
   }

}
