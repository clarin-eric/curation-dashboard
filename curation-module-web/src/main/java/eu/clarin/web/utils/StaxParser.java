package eu.clarin.web.utils;


import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.web.data.CollectionStatistics;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StaxParser {


    //This method initializes and returns a CollectionStatistics object to be used in the general Collections View.
    //It also trims the single-url-report list into 50 urls transforms it into html(with xslt) and saves the html into a folder
    //This is all done to deal with really large reports that can grow up to 800 mbs.
    public static CollectionStatistics handleCollectionXMLs(InputStream stream, String fileName) throws XMLStreamException, IOException {


        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader =
                inputFactory.createXMLEventReader(stream);

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEventWriter writer = outputFactory.createXMLEventWriter(out);

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        CollectionStatistics cs = new CollectionStatistics();

        boolean singleUrl = false;

        boolean provider = false;
        boolean numOfFiles = false;
        boolean totNumOfUniqueLinks = false;
        boolean totNumOfCheckedLinks = false;
        boolean ratioOfValidLinks = false;
        boolean avgNumOfResProxies = false;
        boolean totNumOfResProxies = false;
        boolean ratioOfValidRecords = false;
        boolean avgXMLEmptyElement = false;
        boolean coverage = false;
        boolean trim = true;

        List<Double> facets = new ArrayList<>();

        int urlCount = 0;
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {//only startelement is needed because urls are empty elements
                StartElement startElement = event.asStartElement();

                if (startElement.getName().getLocalPart().equalsIgnoreCase("url")) {
                    if (urlCount < 50) {
                        writer.add(event);
                    }


                } else if (startElement.getName().getLocalPart().equalsIgnoreCase("single-url-report")) {

                    singleUrl = true;

                    writer.add(event);
                    if (urlCount == 50 && trim) {
                        event = eventFactory.createAttribute
                                ("trim", "true");
                        writer.add(event);
                        trim = false;//only once this should be done
                    }


                } else {
                    writer.add(event);

                    if (startElement.getName().getLocalPart().equalsIgnoreCase("provider")) {
                        provider = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("collection-report")) {
                        Attribute scorePercentage = startElement.getAttributeByName(new QName("score-percentage"));
                        if(scorePercentage==null){
                            cs.setScorePercentage(0.0);
                        }else{
                            cs.setScorePercentage(Double.parseDouble(scorePercentage.getValue()));
                        }
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("numOfFiles")) {
                        numOfFiles = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("profiles")) {
                        if (startElement.getAttributeByName(new QName("name")) == null) {//trust me on this, this is correct code
                            cs.setHeaderReport_profiles_totNumOfProfiles(Integer.parseInt(startElement.getAttributeByName(new QName("count")).getValue()));
                        }
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("totNumOfUniqueLinks")) {
                        totNumOfUniqueLinks = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("totNumOfCheckedLinks")) {
                        totNumOfCheckedLinks = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("ratioOfValidLinks")) {
                        ratioOfValidLinks = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("avgNumOfResProxies")) {
                        avgNumOfResProxies = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("totNumOfResProxies")) {
                        totNumOfResProxies = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("ratioOfValidRecords")) {
                        ratioOfValidRecords = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("avgXMLEmptyElement")) {
                        avgXMLEmptyElement = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("coverage")) {
                        coverage = true;
                    } else if (startElement.getName().getLocalPart().equalsIgnoreCase("facet")) {
                        facets.add(Double.parseDouble(startElement.getAttributeByName(new QName("coverage")).getValue()));
                    }


                }

            } else if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                if (singleUrl) {
                    if (urlCount < 50) {
                        writer.add(event);
                    }
                } else {
                    writer.add(event);
                }

                Characters characters = event.asCharacters();
                if (provider) {
                    cs.setFileReport_provider(characters.getData());
                    provider = false;
                } else if (numOfFiles) {
                    cs.setFileReport_numOfFiles(Integer.parseInt(characters.getData()));
                    numOfFiles = false;
                } else if (totNumOfUniqueLinks) {
                    cs.setUrlReport_totNumOfUniqueLinks(Integer.parseInt(characters.getData()));
                    totNumOfUniqueLinks = false;
                } else if (totNumOfCheckedLinks) {
                    cs.setUrlReport_totNumOfCheckedLinks(Integer.parseInt(characters.getData()));
                    totNumOfCheckedLinks = false;
                } else if (ratioOfValidLinks) {
                    cs.setUrlReport_ratioOfValidLinks(Double.parseDouble(characters.getData()));
                    ratioOfValidLinks = false;
                } else if (avgNumOfResProxies) {
                    cs.setResProxyReport_avgNumOfResProxies(Double.parseDouble(characters.getData()));
                    avgNumOfResProxies = false;
                } else if (totNumOfResProxies) {
                    cs.setResProxyReport_totNumOfResProxies(Integer.parseInt(characters.getData()));
                    totNumOfResProxies = false;
                } else if (ratioOfValidRecords) {
                    cs.setXmlValidationReport_ratioOfValidRecords(Double.parseDouble(characters.getData()));
                    ratioOfValidRecords = false;
                } else if (avgXMLEmptyElement) {
                    cs.setXmlPopulatedReport_avgXMLEmptyElement(Double.parseDouble(characters.getData()));
                    avgXMLEmptyElement = false;
                } else if (coverage) {
                    cs.setFacetReport_coverage(Double.parseDouble(characters.getData()));
                    coverage = false;
                }


            } else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {

                EndElement endElement = event.asEndElement();

                if (endElement.getName().getLocalPart().equalsIgnoreCase("url")) {
                    if (urlCount < 50) {
                        writer.add(event);
                    }
                    urlCount++;

                } else if (endElement.getName().getLocalPart().equalsIgnoreCase("single-url-report")) {
                    singleUrl = false;
                } else {
                    writer.add(event);
                }

            } else {
                writer.add(event);
            }

        }

        cs.setFacetReport_facet(facets);

        writer.flush();
        writer.close();


        transformAndSave(out.toByteArray(), Configuration.COLLECTION_HTML_DIRECTORY + "/" + fileName + ".html");


        //might be useful for debugging
        //this is just to see what the xml looks like after stax transformation(its normally not saved anywhere)
//        FileOutputStream fos = null;
//        fos = new FileOutputStream(new File(path to xml file));
//        result.writeTo(fos);
//        fos.close();

        return cs;
    }


    //this transforms the xml file into html through the xslt transformation
    //and then saves the html into the specific folder
    //this is only done for collections and once at initialization
    private static void transformAndSave(byte[] out, String path) throws IOException {

        XSLTTransformer transformer = new XSLTTransformer();
        String html = transformer.transform(CurationEntity.CurationEntityType.COLLECTION, new String(out));

        try (PrintStream ps = new PrintStream(Files.newOutputStream(Paths.get(path)))) {
            ps.println(html);
        }

    }
}
