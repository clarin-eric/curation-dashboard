package eu.clarin.cmdi.curation.utils;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
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

    private final static Logger _logger = LoggerFactory.getLogger(HTTPLinkChecker.class);

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
    public CheckedLink checkLink(String url, int redirectFollowLevel, long durationPassed, String originalURL) throws IOException, IllegalArgumentException {
        _logger.trace("Check link requested with url: " + url + " , redirectFollowLevel: " + redirectFollowLevel);
        if (url == null) {
            throw new IOException("The requested url is null.");
        }

        RequestConfig requestConfig = RequestConfig.custom()//put all timeouts to 5 seconds, should be max 15 seconds per link
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).disableRedirectHandling().build();

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


        duration += durationPassed;//durationPassed is for previous requests if any that led to a redirect

        int statusCode = response.getStatusLine().getStatusCode();

        CheckedLink checkedLink = new CheckedLink();
        checkedLink.setMethod("HEAD");
        checkedLink.setUrl(originalURL);
        checkedLink.setStatus(statusCode);

        //deal with redirect
        if (redirectStatusCodes.contains(statusCode)) {
            if (redirectFollowLevel >= REDIRECT_FOLLOW_LIMIT) {
//                checkedLink.setMessage("Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")");
                checkedLink.setStatus(0);
            } else {
                String redirectLink = response.getHeaders("Location")[0].getValue();
                if (redirectLink != null) {
                    if (redirectLink.equals(url)) {
//                        checkedLink.setMessage("Redirect link is the same");
                        checkedLink.setStatus(0);
                    } else {
                        try {
                            redirectLink = convertRelativeToAbsolute(url, redirectLink);
                            this.redirectLink = redirectLink;
                            return checkLink(redirectLink, redirectFollowLevel + 1, duration, originalURL);
                        } catch (URISyntaxException e) {
//                            checkedLink.setMessage("Redirect link is malformed");
                            checkedLink.setStatus(0);
                        }
                    }
                } else {
//                    checkedLink.setMessage("There is no redirect link('Location' header)");
                    checkedLink.setStatus(0);
                }
            }
        }

        //IF HEAD doesnt work and we are not over the redirect limit, try the same thing with get
        if (statusCode != 200 && statusCode != 304) {
            if (redirectFollowLevel < REDIRECT_FOLLOW_LIMIT) {


                HttpGet get = new HttpGet(url);
                get.setHeader("User-Agent", USERAGENT);

                start = System.currentTimeMillis();
                response = client.execute(get);
                end = System.currentTimeMillis();
                duration += end - start;

                statusCode = response.getStatusLine().getStatusCode();

                checkedLink.setStatus(statusCode);
                checkedLink.setMethod("GET");

                //deal with redirect
                if (statusCode == 200 || statusCode == 304) {
//                    checkedLink.setMessage("Ok");
                    checkedLink.setUrl(originalURL);
                } else if (redirectStatusCodes.contains(statusCode)) {
                    if (redirectFollowLevel >= REDIRECT_FOLLOW_LIMIT) {
//                        checkedLink.setMessage("Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")");
                        checkedLink.setStatus(0);
                    } else {
                        String redirectLink = response.getHeaders("Location")[0].getValue();
                        if (redirectLink != null) {
                            if (redirectLink.equals(url)) {
//                                checkedLink.setMessage("Redirect link is the same");
                                checkedLink.setStatus(0);
                            } else {
                                this.redirectLink = redirectLink;
                                return checkLink(redirectLink, redirectFollowLevel + 1, duration, originalURL);
                            }
                        } else {
//                            checkedLink.setMessage("There is no redirect link('Location' header)");
                            checkedLink.setStatus(0);
                        }
                    }
                } else {
//                    if (undeterminedStatusCodes.contains(statusCode)) {
//                        checkedLink.setMessage("Undetermined");
//                    } else {
//                        checkedLink.setMessage("Broken");
//                    }

                }

            } else {
//                checkedLink.setMessage("Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")");
                checkedLink.setStatus(0);
            }
        }


        String contentType;
        Header[] contentTypeArray = response.getHeaders("Content-Type");
        if (contentTypeArray.length == 0) {
            contentType = "Not specified";
        } else {
            contentType = contentTypeArray[0].getValue();
        }

        Header contentLengthHeader = response.getFirstHeader("Content-Length");
        if(contentLengthHeader!=null){
            int contentLength = Integer.parseInt(contentLengthHeader.getValue());
            checkedLink.setContentType(contentType);
            checkedLink.setByteSize(contentLength);
        }
        checkedLink.setDuration((int)duration);//dont forget to humanize to time
        checkedLink.setTimestamp(new Timestamp(start));//dont forget to humanize to date
        checkedLink.setRedirectCount(redirectFollowLevel);

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
