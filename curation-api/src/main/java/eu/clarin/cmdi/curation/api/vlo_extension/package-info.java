/*
 * Supplies the curation module's facets mappings.
 *
 * It holds one FacetsMappingProvider implementation, which caches mappings per
 * schema location and adds the curation_* facets that VLO does not define in
 * facetConcepts.xml but which are needed by the header checks in the CMDIData object.
 */
package eu.clarin.cmdi.curation.api.vlo_extension;
