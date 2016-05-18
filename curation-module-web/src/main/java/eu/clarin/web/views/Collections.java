package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.renderers.HtmlRenderer;

import eu.clarin.web.Shared;

@DesignRoot
public class Collections extends VerticalLayout implements View {

	Grid grid;

	public Collections() {
		Design.read(this);

		grid.addColumn("Name", String.class);
		grid.addColumn("AvgScore", Double.class);
		grid.addColumn("NumOfRecords", Long.class);
		grid.addColumn("Size in bytes", Long.class);
		grid.addColumn("AvgSize", Long.class);
		grid.addColumn("NumOfProfiles", Integer.class);
		grid.addColumn("AvgNumOfResProxies", Double.class);
		grid.addColumn("AvgNumOfXMLElements", Double.class);
		grid.addColumn("AvgNumOfEmptyXMLElements", Double.class);
		grid.addColumn("AvgRateOfXMLPopulation", Double.class);
		grid.addColumn("AvgFacetCoverage", Double.class);

		Shared.collections.forEach(c -> grid.addRow(
			"<a href='#!ResultView/collection/" + c.fileReport.provider + "' target='_top'>" + c.fileReport.provider + "</a>",
			c.avgScore, c.fileReport.numOfFiles,
			c.fileReport.size, c.fileReport.avgSize, c.headerReport.profiles.totNumOfProfiles,
			c.resProxyReport.avgNumOfResProxies, c.xmlReport.avgNumOfXMLElements, c.xmlReport.avgXMLEmptyElement,
			c.xmlReport.avgRateOfPopulatedElements, c.facetReport.avgFacetCoverageByInstance)
		);
		
		Grid.Column collection = grid.getColumn("Name");
		collection.setRenderer(new HtmlRenderer());
		
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// nothing to do

	}

}
