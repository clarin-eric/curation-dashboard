package eu.clarin.cmdi.curation.exception;


import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import org.apache.http.ConnectionClosedException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.RedirectException;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.*;

public class CategoryException{
    private static final org.slf4j.Logger LOG = LoggerFactory
            .getLogger(CategoryException.class);

    //Different exceptions according to https://docs.google.com/spreadsheets/d/18EyqXjL5-e7tc0kpvTHQNaG5ObXr_WNIfdvrcJRiTAg/edit#gid=0
    //the order of these is important as some of them extend others below
    //cant do a switch case with instanceof so it is if else...
    public static Category getCategoryFromException(Exception e, String url) {
        if (e instanceof MalformedURLException) {
            return Category.Broken;
        } else if (e instanceof IllegalArgumentException) {
            return Category.Broken;
        } else if (e instanceof RedirectException) {
            return Category.Undetermined;
        } else if (e instanceof LoginPageException) {
            return Category.Restricted_Access;
        } else if (e instanceof DeniedByRobotsException) {
            return Category.Blocked_By_Robots_txt;
        } else if (e instanceof CrawlDelayTooLongException) {
            return Category.Blocked_By_Robots_txt;
        } else if (e instanceof ConnectException) {
            return Category.Broken;
        } else if (e instanceof SocketTimeoutException) {
            return Category.Broken;
        } else if (e instanceof NoHttpResponseException) {
            return Category.Broken;
        } else if (e instanceof ConnectTimeoutException) {
            return Category.Broken;
        } else if (e instanceof UnknownHostException) {
            return Category.Broken;
            //These next three exceptions are both child classes of SSLException.
            //Since they all have the same category, we don't need to have the code for them separately
//        } else if (e instanceof SSLHandshakeException) {
//            return Category.Undetermined;
//        }else if (e instanceof SSLPeerUnverifiedException) {
//            return Category.Undetermined;
//        } else if (e instanceof ValidatorException) {
//            return Category.Undetermined;//
        } else if (e instanceof SSLException) {
            return Category.Undetermined;//TODO when stormychecker is deployed to clarin, make this broken
        } else if (e instanceof NoRouteToHostException) {
            return Category.Broken;
        } else if (e instanceof SocketException) {
            return Category.Undetermined;
        } else if (e instanceof ConnectionClosedException) {
            return Category.Broken;
        } else {
            //TODO after running the program for a while, search in the logs for this text and improve it with the exception
            LOG.info("For the URL: \"" + url + "\" there was a yet undefined exception: " + e.getClass().toString() +
                    " with the message: " + e.getMessage() + ". Please add this new exception into the code");
            return Category.Undetermined; // we dont know the exception, then we can't determine it: Undetermined.
        }
    }
}

