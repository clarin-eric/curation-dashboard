package eu.clarin.cmdi.curation.utils;

import at.ac.oeaw.acdh.stormychecker.config.Constants;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HTTPLinkChecker {

    private int timeout;
    private String redirectLink = null;
    private int REDIRECT_FOLLOW_LIMIT;
    private String USERAGENT;
    private List<Integer> redirectStatusCodes = new ArrayList<>(Arrays.asList(301, 302, 303, 307, 308));

    //this determines what status codes will not be considered broken links. urls with these codes will also not factor into the url-scores
    private List<Integer> undeterminedStatusCodes = new ArrayList<>(Arrays.asList(401, 405, 429));

    private final static Logger logger = LoggerFactory.getLogger(HTTPLinkChecker.class);

    //this is only for link-checker module, don't use it from core-module(will throw nullpointer because it can't find properties)
    public HTTPLinkChecker() {
        this(Configuration.TIMEOUT, Configuration.REDIRECT_FOLLOW_LIMIT, Configuration.USERAGENT);
    }

    public HTTPLinkChecker(final int timeout, final int REDIRECT_FOLLOW_LIMIT, final String USERAGENT) {
        redirectLink = null;
        this.timeout = timeout;
        this.REDIRECT_FOLLOW_LIMIT = REDIRECT_FOLLOW_LIMIT;
        this.USERAGENT = USERAGENT;
    }

    //this method checks link with HEAD, if it fails it calls a check link with GET method
    public CheckedLink checkLink(String url) throws IOException, IllegalArgumentException {
        RequestConfig requestConfig = RequestConfig.custom()//put all timeouts to 5 seconds, should be max 15 seconds per link
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

        //valid-example.xml has this url: http://clarin.oeaw.ac.at/lrp/dict-gate/index.html
        //returns 400 for head but browser opens fine

        HttpHead head;
        try {
            head = new HttpHead(url);
        } catch (IllegalArgumentException e) {
            //encode url, to remove problems caused by | and similar characters
            String[] urlArray = url.split("\\?");
            if (urlArray.length == 2) {
                url = urlArray[0] + "?" + URLEncoder.encode(urlArray[1], "UTF-8");
            }
            head = new HttpHead(url);
        }

        head.setHeader("User-Agent", USERAGENT);

        long start = System.currentTimeMillis();
        HttpResponse response = client.execute(head);
        long end = System.currentTimeMillis();
        long duration = end - start;

        CheckedLink checkedLink = new CheckedLink();
        checkedLink.setMethod("HEAD");
        checkedLink.setUrl(url);

        int statusCode = response.getStatusLine().getStatusCode();

        if(!Constants.okStatusCodes.contains(statusCode)){//HEAD unsuccessful try GET
            HttpGet get = new HttpGet(url);
            get.setHeader("User-Agent", USERAGENT);

            start = System.currentTimeMillis();
            response = client.execute(get);
            end = System.currentTimeMillis();
            duration += end - start;

            statusCode = response.getStatusLine().getStatusCode();

            checkedLink.setMethod("GET");
        }

        checkedLink.setStatus(statusCode);


        if (Constants.okStatusCodes.contains(statusCode)) {
            checkedLink.setMessage(Category.Ok.name());
            checkedLink.setCategory(Category.Ok);
        } else if (Constants.undeterminedStatusCodes.contains(statusCode)) {
            checkedLink.setMessage("Undetermined, Status code: " + statusCode);
            checkedLink.setCategory(Category.Undetermined);
        } else {
            checkedLink.setMessage("Broken, Status code: " + statusCode);
            checkedLink.setCategory(Category.Broken);
        }

        String contentType;
        Header[] contentTypeArray = response.getHeaders("Content-Type");
        if (contentTypeArray.length == 0) {
            contentType = "Not specified";
        } else {
            contentType = contentTypeArray[0].getValue();
        }

        Header contentLengthHeader = response.getFirstHeader("Content-Length");
        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader.getValue());
            checkedLink.setContentType(contentType);
            checkedLink.setByteSize(contentLength);
        }
        checkedLink.setDuration((int) duration);//dont forget to humanize to time
        checkedLink.setTimestamp(new Timestamp(start));//dont forget to humanize to date

        return checkedLink;
    }

    public String convertRelativeToAbsolute(String url, String locationHeader) throws URISyntaxException {
        String result;
        if (locationHeader.startsWith(".")) {
            //remove query parameters
            url = url.split("\\?")[0];
            int lastIndex = url.lastIndexOf("/");
            result = url.substring(0, lastIndex) + locationHeader.substring(1);

        } else if (locationHeader.startsWith("/")) {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String domain = uri.getHost();
            result = scheme + "://" + domain + locationHeader;
        } else {
            return locationHeader;
        }
        return result;
    }

    //todo 5 mb size limit
    public void download(String url, File destination) throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);

        FileOutputStream fos = new FileOutputStream(destination);

        response.getEntity().writeTo(fos);
    }

}
