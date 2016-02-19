///**
// * 
// */
//package eu.clarin.cmdi.curation.subprocessor;
//
//import java.text.DecimalFormat;
//import java.util.LinkedList;
//import java.util.List;
//
//import eu.clarin.cmdi.curation.entities.CMDIProfile;
//import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
//import eu.clarin.cmdi.curation.facets.Profile2FacetMap;
//import eu.clarin.cmdi.curation.report.Message;
//import eu.clarin.cmdi.curation.report.Severity;
//
///**
// * @author dostojic
// *
// */
//public class ProfileFacetCoverage implements ProcessingStep<CMDIProfile, ?> {
//
//    @Override
//    public Severity process(CMDIProfile profile) {
//	try {
//	    FacetConceptMappingService facetService = FacetConceptMappingService.getInstance();
//	    Profile2FacetMap map = facetService.getMapping(profile.getId());
//
//	    List<String> uncoveredFacets = new LinkedList<>();
//	    for (String facet : facetService.getFacetConceptsNames())
//		if (!map.getFacetNames().contains(facet))
//		    uncoveredFacets.add(facet);
//
//	    int numOfFacets = facetService.getFacetConceptsNames().size();
//
//	    StringBuilder sb = new StringBuilder(2000);
//	    int numOfCoveredFacets = map.getMappings().size();
//	    double facetCoverage = numOfCoveredFacets * 1.0 / numOfFacets * 100;
//
//	    sb.append("Facet Coverage - Profile: ").append(profile.getId());
//	    sb.append("\n");
//	    sb.append("\t").append(numOfCoveredFacets).append("/").append(numOfFacets).append(", ")
//		    .append(new DecimalFormat("0.00").format(facetCoverage)).append("%");
//	    sb.append("\n");
//	    sb.append("\t").append("Covered facets: ").append(String.join(", ", map.getFacetNames()));
//	    sb.append("\n");
//	    sb.append("\t").append("Uncovered facets: ").append(String.join(", ", uncoveredFacets));
//
//	    // report.addMessage(sb.toString());
//
//	    profile.setFacetCoverage(facetCoverage);
//	    profile.setCoveredFacets(map.getFacetNames());
//	    return Severity.NONE;
//
//	} catch (Exception e) {
//	    profile.addDetail(Severity.FATAL, e.getMessage());
//	    return Severity.FATAL;
//	}
//    }
//
//}
