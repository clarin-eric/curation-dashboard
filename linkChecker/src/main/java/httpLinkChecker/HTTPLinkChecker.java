package httpLinkChecker;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlElements.URLElement;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * @author dostojic
 */
public class HTTPLinkChecker {

    private int timeout;
    private HttpURLConnection connection = null;
    private String redirectLink = null;
    //    private int REDIRECT_FOLLOW_LIMIT = Configuration.REDIRECT_FOLLOW_LIMIT;//todo
    private int REDIRECT_FOLLOW_LIMIT = 5;
    private List<Integer> redirectStatusCodes = new ArrayList<>(Arrays.asList(301, 302, 303, 307, 308));

    private final static Logger logger = LoggerFactory.getLogger(HTTPLinkChecker.class);

//    public HTTPLinkChecker() {
//        this(Configuration.TIMEOUT);//todo
//    }

    public HTTPLinkChecker() {
        this(5000);
    }

    public HTTPLinkChecker(final int timeout) {
        redirectLink = null;
        this.timeout = timeout;
    }

    //this method checks link with HEAD, if it fails it calls a check link with GET method
    public URLElement checkLink(String url, int redirectFollowLevel, long durationPassed) throws IOException {
        logger.info("Check link requested with url: " + url + " , redirectFollowLevel: " + redirectFollowLevel);
        RequestConfig requestConfig = RequestConfig.custom()//put all timeouts to 5 seconds, should be max 15 seconds per link
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).disableRedirectHandling().build();

        //try get if head doesnt work

        //valid-example.xml has this url: http://clarin.oeaw.ac.at/lrp/dict-gate/index.html
        //returns 400 for head but browser opens fine

        HttpHead head = new HttpHead(url);

        long start = System.currentTimeMillis();
        HttpResponse response = client.execute(head);
        long end = System.currentTimeMillis();
        long duration = end - start;

        duration += durationPassed;//durationPassed is for previous requests if any that led to a redirect

        int statusCode = response.getStatusLine().getStatusCode();

        URLElement urlElement = new URLElement();
        urlElement.setMethod("HEAD");
        urlElement.setMessage("Ok");
        urlElement.setUrl(url);
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
                        this.redirectLink = redirectLink;
                        return checkLink(redirectLink, redirectFollowLevel + 1, duration);
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
                    urlElement.setUrl(url);
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
                                return checkLink(redirectLink, redirectFollowLevel + 1, duration);
                            }
                        } else {
                            urlElement.setMessage("There is no redirect link('Location' header)");
                            urlElement.setStatus(0);
                        }
                    }
                } else {
                    urlElement.setMessage("Broken Link");

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

        return urlElement;
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
