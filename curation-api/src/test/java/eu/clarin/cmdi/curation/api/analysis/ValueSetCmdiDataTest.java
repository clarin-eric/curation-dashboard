package eu.clarin.cmdi.curation.api.analysis;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValueSetCmdiDataTest {

   private ValueSetCmdiData data;

   @BeforeEach
   void setup() {

      final FieldNameService fieldNames = mock(FieldNameService.class);
      when(fieldNames.getFieldName(FieldKey.ID)).thenReturn("id");
      data = new ValueSetCmdiData(fieldNames);
   }

   @Test
   void synthesisesValueSetsForValuesAddedByName() {

      data.addDocField("some_facet", "some value", false);

      final List<ValueSet> values = data.getDocument().get("some_facet");
      assertEquals(1, values.size());
      assertEquals("some value", values.get(0).getValue());
      assertEquals(-1, values.get(0).getVtdIndex(), "no source position for synthesised values");
      assertEquals(List.of("some value"), List.copyOf(data.getDocField("some_facet")));
      assertTrue(data.hasField("some_facet"));

      data.removeField("some_facet");
      assertFalse(data.hasField("some_facet"));
      assertNull(data.getDocField("some_facet"), "absent facet reads as null, not empty");
   }

   /**
    * Same behaviour as VLO's Solr implementation.
    */
   @Test
   void addsIfNullOnlyToAnAbsentFacet() {

      data.addDocField("some_facet", "first", false);
      final ValueSet first = data.getDocument().get("some_facet").getFirst();

      data.addDocFieldIfNull(first, false);
      assertEquals(1, data.getDocument().get("some_facet").size(), "present facet is left alone");

      data.replaceDocField(first, false);
      assertEquals(List.of("first"), List.copyOf(data.getDocField("some_facet")), "replace keeps the value");

      data.removeField("some_facet");
      data.addDocFieldIfNull(first, false);
      assertTrue(data.hasField("some_facet"), "absent facet is added");
   }
}
