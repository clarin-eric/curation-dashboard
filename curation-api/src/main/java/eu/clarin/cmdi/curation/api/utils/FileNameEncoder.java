package eu.clarin.cmdi.curation.api.utils;

/**
 * The type File name encoder.
 */
public class FileNameEncoder {

    /**
     * Encode string.
     *
     * @param sourceString the source string to encode
     * @return the string
     */
    public static String encode(String sourceString){
        return sourceString.replaceAll("[\\/.:]", "_");
    }
}
