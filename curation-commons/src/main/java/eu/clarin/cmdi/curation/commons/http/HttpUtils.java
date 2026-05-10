package eu.clarin.cmdi.curation.commons.http;

import eu.clarin.cmdi.curation.commons.conf.HttpConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@Component
public class HttpUtils {

    private final HttpConfig httpConfig;

    private final HttpClient httpClient;

    public HttpUtils(HttpConfig httpConfig) {

        this.httpConfig = httpConfig;

        HttpClient.Builder builder = HttpClient
                .newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL);

        if(StringUtils.isNotBlank(httpConfig.getProxyHost()) && (httpConfig.getProxyPort() > 0)){

            builder.proxy(ProxySelector.of(new InetSocketAddress(httpConfig.getProxyHost(), httpConfig.getProxyPort())));
        }
        if(httpConfig.getConnectionTimeout() > 0) {

            builder.connectTimeout(Duration.ofMillis(httpConfig.getConnectionTimeout()));
        }
        this.httpClient = builder.build();
    }

    private HttpRequest getHttpRequest(URI uri, String acceptHeader) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri);

        if(StringUtils.isNotBlank(httpConfig.getUserAgent())) {

            builder.header("User-Agent", httpConfig.getUserAgent());
        }
        if(StringUtils.isNotBlank(acceptHeader)) {

            builder.header("Accept", acceptHeader);
        }
        if(httpConfig.getReadTimeout() >0){

            builder.timeout(Duration.ofMillis(httpConfig.getReadTimeout()));
        }

        return builder.build();
    }

    public byte[] getBytes(URI uri) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, null), HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    public byte[] getBytes(URI uri, String acceptHeader) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, acceptHeader), HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    public String getString(URI uri) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, null), HttpResponse.BodyHandlers.ofString()).body();
    }

    public String getString(URI uri, String acceptHeader) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, acceptHeader), HttpResponse.BodyHandlers.ofString()).body();
    }

    public InputStream getInputStream(URI uri) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, null), HttpResponse.BodyHandlers.ofInputStream()).body();
    }

    public InputStream getInputStream(URI uri, String acceptHeader) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, acceptHeader), HttpResponse.BodyHandlers.ofInputStream()).body();
    }

    public HttpResponse<InputStream> getReponse(URI uri, String acceptHeader) throws IOException, InterruptedException {

        return this.httpClient.send(getHttpRequest(uri, acceptHeader), HttpResponse.BodyHandlers.ofInputStream());
    }
}
