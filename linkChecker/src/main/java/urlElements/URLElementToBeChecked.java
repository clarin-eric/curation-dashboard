package urlElements;

import org.bson.Document;

public class URLElementToBeChecked {

    private String url;

    private String collection;

    public URLElementToBeChecked(Document document){
        this.url = document.getString("url");
        this.collection = document.getString("collection");
    }

    public URLElementToBeChecked(String url, String collection) {
        this.url = url;
        this.collection = collection;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Document getMongoDocument() {
        Document document = new Document("url", url)
                .append("collection", collection);

        return document;
    }

}
