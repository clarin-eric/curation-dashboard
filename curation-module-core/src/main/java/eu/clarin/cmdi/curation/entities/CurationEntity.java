package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;

import eu.clarin.cmdi.curation.main.Main;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.report.Report;

public abstract class CurationEntity {

    public enum CurationEntityType {PROFILE, INSTANCE, COLLECTION}

    static final Logger logger = LoggerFactory.getLogger(CurationEntity.class);

    protected Path path = null;

    protected Report report = null;

    protected long size = 0;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    protected String url;

    public CurationEntity(Path path) {
        this.path = path;
    }

    public CurationEntity(Path path, long size) {
        this.path = path;
        this.size = size;
    }

    public Report generateReport(String parentName) throws InterruptedException {

        if (report == null) {

            report = getProcessor().process(this,parentName);

        }

        return report;
    }

    protected abstract AbstractProcessor getProcessor();

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public abstract String toString();

}
