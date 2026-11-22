/*
 * Copyright (C) 2026 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.curation.api.analysis;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataBaseImpl;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A {@link CMDIData} that keeps the full {@link ValueSet} of every facet value,
 * including its VTD index and whether it was derived or value-mapped. VLO's Solr
 * implementation keeps only the value.
 */
class ValueSetCmdiData extends CMDIDataBaseImpl<Map<String, List<ValueSet>>> {

    /**
     * Origin facet name recorded for values added by facet name rather than by
     * {@link ValueSet}, where the true origin is not known.
     */
    static final String UNKNOWN_ORIGIN_FACET = "unknown";

    /**
     * VTD index for a value that has no position in the source document.
     */
    private static final int NO_VTD_INDEX = -1;

    private final Map<String, List<ValueSet>> facetValuesMap = new HashMap<>();

    ValueSetCmdiData(FieldNameService fieldNameService) {
        super(fieldNameService);
    }

    @Override
    public void addDocField(ValueSet valueSet, boolean caseInsensitive) {
        final String facetName = valueSet.getTargetFacetName();
        if (fieldNameService.getFieldName(FieldKey.ID).equals(facetName)) {
            setId(valueSet.getValue().trim());
        } else {
            addValueSet(valueSet, caseInsensitive);
        }
    }

    @Override
    public void addDocField(String facetName, Object value, boolean caseInsensitive) {
        addValueSet(facetName, value, caseInsensitive);
    }

    @Override
    public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
        if (!this.facetValuesMap.containsKey(valueSet.getTargetFacetName())) {
            addDocField(valueSet, caseInsensitive);
        }
    }

    @Override
    public Collection<Object> getDocField(String facetName) {
        final List<ValueSet> valueSets = this.facetValuesMap.get(facetName);
        return valueSets == null
                ? null
                : valueSets.stream().map(ValueSet::getValue).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<ValueSet>> getDocument() {
        return this.facetValuesMap;
    }

    @Override
    public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
        removeField(valueSet.getTargetFacetName());
        addDocFieldIfNull(valueSet, caseInsensitive);
    }

    @Override
    public void replaceDocField(String facetName, Object value, boolean caseInsensitive) {
        removeField(facetName);
        addValueSet(facetName, value, caseInsensitive);
    }

    @Override
    public void removeField(String facetName) {
        this.facetValuesMap.remove(facetName);
    }

    @Override
    public boolean hasField(String facetName) {
        return this.facetValuesMap.containsKey(facetName);
    }

    @Override
    public Collection<Object> getFieldValues(String facetName) {
        return getDocField(facetName);
    }

    private void addValueSet(ValueSet valueSet, boolean caseInsensitive) {
        if (caseInsensitive) {
            valueSet.setValue(valueSet.getValueLanguagePair().getLeft().trim().toLowerCase());
        }
        this.facetValuesMap
                .computeIfAbsent(valueSet.getTargetFacetName(), name -> new ArrayList<>())
                .add(valueSet);
    }

    private void addValueSet(String facetName, Object value, boolean caseInsensitive) {
        // create a synthetic ValueSet
        if (value == null) {
            return;
        }
        final String stringValue = value.toString();
        final FacetDefinition noOrigin = new FacetDefinition(null, UNKNOWN_ORIGIN_FACET);
        final TargetFacet target = new TargetFacet(new FacetDefinition(null, facetName), stringValue);

        addValueSet(
                new ValueSet(NO_VTD_INDEX, noOrigin, target,
                        Pair.of(stringValue, LanguageDefaults.DEFAULT_LANGUAGE),
                        false, false),
                caseInsensitive);
    }
}
