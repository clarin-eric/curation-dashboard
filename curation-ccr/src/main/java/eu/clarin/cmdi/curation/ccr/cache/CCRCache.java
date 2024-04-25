package eu.clarin.cmdi.curation.ccr.cache;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRStatus;
import eu.clarin.cmdi.curation.ccr.conf.CCRConfig;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * The type Ccr cache.
 */
@Component
@Slf4j
public class CCRCache {

    private final HttpUtils httpUtils;
    private final CCRConfig ccrProps;

    private final SAXParserFactory fac;

    public CCRCache(HttpUtils httpUtils, CCRConfig ccrProps) {

        this.httpUtils = httpUtils;

        this.ccrProps = ccrProps;

        this.fac = SAXParserFactory.newInstance();
        this.fac.setNamespaceAware(true);
    }

    /**
     * Gets ccr concept map.
     *
     * @return the ccr concept map with conceptURI as key
     */
    @Cacheable(value = "ccrCache")
    public CCRConcept getCCRConcept(String conceptURI) throws CCRServiceNotAvailableException {

        final CCRConcept[] concept = {null};
        /*
         * wowasa (2017-05-26): validation check might be switched off to bypass expired
         * certificates. System-property can be set with the following entry in web.xml
         * <env-entry> <env-entry-name>ccrservice.ssl.validate</env-entry-name>
         * <env-entry-type>java.lang.String</env-entry-type>
         * <env-entry-value>off</env-entry-value> </env-entry>
         */

        if (System.getProperty("ccrservice.ssl.validate", "on").equalsIgnoreCase("off")) {
            try {
                log.warn("SSL-certificate check in CCRService deactivated");

                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }};

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                String refHostName = new URL(ccrProps.getRestApi()).getHost();

                HttpsURLConnection
                        .setDefaultHostnameVerifier((hostname, session) -> hostname.equals(refHostName));


            }
            catch (NoSuchAlgorithmException ex) {

                log.error("SSL algorithm not available from SSL context");
                throw new CCRServiceNotAvailableException(ex);

            }
            catch (KeyManagementException ex) {

                log.error("couldn't set trust all certificate - this might be forbidden by policy settings");
                throw new CCRServiceNotAvailableException(ex);

            }
            catch (MalformedURLException ex) {

                log.error("can't extract hostname from URL '{}'", ccrProps.getRestApi());
                throw new CCRServiceNotAvailableException(ex);
            }
        } // end switch off validation check

        String fileName = conceptURI.replaceAll("[/.:]", "_") + ".xml";

        Path filePath = ccrProps.getCcrCache().resolve(fileName);

        try {
            if(Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS) || Files.size(filePath) == 0){

                if(Files.notExists(ccrProps.getCcrCache())) {

                    try {

                        Files.createDirectories(ccrProps.getCcrCache());
                    }
                    catch (IOException e) {

                        log.error("could create ccr cache directory '{}'", ccrProps.getCcrCache());
                        throw new CCRServiceNotAvailableException(e);
                    }
                }

                String restApiUrlStr = ccrProps.getRestApi() + ccrProps.getQuery().replace("${conceptURI}", URLEncoder.encode(conceptURI, StandardCharsets.UTF_8));

                log.debug("Fetching from {}", restApiUrlStr);

                try(InputStream in = httpUtils.getURLConnection(restApiUrlStr).getInputStream()){

                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {

                    log.info("can't cache concept '{}'", conceptURI);
                    return concept[0];
                }
            }
        }
        catch (IOException e) {

            log.error("can't access file '{}'", filePath);
            throw new CCRServiceNotAvailableException(e);
        }

        try {

            SAXParser parser = fac.newSAXParser();

            parser.parse(filePath.toFile(),
                    new DefaultHandler() {

                        private StringBuilder elementValue;

                        String prefLabel;
                        CCRStatus status = CCRStatus.UNKNOWN;

                        @Override
                        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

                            switch (localName) {

                                case "prefLabel", "status" -> elementValue = new StringBuilder();
                            }
                        }

                        @Override
                        public void endElement(String uri, String localName, String qName) throws SAXException {

                            switch (localName) {

                                case "prefLabel" -> this.prefLabel = this.elementValue.toString();
                                case "status" -> this.status = EnumUtils.getEnum(CCRStatus.class, this.elementValue.toString().toUpperCase(), CCRStatus.UNKNOWN);
                            }
                        }

                        @Override
                        public void endDocument() throws SAXException {

                            concept[0] = new CCRConcept(conceptURI, prefLabel, status);
                        }

                        @Override
                        public void characters(char[] ch, int start, int length) throws SAXException {
                            if (elementValue == null) {
                                elementValue = new StringBuilder();
                            } else {
                                elementValue.append(ch, start, length);
                            }
                        }
                    });
        }

        catch (ParserConfigurationException ex) {

            log.info("can't configure new SAXParser", ex);
            throw new CCRServiceNotAvailableException(ex);
        }
        catch (IOException ex) {

            log.info("can't read file '{}'", filePath);
        }

        catch (SAXException ex) {

            log.info("can't parse file '{}'", filePath);
        }

        return concept[0];
    }
}
