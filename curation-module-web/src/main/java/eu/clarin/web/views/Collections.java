package eu.clarin.web.views;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.annotations.Title;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;

import eu.clarin.web.Shared;
import eu.clarin.web.components.GridPanel;

@Title("Collections")
public class Collections extends GridPanel {

	private static final long serialVersionUID = -5552612346775775075L;

	private StringBuilder sb;
	
	public Collections() {
		super();
	}

	@Override
	protected IndexedContainer createContainer() {
		IndexedContainer container = new IndexedContainer();

		container.addContainerProperty("Name", String.class, null);
		container.addContainerProperty("Score", Double.class, null);
		container.addContainerProperty("NumOfRecords", Long.class, null);
		container.addContainerProperty("NumOfProfiles", Integer.class, null);
		container.addContainerProperty("AvgNumOfResProxies", Double.class, null);
		container.addContainerProperty("AvgNumOfEmptyXMLElements", Double.class, null);
		container.addContainerProperty("AvgFacetCoverage", Double.class, null);
		Shared.facetNames.forEach(facetName -> container.addContainerProperty(facetName, Double.class, null));

		sb = new StringBuilder();
		// csv headers
		sb.append("Name").append("\t").append("Score").append("\t").append("NumOfRecords").append("\t")
				.append("NumOfProfiles").append("\t").append("AvgNumOfResProxies").append("\t")
				.append("AvgNumOfEmptyXMLElements").append("\t").append("AvgFacetCoverage").append("\t");
		// facets
		Shared.facetNames.forEach(facetName -> sb.append(facetName + "\t"));
		sb.append("\n");
		return container;
	}

	@Override
	protected void customRendering() {
		grid.getColumn("Name").setRenderer(new HtmlRenderer());		
		Shared.facetNames.forEach(facetName -> grid.getColumn(facetName).setExpandRatio(1));

	}

	@Override
	protected void fillInData() {
		Shared.collections.forEach(c -> {
			Collection<Object> rowValues = new ArrayList<>();

			rowValues.add("<a href='#!ResultView/collection//" + c.fileReport.provider + "' target='_top'>"
					+ c.fileReport.provider + "</a>");
			rowValues.add(c.avgScore);
			rowValues.add(c.fileReport.numOfFiles);
			rowValues.add(c.headerReport.profiles.totNumOfProfiles);
			rowValues.add(c.resProxyReport.avgNumOfResProxies);
			rowValues.add(c.xmlReport.avgXMLEmptyElement);
			rowValues.add(c.facetReport.coverage);

			// add Facet coverage data
			c.facetReport.facet.forEach(facet -> rowValues.add(facet.coverage));

			grid.addRow(rowValues.toArray());

			// csv data
			sb.append(c.fileReport.provider).append("\t");
			sb.append(c.avgScore).append("\t");
			sb.append(c.fileReport.numOfFiles).append("\t");
			sb.append(c.headerReport.profiles.totNumOfProfiles).append("\t");
			sb.append(c.resProxyReport.avgNumOfResProxies).append("\t");
			sb.append(c.xmlReport.avgXMLEmptyElement).append("\t");
			sb.append(c.facetReport.coverage).append("\t");

			// add Facet coverage for each facet
			c.facetReport.facet.forEach(facet -> sb.append(facet.coverage).append("\t"));
			sb.append("\n");
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
		}, "collections_export.tsv");
	}

}
