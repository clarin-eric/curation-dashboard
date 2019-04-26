package eu.clarin.helpers.HTMLHelpers;

public class NavbarButton {

    private String link;
    private String displayText;

    public NavbarButton(String link, String displayText) {
        this.displayText = displayText;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public String getDisplayText() {
        return displayText;
    }

}