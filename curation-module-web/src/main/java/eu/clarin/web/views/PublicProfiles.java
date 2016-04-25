package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.renderers.HtmlRenderer;

import eu.clarin.web.Shared;

@DesignRoot
@Title("Curation Module - Public Profiles")
public class PublicProfiles extends VerticalLayout implements View {

	Grid grid;

	public PublicProfiles() {
		Design.read(this);

		grid.addColumn("Id", String.class);
		grid.addColumn("Name", String.class);
		grid.addColumn("Score", Double.class);
		grid.addColumn("FacetCoverage", Double.class);
		grid.addColumn("Perc of Elements with Concepts", Double.class);
		grid.addColumn("Details", String.class);
		grid.addColumn("SMC", String.class);
		
		Column c = grid.getColumn("");

		Shared.publicProfiles.forEach(
				p -> grid.addRow(p.getId(), p.getName(), p.getScore(), p.getFacetCoverage(), p.getElementsWithConcepts(),
				"<a href='#!ResultView/profile/" + p.getId() + "' target='_top'>see details</a>",
				"<a href='#!SMC Browser/" + p.getId() + "' target='_top'>explore</a>")
		);
		grid.setColumnOrder("Id", "Name", "Score", "FacetCoverage", "Perc of Elements with Concepts", "Details", "SMC");
		grid.sort("Score", SortDirection.DESCENDING);

		Grid.Column link = grid.getColumn("Details");
		link.setRenderer(new HtmlRenderer());
		
		Grid.Column smc = grid.getColumn("SMC");
		smc.setRenderer(new HtmlRenderer());
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}
