package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;


/**
 * The interface Curation module.
 */
public interface CurationModule {

   /**
    * Process cmd profile cmd profile report.
    *
    * @param profileId the profile id
    * @return the cmd profile report
    * @throws MalformedURLException the malformed url exception
    */
   public CMDProfileReport processCMDProfile(String profileId) throws MalformedURLException;

   /**
    * Process cmd profile cmd profile report.
    *
    * @param path the path
    * @return the cmd profile report
    * @throws MalformedURLException the malformed url exception
    */
   public CMDProfileReport processCMDProfile(Path path) throws MalformedURLException;

   /**
    * Process cmd profile cmd profile report.
    *
    * @param schemaLocation the schema location
    * @return the cmd profile report
    */
   public CMDProfileReport processCMDProfile(URL schemaLocation);

   /**
    * Process cmd instance cmd instance report.
    *
    * @param file the file
    * @return the cmd instance report
    */
   /*
    * throws Exception if file doesn't exist or is invalid
    */
   public CMDInstanceReport processCMDInstance(Path file);

   /**
    * Process cmd instance cmd instance report.
    *
    * @param url the url
    * @return the cmd instance report
    */
   public CMDInstanceReport processCMDInstance(URL url);

   /**
    * Process collection collection report.
    *
    * @param path the path
    * @return the collection report
    */
   public CollectionReport processCollection(Path path);

   /**
    * Gets linkchecker detail reports.
    *
    * @return the linkchecker detail reports
    */
   public Collection<LinkcheckerDetailReport> getLinkcheckerDetailReports();

}
