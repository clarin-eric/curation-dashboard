package eu.clarin.web.views;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

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
    private static final Logger logger = LoggerFactory.getLogger(Collections.class);

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

    }

    @Override
    protected void fillInData() {
        Shared.collections.forEach(c -> {
            try {
                Collection<Object> rowValues = new ArrayList<>();

                rowValues.add("<a href='#!ResultView/collection//" + c.fileReport.provider + "' target='_top'>"
                        + c.fileReport.provider + "</a>");
                rowValues.add(c.scorePercentage);
                rowValues.add(c.fileReport.numOfFiles);
                rowValues.add(c.headerReport.profiles.totNumOfProfiles);
                rowValues.add(c.urlReport.totNumOfUniqueLinks);
                rowValues.add(c.urlReport.totNumOfCheckedLinks);
                rowValues.add(c.urlReport.ratioOfValidLinks);
                rowValues.add(c.resProxyReport.avgNumOfResProxies);
                rowValues.add(c.resProxyReport.totNumOfResProxies);
                rowValues.add(c.xmlValidationReport.ratioOfValidRecords);
                rowValues.add(c.xmlPopulatedReport.avgXMLEmptyElement);
                rowValues.add(c.facetReport.coverage);

                // add Facet coverage data
                c.facetReport.facet.forEach(facet -> rowValues.add(facet.coverage));

                grid.addRow(rowValues.toArray());

                // csv data
                sb.append(c.fileReport.provider).append("\t");
                sb.append(c.scorePercentage).append("\t");
                sb.append(c.fileReport.numOfFiles).append("\t");
                sb.append(c.headerReport.profiles.totNumOfProfiles).append("\t");
                sb.append(c.urlReport.totNumOfUniqueLinks).append("\t");
                sb.append(c.urlReport.totNumOfCheckedLinks).append("\t");
                sb.append(c.urlReport.ratioOfValidLinks).append("\t");
                sb.append(c.resProxyReport.avgNumOfResProxies).append("\t");
                sb.append(c.resProxyReport.totNumOfResProxies).append("\t");
                sb.append(c.xmlValidationReport.ratioOfValidRecords).append("\t");
                sb.append(c.xmlPopulatedReport.avgXMLEmptyElement).append("\t");
                sb.append(c.facetReport.coverage).append("\t");

                // add Facet coverage for each facet
                c.facetReport.facet.forEach(facet -> sb.append(facet.coverage).append("\t"));
                sb.append("\n");
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("can't read report for provider " + c.fileReport.provider);
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
