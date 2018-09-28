package eu.clarin.web.views;

import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

import eu.clarin.cmdi.curation.entities.CurationEntity.CurationEntityType;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.web.MainUI;
import eu.clarin.web.Shared;
import eu.clarin.web.components.LinkButton;
import eu.clarin.web.utils.LinkCheckerStatisticsHelper;
import eu.clarin.web.utils.XSLTTransformer;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

@DesignRoot
public class ResultView extends Panel implements View {

    private enum SourceType {PROFILE_ID, URL, FILE}

    private VerticalLayout sideMenu;

    Label label;
    StreamResource xmlReport;


    XSLTTransformer transformer = new XSLTTransformer();

    public ResultView() {
        setSizeFull();
        label = new Label();
        label.setContentMode(ContentMode.HTML);

        setContent(label);

        xmlReport = new StreamResource(null, "report.xml");
        xmlReport.setMIMEType("application/xml");
        xmlReport.setCacheTime(0);

        sideMenu = new VerticalLayout();
        LinkButton export = new LinkButton("Download Report (xml)");
        sideMenu.addComponent(export);

        FileDownloader fileDownloader = new FileDownloader(xmlReport);

        fileDownloader.extend(export);

//		BrowserWindowOpener popup = new BrowserWindowOpener(xmlPopulatedReport);
//		popup.setFeatures("");
//		popup.extend(bXML);	

    }

    @Override
    public void enter(ViewChangeEvent event) {
        int first = event.getParameters().indexOf('/');
        int sec = event.getParameters().indexOf('/', first + 1);

        String curationType = event.getParameters().substring(0, first);
        String source = event.getParameters().substring(first + 1, sec);
        String value = event.getParameters().substring(sec + 1);

        SourceType sourceType = null;
        switch (source) {
            case "id":
                sourceType = SourceType.PROFILE_ID;
                break;
            case "url":
                sourceType = SourceType.URL;
                break;
            case "file":
                sourceType = SourceType.FILE;
                break;
        }

        switch (curationType) {
            case "instance":
                curate(CurationEntityType.INSTANCE, sourceType, value);
                break;
            case "profile":
                curate(CurationEntityType.PROFILE, sourceType, value);
                break;
            case "collection":
                curate(CurationEntityType.COLLECTION, sourceType, value);
                break;
            case "statistics":
                curate(CurationEntityType.STATISTICS, sourceType, value);
                break;
        }

        ((MainUI) getUI()).setCustomMenu(sideMenu);
    }


    private void curate(CurationEntityType curationType, SourceType sourceType, String input) {
        Report r = null;
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            switch (curationType) {
                case INSTANCE:
                    switch (sourceType) {
                        case FILE:
                            Path cmdiFile = Paths.get(System.getProperty("java.io.tmpdir"), input);
                            r = new CurationModule().processCMDInstance(cmdiFile);
                            if (r instanceof CMDInstanceReport)
                                ((CMDInstanceReport) r).fileReport.location = cmdiFile.getFileName().toString();
                            Files.delete(cmdiFile);
                            break;
                        case URL:
                            r = new CurationModule().processCMDInstance(new URL(input));
                            break;
                    }

                    r.toXML(result);

                    label.setValue(transformer.transform(curationType, result.toString()));


                    xmlReport.setStreamSource(new StreamSource() {
                        @Override
                        public InputStream getStream() {
                            return new ByteArrayInputStream(result.toByteArray());
                        }
                    });

                    break;
                case PROFILE:
                    switch (sourceType) {
                        case PROFILE_ID:
                            r = new CurationModule().processCMDProfile(input);
                            break;
                        case URL:
                            r = new CurationModule().processCMDProfile(new URL(input));
                            break;
                    }

                    r.toXML(result);

                    label.setValue(transformer.transform(curationType, result.toString()));


                    xmlReport.setStreamSource(new StreamSource() {
                        @Override
                        public InputStream getStream() {
                            return new ByteArrayInputStream(result.toByteArray());
                        }
                    });

                    break;
                case COLLECTION:


                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(Configuration.COLLECTION_HTML_DIRECTORY)) {
                        for (Path path : ds) {
                            if (path.getFileName().toString().equals(input + ".html")) {

                                byte[] out = Files.readAllBytes(path);

                                label.setValue(new String(out));

                                byte[] finalOut = out;
                                xmlReport.setStreamSource(new StreamSource() {
                                    @Override
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(finalOut);
                                    }
                                });

                            }
                        }
                    }

                    break;

                case STATISTICS:


                    String collectionName = input.split("/")[0];
                    int status = Integer.parseInt(input.split("/")[1]);

                    LinkCheckerStatisticsHelper helper = new LinkCheckerStatisticsHelper();

                    String resultHTML = helper.createURLTable(collectionName,status);
                    label.setValue(resultHTML);

                    break;

            }


        } catch (Exception e) {
            e.printStackTrace();

            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String msg = "Error while assesing " + curationType.toString().toLowerCase() + " from " + input + "!\n"
                    + errors.toString();
            label.setValue("<pre>" + msg + "</pre>");
        }
    }
}
