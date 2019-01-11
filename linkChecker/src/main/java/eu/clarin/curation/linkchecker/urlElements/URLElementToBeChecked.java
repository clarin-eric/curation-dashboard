package eu.clarin.curation.linkchecker.urlElements;

import org.bson.Document;

public class URLElementToBeChecked {

    private String url;
    private String record;
    private String collection;
    private String expectedMimeType;

    public URLElementToBeChecked(Document document) {
        this.url = document.getString("url");
        this.record = document.getString("record");
        this.collection = document.getString("collection");
        this.expectedMimeType = document.getString("expectedMimeType");
    }

    public URLElementToBeChecked(String url, String record, String collection, String expectedMimeType) {
        this.url = url;
        this.record = record;
        this.collection = collection;
        this.expectedMimeType = expectedMimeType;
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
                .append("record",record)
                .append("collection", collection)
                .append("expectedMimeType",expectedMimeType);

        return document;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getExpectedMimeType() {
        return expectedMimeType;
    }

    public void setExpectedMimeType(String expectedMimeType) {
        this.expectedMimeType = expectedMimeType;
    }
}
