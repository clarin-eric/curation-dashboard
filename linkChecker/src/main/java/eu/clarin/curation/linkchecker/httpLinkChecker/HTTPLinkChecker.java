package eu.clarin.curation.linkchecker.httpLinkChecker;

import eu.clarin.curation.linkchecker.helpers.Configuration;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dostojic
 */
public class HTTPLinkChecker {

    private int timeout;
    private HttpURLConnection connection = null;
    private String redirectLink = null;
    private int REDIRECT_FOLLOW_LIMIT;
    private String USERAGENT;
    private List<Integer> redirectStatusCodes = new ArrayList<>(Arrays.asList(301, 302, 303, 307, 308));

    //this determines what status codes will not be considered broken links. urls with these codes will also not factor into the url-scores
    private List<Integer> undeterminedStatusCodes = new ArrayList<>(Arrays.asList(401,405,429));

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

    //this method lets httpclient handle the redirects by itself
    public int checkLinkAndGetResponseCode(String url) throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()//put all timeouts to 5 seconds, should be max 15 seconds per link
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

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
        HttpResponse response = client.execute(head);
        return response.getStatusLine().getStatusCode();

    }

    //this method checks link with HEAD, if it fails it calls a check link with GET method
    public URLElement checkLink(String url, int redirectFollowLevel, long durationPassed, String originalURL) throws IOException, IllegalArgumentException {
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

        URLElement urlElement = new URLElement();
        urlElement.setMethod("HEAD");
        urlElement.setMessage("Ok");
        urlElement.setUrl(originalURL);
        urlElement.setStatus(statusCode);

        //deal with redirect
        if (redirectStatusCodes.contains(statusCode)) {
            if (redirectFollowLevel >= REDIRECT_FOLLOW_LIMIT) {
                urlElement.setMessage("Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")");
            } else {
                String redirectLink = response.getHeaders("Location")[0].getValue();
                if (redirectLink != null) {
                    if (redirectLink.equals(url)) {
                        urlElement.setMessage("Redirect link is the same");
                    } else {
                        try {
                            redirectLink = convertRelativeToAbsolute(url, redirectLink);
                            this.redirectLink = redirectLink;
                            return checkLink(redirectLink, redirectFollowLevel + 1, duration, originalURL);
                        } catch (URISyntaxException e) {
                            urlElement.setMessage("Redirect link is malformed");
                        }
                    }
                } else {
                    urlElement.setMessage("There is no redirect link('Location' header)");
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

                urlElement.setStatus(statusCode);
                urlElement.setMethod("GET");

                //deal with redirect
                if (statusCode == 200 || statusCode == 304) {
                    urlElement.setMessage("Ok");
                    urlElement.setUrl(originalURL);
                } else if (redirectStatusCodes.contains(statusCode)) {
                    if (redirectFollowLevel >= REDIRECT_FOLLOW_LIMIT) {
                        urlElement.setMessage("Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")");
                        urlElement.setStatus(0);
                    } else {
                        String redirectLink = response.getHeaders("Location")[0].getValue();
                        if (redirectLink != null) {
                            if (redirectLink.equals(url)) {
                                urlElement.setMessage("Redirect link is the same");
                                urlElement.setStatus(0);
                            } else {
                                this.redirectLink = redirectLink;
                                return checkLink(redirectLink, redirectFollowLevel + 1, duration, originalURL);
                            }
                        } else {
                            urlElement.setMessage("There is no redirect link('Location' header)");
                            urlElement.setStatus(0);
                        }
                    }
                } else {
                    if(undeterminedStatusCodes.contains(statusCode)){
                        urlElement.setMessage("Undetermined");
                    }else{
                        urlElement.setMessage("Broken Link");
                    }

                }

            } else {
                urlElement.setMessage("Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")");
                urlElement.setStatus(0);
            }
        }


        String contentType;
        Header[] contentTypeArray = response.getHeaders("Content-Type");
        if (contentTypeArray.length == 0) {
            contentType = "Not specified";
        } else {
            contentType = contentTypeArray[0].getValue();
        }

        String contentLength;
        Header[] contentLengthArray = response.getHeaders("Content-Length");
        if (contentLengthArray.length == 0) {
            contentLength = "Not specified";
        } else {
            contentLength = contentLengthArray[0].getValue();
        }


        urlElement.setContentType(contentType);
        urlElement.setByteSize(contentLength);
        urlElement.setDuration(duration);//dont forget to humanize to time
        urlElement.setTimestamp(start);//dont forget to humanize to date
        urlElement.setRedirectCount(redirectFollowLevel);

        return urlElement;
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

    public String getRedirectLink() {
        return redirectLink;
    }


    public String getResponse() throws IOException {
        if (connection == null)
            return "Connection is null";

        StringBuilder builder = new StringBuilder();
        builder.append(connection.getResponseCode())
                .append(" ")
                .append(connection.getResponseMessage())
                .append("\n");

        Map<String, List<String>> map = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() == null)
                continue;
            builder.append(entry.getKey())
                    .append(": ");

            List<String> headerValues = entry.getValue();
            Iterator<String> it = headerValues.iterator();
            if (it.hasNext()) {
                builder.append(it.next());

                while (it.hasNext()) {
                    builder.append(", ")
                            .append(it.next());
                }
            }

            builder.append("\n");
        }

        return builder.toString();
    }

}
