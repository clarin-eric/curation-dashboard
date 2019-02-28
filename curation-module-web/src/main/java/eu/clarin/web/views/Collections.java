package eu.clarin.web.views;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.v7.ui.renderers.NumberRenderer;
import eu.clarin.web.components.LinkButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Title;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.v7.ui.renderers.HtmlRenderer;

import eu.clarin.web.Shared;
import eu.clarin.web.components.GridPanel;

@Title("Collections")
public class Collections extends GridPanel {

    private static final long serialVersionUID = -5552612346775775075L;
    private static final Logger _logger = LoggerFactory.getLogger(Collections.class);
    
    
    

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
        container.addContainerProperty("NoOfUniqueLinks", Integer.class, null);
        container.addContainerProperty("NoOfCheckedLinks", Integer.class, null);
        container.addContainerProperty("RatioOfValidLinks", Double.class, null);
        container.addContainerProperty("AvgNumOfResProxies", Double.class, null);
        container.addContainerProperty("NumOfResProxies", Integer.class, null);
        container.addContainerProperty("RatioOfValidRecords", Double.class, null);
        container.addContainerProperty("AvgNumOfEmptyXMLElements", Double.class, null);
        container.addContainerProperty("AvgFacetCoverage", Double.class, null);
        Shared.facetNames.forEach(facetName -> container.addContainerProperty(facetName, Double.class, null));

        sb = new StringBuilder();
        // csv headers
        sb.append("Name").append("\t")
                .append("Score").append("\t")
                .append("NumOfRecords").append("\t")
                .append("NumOfProfiles").append("\t")
                .append("NoOfUniqueLinks").append("\t")
                .append("NoOfCheckedLinks").append("\t")
                .append("RatioOfValidLinks").append("\t")
                .append("AvgNumOfResProxies").append("\t")
                .append("NumOfResProxies").append("\t")
                .append("RatioOfValidRecords").append("\t")
                .append("AvgNumOfEmptyXMLElements").append("\t")
                .append("AvgFacetCoverage").append("\t");
        // facets
        Shared.facetNames.forEach(facetName -> sb.append(facetName + "\t"));
        sb.append("\n");
        return container;
    }

    @Override
    protected void customRendering() {

        
        grid.getColumn("Name").setRenderer(new HtmlRenderer());

        Shared.facetNames.forEach(facetName -> grid.getColumn(facetName).setExpandRatio(1));

        grid.getColumn("Score").setRenderer(new NumberRenderer("%.4f%n"));
        grid.getColumn("NumOfRecords").setRenderer(new NumberRenderer("%d%n"));
        grid.getColumn("NumOfProfiles").setRenderer(new NumberRenderer("%d%n"));
        grid.getColumn("NoOfUniqueLinks").setRenderer(new NumberRenderer("%d%n"));
        grid.getColumn("NoOfCheckedLinks").setRenderer(new NumberRenderer("%d%n"));
        grid.getColumn("RatioOfValidLinks").setRenderer(new NumberRenderer(PERCENTAGE));
        grid.getColumn("RatioOfValidRecords").setRenderer(new NumberRenderer(PERCENTAGE));
        grid.getColumn("AvgNumOfResProxies").setRenderer(new NumberRenderer("%.4f%n"));
        grid.getColumn("AvgNumOfEmptyXMLElements").setRenderer(new NumberRenderer("%.4f%n"));
        grid.getColumn("AvgFacetCoverage").setRenderer(new NumberRenderer("%.4f%n"));
        
        Shared.facetNames.forEach(facetName -> grid.getColumn(facetName).setRenderer(new NumberRenderer(PERCENTAGE)));

    }

    @Override
    protected void fillInData() {
        Shared.collectionStatistics.forEach(c -> {
            try {
                Collection<Object> rowValues = new ArrayList<>();

                rowValues.add("<a href='#!ResultView/collection//" + c.getFileReport_provider()+ "' target='_top'>"
                        + c.getFileReport_provider()+ "</a>");
                rowValues.add(c.getScorePercentage());
                rowValues.add(c.getFileReport_numOfFiles());
                rowValues.add(c.getHeaderReport_profiles_totNumOfProfiles());
                rowValues.add(c.getUrlReport_totNumOfUniqueLinks());
                rowValues.add(c.getUrlReport_totNumOfCheckedLinks());
                rowValues.add(c.getUrlReport_ratioOfValidLinks());
                rowValues.add(c.getResProxyReport_avgNumOfResProxies());
                rowValues.add(c.getResProxyReport_totNumOfResProxies());
                rowValues.add(c.getXmlValidationReport_ratioOfValidRecords());
                rowValues.add(c.getXmlPopulatedReport_avgXMLEmptyElement());
                rowValues.add(c.getFacetReport_coverage());

                // add Facet coverage data
                c.getFacetReport_facet().forEach(facet -> rowValues.add(facet));

                grid.addRow(rowValues.toArray());

                // csv data
                sb.append(c.getFileReport_provider()).append("\t");
                sb.append(c.getScorePercentage()).append("\t");
                sb.append(c.getFileReport_numOfFiles()).append("\t");
                sb.append(c.getHeaderReport_profiles_totNumOfProfiles()).append("\t");
                sb.append(c.getUrlReport_totNumOfUniqueLinks()).append("\t");
                sb.append(c.getUrlReport_totNumOfCheckedLinks()).append("\t");
                sb.append(c.getUrlReport_ratioOfValidLinks()).append("\t");
                sb.append(c.getResProxyReport_avgNumOfResProxies()).append("\t");
                sb.append(c.getResProxyReport_totNumOfResProxies()).append("\t");
                sb.append(c.getXmlValidationReport_ratioOfValidRecords()).append("\t");
                sb.append(c.getXmlPopulatedReport_avgXMLEmptyElement()).append("\t");
                sb.append(c.getFacetReport_coverage()).append("\t");

                // add Facet coverage for each facet
                c.getFacetReport_facet().forEach(facet -> sb.append(facet).append("\t"));
                sb.append("\n");
            } catch (Exception e) {

                _logger.warn("Can't read report for provider " + c.getFileReport_provider(), e);
            }
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
