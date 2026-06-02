package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIDataBaseImpl;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.AllArgsConstructor;
import lombok.Data;
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
      facetValuesMap = new HashMap<>();
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
         addValueSet(valueSet, caseInsensitive);
      }
   }

   /**
    * Add doc field.
    *
    * @param facetName       the field name
    * @param value           the value
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void addDocField(String facetName, Object value, boolean caseInsensitive) {

      addValueSet(facetName, value, caseInsensitive);
   }

   /**
    * Add doc field if null.
    *
    * @param valueSet        the value set
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {

      if(this.facetValuesMap.containsKey(valueSet.getTargetFacetName())){

         addDocField(valueSet, caseInsensitive);
      }
   }

   /**
    * Gets doc field.
    *
    * @param facetName the name of the field/facet
    * @return the doc field
    */
   @Override
   public Collection<Object> getDocField(String facetName) {

      return (hasField(facetName)? this.facetValuesMap.get(facetName).stream().map(ValueSet::getValue).collect(Collectors.toList()) : null);
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

      removeField(valueSet.getTargetFacetName());

      addDocFieldIfNull(valueSet, caseInsensitive);
   }

   /**
    * Replace doc field.
    *
    * @param facetName            the name
    * @param value           the value
    * @param caseInsensitive the case insensitive
    */
   @Override
   public void replaceDocField(String facetName, Object value, boolean caseInsensitive) {

      removeField(facetName);

      addValueSet(facetName, value, caseInsensitive);
   }

   /**
    * Remove field.
    *
    * @param facetName the name
    */
   @Override
   public void removeField(String facetName) {

      this.facetValuesMap.remove(facetName);
   }

   /**
    * Has field boolean.
    *
    * @param facetName the name
    * @return the boolean
    */
   @Override
   public boolean hasField(String facetName) {

      return this.facetValuesMap.containsKey(facetName);
   }

   /**
    * Gets field values.
    *
    * @param facetName the name
    * @return the field values
    */
   @Override
   public Collection<Object> getFieldValues(String facetName) {

      return getDocField(facetName);
   }


   private void addValueSet(ValueSet valueSet, boolean caseInsensitive){

      if(caseInsensitive){

         valueSet.setValue(valueSet.getValueLanguagePair().getLeft().trim().toLowerCase());
      }
      this.facetValuesMap.computeIfAbsent(valueSet.getTargetFacetName(), list -> new ArrayList<ValueSet>())
              .add(valueSet);
   }

   private void addValueSet(String facetName, Object value, boolean caseInsensitive){
      addValueSet(
         new ValueSet(-1, new FacetDefinition(null, "unknown"),
                 new TargetFacet(new FacetDefinition(null, facetName), value.toString()),
                 Pair.of(value.toString(), DEFAULT_LANGUAGE), false, false),
         caseInsensitive
      );
   }
}
