/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Collection;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.CMDProfileReport.Component;
import eu.clarin.cmdi.curation.report.CMDProfileReport.Components;
import eu.clarin.cmdi.curation.report.CMDProfileReport.Concepts;
import eu.clarin.cmdi.curation.report.CMDProfileReport.Datcat;
import eu.clarin.cmdi.curation.report.CMDProfileReport.Elements;
import eu.clarin.cmdi.curation.report.Score;

/**
 * @author dostojic
 *
 */
public class ProfileElementsHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

	@Override
	public void process(CMDProfile entity, CMDProfileReport report) throws Exception {
		ParsedProfile parsedProfile;
		try {
			parsedProfile = new CRService().getParsedProfile(report.header);
		} catch (Exception e) {
			throw new Exception("Unable to parse " + entity, e);
		}
		
		report.components = createComponentSegment(parsedProfile);
		report.elements = createElementSegment(parsedProfile);
	}
	
	@Override
	public Score calculateScore(CMDProfileReport report) {
		double score = report.elements.percWithConcept;
		return new Score(score, 1.0, "cmd-concepts-section", msgs);
	}
	
	private Components createComponentSegment(ParsedProfile parsedProfile){
		Collection<CRElement> crComponents = parsedProfile.getComponents();		
		Components comp = new Components();
		comp.total = crComponents.size();
		comp.required = 0;
		comp.unique = 0;
		comp.components = new ArrayList<Component>();
		
		crComponents.forEach(crc -> {
			
			if(crc.isRequired())
				comp.required++;
			
			Component component = comp.components.stream().filter(c -> c.id.equals(crc.getComponentId())).findFirst().orElse(null);
			if(component != null){
				component.count++;
			}else{
				component = new Component();
				component.id = crc.getComponentId();
				component.name = crc.getName();
				component.count = 1;				
				comp.components.add(component);
			}
			
		});
		
		comp.unique = comp.components.size();
		return comp;
	}
	
	private Elements createElementSegment(ParsedProfile parsedProfile){
		Collection<CRElement> crElements = parsedProfile.getElements();
		
		Elements elems = new Elements();		
		elems.total = crElements.size();
		elems.required = 0;
		elems.withConcept = 0;
		elems.concepts = new Concepts();		
		elems.concepts.total = 0;
		elems.concepts.unique = 0;
		elems.concepts.required = 0;
		elems.concepts.concepts = new ArrayList<>();
		
		crElements.forEach(e -> {
			if(e.isRequired())
				elems.required++;
			
			if(e.getConcept() != null){				
				elems.withConcept++;
				
				if(e.isRequired())
					elems.concepts.required++;				
				
				Datcat datcat = elems.concepts.concepts.stream().filter(d -> d.ccr.equals(e.getConcept())).findFirst().orElse(null);
				if(datcat != null){
					datcat.count++;
				}else{
					datcat = new Datcat();
					datcat.ccr = e.getConcept();
					datcat.count = 1;
					elems.concepts.concepts.add(datcat);
				}				
			}
			
		});
				
		
		elems.concepts.total = elems.withConcept;
		elems.concepts.unique = elems.concepts.concepts.size();
		elems.percWithConcept = ((double) elems.withConcept) / elems.total;
		
		return elems;
		
	}

}
