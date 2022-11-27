package eu.clarin.cmdi.curation.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.CollectionReport;

public interface CurationModule {

   public CMDProfileReport processCMDProfile(String profileId) throws MalformedURLException, SubprocessorException;

   public CMDProfileReport processCMDProfile(Path path) throws MalformedURLException, SubprocessorException;

   public CMDProfileReport processCMDProfile(URL schemaLocation) throws SubprocessorException;

   /*
    * throws Exception if file doesn't exist or is invalid
    */
   public CMDInstanceReport processCMDInstance(Path file);

   public CMDInstanceReport processCMDInstance(URL url);

   public CollectionReport processCollection(Path path);

}
