package eu.clarin.cmdi.curation.commons.http;

import eu.clarin.cmdi.curation.commons.conf.HttpConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;


@Component
public class HttpUtils {

    private final HttpConfig httpConfig;

    public HttpUtils(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    public URLConnection getURLConnection(String urlString) throws IOException, URISyntaxException {

        URL url = new URI(urlString).toURL();

        Proxy proxy = (StringUtils.isNotBlank(httpConfig.getProxyHost()) && (httpConfig.getProxyPort() > 0)) ?
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpConfig.getProxyHost(), httpConfig.getProxyPort())) :
                Proxy.NO_PROXY;

        URLConnection connection = url.openConnection(proxy);

        if(httpConfig.getConnectionTimeout() > 0) {
            connection.setConnectTimeout(httpConfig.getConnectionTimeout());
        }
        if(httpConfig.getReadTimeout() > 0) {
            connection.setReadTimeout(httpConfig.getReadTimeout());
        }
        if(StringUtils.isNotBlank(httpConfig.getUserAgent())) {
            connection.setRequestProperty("User-Agent", httpConfig.getUserAgent());
        }

        return connection;
    }

    public URLConnection getURLConnection(String urlString, String acceptHeader) throws IOException {

        URLConnection connection = getURLConnection(urlString);

        connection.setRequestProperty("Accept", acceptHeader);

        return connection;
    }
}
