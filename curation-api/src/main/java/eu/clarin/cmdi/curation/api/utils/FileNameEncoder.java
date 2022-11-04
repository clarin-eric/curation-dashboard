package eu.clarin.cmdi.curation.api.utils;

public class FileNameEncoder {

    public static String encode(String source){
        return source.replaceAll("[/.:]", "_");
    }
}
