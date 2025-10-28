package eu.clarin.cmdi.curation.ccr.cache;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRStatus;
import eu.clarin.cmdi.curation.ccr.conf.CCRConfig;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Ccr cache.
 */
@Component
@Slf4j
public class CCRCache {

    private final HttpUtils httpUtils;
    private final CCRConfig ccrConfig;

    private final Pattern DUBLIN_PATTERN = Pattern.compile("http(s)?://purl.org.+/(\\w+)");
    private final Pattern WIKIDATA_PATTERN = Pattern.compile("http(s)?://www.wikidata.org/.*([PQ]{1}\\d+)");
//    private final Pattern W3ID_PATTERN = Pattern.compile("http(s)?://www.w3id.org/.+/(\\w+)");
//    private final Pattern MODS_PATTERN = Pattern.compile("http(s)?://www.loc.gov/.+#(\\w+)");

    private final SAXParserFactory fac;

    public CCRCache(HttpUtils httpUtils, CCRConfig ccrConfig) {

        this.httpUtils = httpUtils;

        this.ccrConfig = ccrConfig;

        this.fac = SAXParserFactory.newInstance();
        this.fac.setNamespaceAware(false);
    }

    /**
     * Gets ccr concept map.
     *
     * @return the ccr concept map with conceptURI as key
     */
    @Cacheable(value = "ccrCache", key = "#conceptURI", sync = true)
    public CCRConcept getCCRConcept(String conceptURI) throws CCRServiceNotAvailableException {

        Matcher matcher;

        if ((matcher = DUBLIN_PATTERN.matcher(conceptURI)).matches()
//                || (matcher = W3ID_PATTERN.matcher(conceptURI)).matches()
//                || (matcher = MODS_PATTERN.matcher(conceptURI)).matches()
        ) {

            return new CCRConcept(conceptURI, matcher.group(2), CCRStatus.APPROVED);
        }


        // we check whether the URI is wikidata
        boolean isWikidata = false;

        String requestUrlStr;

        if((matcher = WIKIDATA_PATTERN.matcher(conceptURI)).matches()) {
            isWikidata = true;
            requestUrlStr = ccrConfig.getWikidataRequest().replace("${entityID}", matcher.group(2));
        }
        else{

            requestUrlStr = ccrConfig.getClavasRequest().replace("${conceptURI}", URLEncoder.encode(conceptURI, StandardCharsets.UTF_8));
        }

        try {

            HttpResponse<InputStream> response = this.httpUtils.getReponse(new URI(requestUrlStr), "*/*");

            if(response.statusCode() != 200) {

                return new CCRConcept(conceptURI, "invalid concept", CCRStatus.UNKNOWN);
            }

            SAXParser parser = fac.newSAXParser();

            DefaultHandler handler;

            CCRConcept concept = new CCRConcept(conceptURI);

            if (isWikidata) {

                handler = new WikidataHandler(concept);
            }
            else {

                handler = new SkosHandler(concept);
            }

            parser.parse(response.body(), handler);

            return concept;
        }
        catch (ParserConfigurationException e) {

            throw new CCRServiceNotAvailableException(e);
        }
        catch (SAXException | URISyntaxException | IOException | InterruptedException e) {

            log.error("can't process concept URI '{}'", conceptURI);
            return new CCRConcept(conceptURI, "invalid concept", CCRStatus.UNKNOWN);
        }
    }

    private class SkosHandler extends DefaultHandler {

        private final CCRConcept concept;

        private StringBuilder elementValue;

        private SkosHandler(CCRConcept concept) {

            this.concept = concept;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            switch (qName) {

                case "skos:prefLabel", "ns0:status" -> elementValue = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {

            switch (qName) {

                case "skos:prefLabel" -> this.concept.setPrefLabel(this.elementValue.toString());
                case "ns0:status" ->
                        this.concept.setStatus(EnumUtils.getEnum(CCRStatus.class, this.elementValue.toString().toUpperCase(), CCRStatus.UNKNOWN));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (elementValue == null) {
                elementValue = new StringBuilder();
            } else {
                elementValue.append(ch, start, length);
            }
        }
    }
    private class WikidataHandler extends DefaultHandler {

        private final CCRConcept concept;

        private StringBuilder elementValue;

        private WikidataHandler(CCRConcept concept) {
            this.concept = concept;
            this.concept.setStatus(CCRStatus.APPROVED);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            switch (qName) {

                case "literal" -> elementValue = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {

            switch (qName) {

                case "literal" -> this.concept.setPrefLabel(this.elementValue.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (elementValue == null) {
                elementValue = new StringBuilder();
            } else {
                elementValue.append(ch, start, length);
            }
        }
    }
}
