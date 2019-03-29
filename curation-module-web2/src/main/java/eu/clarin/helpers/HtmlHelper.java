package eu.clarin.helpers;

import eu.clarin.main.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlHelper {

    public static String addContentToGenericHTML(String content) {
        Document doc = Jsoup.parse(Configuration.GENERIC_HTML);

        Element contentElement = doc.getElementById("content");
        contentElement.append(content);
        return doc.html();
    }
}
