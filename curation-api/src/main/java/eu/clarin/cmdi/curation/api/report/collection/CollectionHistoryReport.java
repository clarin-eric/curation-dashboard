package eu.clarin.cmdi.curation.api.report.collection;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class CollectionHistoryReport implements NamedReport {
    @XmlAttribute
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public final LocalDateTime creationTime = LocalDateTime.now();
    private final TreeMap<String, ProviderGroup> providerGroups = new TreeMap<>((pg1, pg2) -> pg1.compareTo(pg2));
    @XmlElement(name = "collection")
    public Collection<ProviderGroup> getCollections(){

        return this.providerGroups.values();
    }

    public void addReport(String providerGroup, String isoDate, String fileName){

        this.providerGroups.computeIfAbsent(providerGroup, k -> new ProviderGroup(k)).addReport(isoDate, fileName);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public LocalDateTime getCreationTime() {

        return this.creationTime;
    }

    @Override
    public void setPreviousCreationTime(LocalDateTime previousCreationTime) {

    }

    @Override
    public LocalDateTime getPreviousCreationTime() {
        return null;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @RequiredArgsConstructor
    @NoArgsConstructor(force = true)
    private static class ProviderGroup{
        @XmlAttribute
        public final String name;
        @XmlElement(name = "report")
        public TreeSet<Report> reports = new TreeSet<>((report1, report2) -> report2.isoDate.compareTo(report1.isoDate));

        public void addReport(String isoDate, String fileName){
            this.reports.add(new Report(isoDate, fileName));
        }
    }
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @RequiredArgsConstructor
    @NoArgsConstructor(force = true)
    public static class Report  {
        @XmlAttribute
        public final String isoDate;
        @XmlAttribute
        public final String fileName;
    }
}
