package eu.clarin.cmdi.curation.io;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author dostojic
 */
public class HTTPLinkChecker {

    private int timeout;
    private HttpURLConnection connection = null;
    private String redirectLink = null;
    private int REDIRECT_FOLLOW_LIMIT = Configuration.REDIRECT_FOLLOW_LIMIT;
    private List<Integer> redirectStatusCodes = new ArrayList<>(Arrays.asList(301, 302, 303, 307, 308));

    public HTTPLinkChecker() {
        this(5000);
    }

    public HTTPLinkChecker(final int timeout) {
        HttpURLConnection connection = null;
        redirectLink = null;
        this.timeout = timeout;
    }

    public int checkLink(String url, CMDInstanceReport report, int redirectFollowLevel) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();

        //try get if head doesnt work
        //todo really?

        HttpHead head = new HttpHead(url);

        long start = System.currentTimeMillis();
        HttpResponse response = client.execute(head);
        long end = System.currentTimeMillis();
        long duration = end - start;

        int statusCode = response.getStatusLine().getStatusCode();

        CMDInstanceReport.URLElement urlElement = new CMDInstanceReport.URLElement();
        urlElement.message = "Ok";
        urlElement.url = url;
        urlElement.status = statusCode;

        if (redirectStatusCodes.contains(statusCode)) {
            if (redirectFollowLevel >= REDIRECT_FOLLOW_LIMIT) {
                urlElement.message = "Too Many Redirects(Limit:" + REDIRECT_FOLLOW_LIMIT + ")";
            } else {
                String redirectLink = response.getHeaders("Location")[0].getValue();
                if (redirectLink != null) {
                    if (redirectLink.equals(url)) {
                        urlElement.message = "Redirect link is the same";
                    } else {
                        this.redirectLink = redirectLink;
                        return checkLink(redirectLink, report, redirectFollowLevel + 1);
                    }
                } else {
                    urlElement.message = "There is no redirect link('Location' header)";
                }
            }
        }

        String contentType = response.getHeaders("Content-Type")[0].getValue();
        String contentLength = response.getHeaders("Content-Length")[0].getValue();


        urlElement.contentType = contentType;
        try {
            urlElement.byteSize = Long.parseLong(contentLength);
        } catch (NumberFormatException e) {
            urlElement.byteSize = 0;
        }
        urlElement.duration = duration;
        urlElement.timestamp = start;

        report.addURLElement(urlElement);

        return statusCode;
    }

    //todo change this also for downloader
    //this is legacy for downloader
    public int checkLink(String url) throws Exception {
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
        connection.setRequestMethod("HEAD");
        String redirectLink = connection.getHeaderField("Location");
        if (redirectLink != null && !redirectLink.equals(url)) {
            this.redirectLink = redirectLink;
            return checkLink(redirectLink);
        } else
            return connection.getResponseCode();
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
