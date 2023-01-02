package eu.clarin.cmdi.curation.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;


public interface CurationModule {

   public CMDProfileReport processCMDProfile(String profileId) throws MalformedURLException;

   public CMDProfileReport processCMDProfile(Path path) throws MalformedURLException;

   public CMDProfileReport processCMDProfile(URL schemaLocation);

   /*
    * throws Exception if file doesn't exist or is invalid
    */
   public CMDInstanceReport processCMDInstance(Path file);

   public CMDInstanceReport processCMDInstance(URL url);

   public CollectionReport processCollection(Path path);
   
   public Collection<LinkcheckerDetailReport> getLinkcheckerDetailReports();

}
