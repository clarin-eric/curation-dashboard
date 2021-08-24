package eu.clarin.cmdi.curation.utils;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDownloader {

    private int timeout;


    private final static Logger logger = LoggerFactory.getLogger(FileDownloader.class);

    //this is only for link-checker module, don't use it from core-module(will throw nullpointer because it can't find properties)
    public FileDownloader() {
        this(Configuration.TIMEOUT);
    }

    public FileDownloader(final int timeout) {
        this.timeout = timeout;
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
