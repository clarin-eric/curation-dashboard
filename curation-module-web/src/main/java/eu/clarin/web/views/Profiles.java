package eu.clarin.web.views;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.annotations.Title;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.v7.ui.renderers.HtmlRenderer;
import com.vaadin.v7.ui.renderers.NumberRenderer;

import eu.clarin.web.Shared;
import eu.clarin.web.components.GridPanel;
import eu.clarin.web.components.LinkButton;
import eu.clarin.web.components.ProfileAssessmentForm;

@Title("Profiles")
public class Profiles extends GridPanel {

	private static final long serialVersionUID = 7116025895347108420L;

	private StringBuilder sb;
	
	public Profiles() {
		super();
		
		LinkButton assesmentForm = new LinkButton("Assessment Form");
		assesmentForm.addClickListener(event -> createForm());
		
		sideMenu.addComponent(assesmentForm);
	}
	
	@Override
	protected IndexedContainer createContainer() {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("Id", String.class, null);
		container.addContainerProperty("Name", String.class, null);
		container.addContainerProperty("Score", Double.class, null);
		container.addContainerProperty("FacetCoverage", Double.class, null);
		Shared.facetNames.forEach(facetName -> container.addContainerProperty(facetName, Boolean.class, null));
		container.addContainerProperty("Perc of Elements with Concepts", Double.class, null);
		container.addContainerProperty("SMC", String.class, null);

		// add headers for export data
		sb = new StringBuilder();
		sb.append("Id\tName\tScore\tFacetCoverage\t");
		Shared.facetNames.forEach(facetName -> sb.append(facetName + "\t"));
		sb.append("Perc of Elements with Concepts" + "\n");

		return container;
	}

	@Override
	protected void customRendering() {
		//grid.getColumn("Score").setRenderer(new ProgressBarRenderer()).setExpandRatio(2);
		grid.getColumn("Id").setRenderer(new HtmlRenderer());
		grid.getColumn("SMC").setRenderer(new HtmlRenderer());
		grid.getColumn("Score").setRenderer(new NumberRenderer("%.2f%n"));
		grid.getColumn("FacetCoverage").setRenderer(new NumberRenderer(PERCENTAGE));
		grid.getColumn("Perc of Elements with Concepts").setRenderer(new NumberRenderer(PERCENTAGE));
		
		Shared.facetNames.forEach(facetName -> grid.getColumn(facetName).setExpandRatio(1));
		
		grid.getColumn("SMC").setRenderer(new HtmlRenderer());
	}

	@Override
	protected void fillInData() {
		// add data

		Shared.publicProfiles.forEach(p -> {
			Collection<Object> rowValues = new ArrayList<>();

			rowValues.add("<a href='#!ResultView/profile/id/" + p.getId() + "' target='_top'>" + p.getId() + "</a>");
			rowValues.add(p.getName());
			rowValues.add(p.getScore());
			rowValues.add(p.getFacetCoverage());

			// add Facet coverage for each facet
			p.getFacets().forEach((name, covered) -> rowValues.add(covered));

			rowValues.add(p.getElementsWithConcepts());
			
			
			//Open SMC Browser in new tab
			//rowValues.add("<a href='#!SMC Browser/" + p.getId() + "' target='_top'>explore</a>");
			rowValues.add("<a href='" + SMC.SMC_URL.replace("PROFILEID&",  p.getId().substring(14)) + " ' target='_top'>explore in SMC Browser</a>");			
			

			grid.addRow(rowValues.toArray());

			// csv data

			sb.append(p.getId()).append("\t");
			sb.append(p.getName()).append("\t");
			sb.append(p.getScore()).append("\t");
			sb.append(p.getFacetCoverage()).append("\t");

			// add Facet coverage for each facet
			p.getFacets().forEach((name, covered) -> sb.append(covered).append("\t"));
			sb.append(p.getElementsWithConcepts()).append("\t").append("\n");
		});

		grid.sort("Score", SortDirection.DESCENDING);

		// render background of facet fileds -> red not covered; green covered
		grid.setCellStyleGenerator(cellRef -> {
			if (Shared.facetNames.contains(cellRef.getPropertyId())) {
				return (boolean) cellRef.getValue() ? "facetCovered" : "facetNotCovered";
			} else
				return null;
		});

	}

	@Override
	protected StreamResource generateStreamResource() {
		return new StreamResource(new StreamSource() {

			@Override
			public InputStream getStream() {
				byte[] data = sb.toString().getBytes();
				return new ByteArrayInputStream(data);
			}
		}, "public_profiles_export.tsv");
	}
	
	private void createForm() {
		ProfileAssessmentForm form = new ProfileAssessmentForm();
		getUI().addWindow(form);
	}

}
