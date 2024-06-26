package eu.clarin.cmdi.curation.commons.http;

import eu.clarin.cmdi.curation.commons.conf.HttpConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

@Component
public class HttpUtils {

    @Autowired
    private HttpConfig httpConfig;

    public URLConnection getURLConnection(String urlString) throws IOException {

        URL url = new URL(urlString);

        Proxy proxy = (StringUtils.isNotBlank(httpConfig.getProxyHost()) && (httpConfig.getProxyPort() > 0)) ?
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpConfig.getProxyHost(), httpConfig.getProxyPort())) :
                Proxy.NO_PROXY;

        URLConnection connection = url.openConnection(proxy);

        connection.setConnectTimeout(httpConfig.getConnectionTimeout());

        connection.setReadTimeout(httpConfig.getReadTimeout());

        connection.setRequestProperty("User-Agent", httpConfig.getUserAgent());

        return connection;
    }
}
