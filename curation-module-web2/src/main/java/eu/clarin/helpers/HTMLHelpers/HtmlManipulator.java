package eu.clarin.helpers.HTMLHelpers;

import eu.clarin.helpers.FileManager;
import eu.clarin.main.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class HtmlManipulator {

    //if navbarbutton is null, then nothing is shown
    public static String addContentToGenericHTML(String content, NavbarButton button) throws IOException {

        String genericHTML = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/generic.html");

        Document doc = Jsoup.parse(genericHTML);

        if(button!=null){
            Element navbarButtonElement = doc.getElementById("navbarButton");
            navbarButtonElement.removeAttr("hidden");
            navbarButtonElement.append(button.getDisplayText());
            navbarButtonElement.attr("href",button.getLink());
        }

        Element contentElement = doc.getElementById("content");
        contentElement.append(content);
        return doc.html();
    }
}
