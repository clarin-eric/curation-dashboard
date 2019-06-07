package eu.clarin.cmdi.curation.main;

import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.io.CMDFileVisitor;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.FileNameEncoder;
import eu.clarin.curation.linkchecker.httpLinkChecker.HTTPLinkChecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class CurationModule implements CurationModuleInterface {

    @Override
    public Report<?> processCMDProfile(String profileId) {
        return new CMDProfile(Configuration.VLO_CONFIG.getComponentRegistryProfileSchema(profileId), "1.x").generateReport(null);

    }


    @Override
    public Report<?> processCMDProfile(URL schemaLocation) {

        return new CMDProfile(schemaLocation.toString(), "1.x").generateReport(null);
    }
    
    @Override    
    public Report<?> processCMDProfile(Path path) throws MalformedURLException {

            return processCMDProfile(path.toUri().toURL());
    }

    @Override
    public Report<?> processCMDInstance(Path path) throws IOException {
        if (Files.notExists(path))
            throw new IOException(path.toString() + " doesn't exist!");

        return new CMDInstance(path, Files.size(path)).generateReport(null);

    }


    @Override
    public Report<?> processCMDInstance(URL url) throws IOException {
        String path = FileNameEncoder.encode(url.toString()) + ".xml";
        Path cmdiFilePath = Paths.get(System.getProperty("java.io.tmpdir"), path);
        new HTTPLinkChecker(15000, 5, Configuration.USERAGENT).download(url.toString(), cmdiFilePath.toFile());
        long size = Files.size(cmdiFilePath);
        CMDInstance cmdInstance = new CMDInstance(cmdiFilePath, size);
        cmdInstance.setUrl(url.toString());

        Report<?> report = cmdInstance.generateReport(null);

//		Files.delete(path);

        ((CMDInstanceReport) report).fileReport.location = url.toString();

        return report;
    }

    @Override
    public Report<?> processCollection(Path path) throws IOException {
        CMDFileVisitor entityTree = new CMDFileVisitor();
        Files.walkFileTree(path, entityTree);
        CMDCollection collection = entityTree.getRoot();

        return collection.generateReport(null);
    }

    @Override
    public Report<?> aggregateReports(Collection<Report> reports) {
        // TODO Auto-generated method stub
        return null;
    }

}
