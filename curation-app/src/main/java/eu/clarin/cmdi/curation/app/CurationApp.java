package eu.clarin.cmdi.curation.app;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.collection.AllCollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionHistoryReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.AllLinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.profile.AllProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.utils.FileStorage;
import eu.clarin.cmdi.curation.app.conf.AppConfig;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.chart.SimpleChartFactory;
import eu.clarin.cmdi.curation.chart.StackedAreaChart;
import eu.clarin.cmdi.curation.chart.conf.ChartConfig;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.linkchecker.persistence.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The type Curation app.
 */
@SpringBootApplication
@ComponentScan({"eu.clarin.cmdi.curation", "eu.clarin.linkchecker.persistence"})
@EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
@EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
@EnableCaching
@EnableConfigurationProperties
@Slf4j
public class CurationApp {

    private static final Pattern pattern = Pattern.compile("(\\S+)_(\\d{4}-\\d{2}-\\d{2}).html");

    private final AppConfig conf;
    private final ChartConfig chartConf;
    private final CurationModule curation;
    private final CRService crService;
    /**
     * The Storage.
     */
    final
    FileStorage storage;
    /**
     * The Link service.
     */
    final
    LinkService linkService;
    final
    CacheManager cacheManager;
    final
    SimpleChartFactory chartFactory;

    public CurationApp(AppConfig conf, ChartConfig chartConf, CurationModule curation, CRService crService, FileStorage storage, LinkService linkService, CacheManager cacheManager, SimpleChartFactory chartFactory) {
        this.conf = conf;
        this.chartConf = chartConf;
        this.curation = curation;
        this.crService = crService;
        this.storage = storage;
        this.linkService = linkService;
        this.cacheManager = cacheManager;
        this.chartFactory = chartFactory;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CurationApp.class, args);
    }

    /**
     * Command line runner command line runner.
     *
     * @param ignoredCtx the ctx
     * @return the command line runner
     */
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ignoredCtx) {

        return args -> {

            log.info("start time: {}", LocalDateTime.now());

            if ("all".equalsIgnoreCase(conf.getMode()) || "collection".equalsIgnoreCase(conf.getMode())) {


                final AllCollectionReport allCollectionReport = new AllCollectionReport();
                final AllLinkcheckerReport allLinkcheckerReport = new AllLinkcheckerReport();
                final CollectionHistoryReport collectionHistoryReport = new CollectionHistoryReport();

                log.info("start generating collection reports at time: {}", LocalDateTime.now());
                Collection<CollectionReport> collectionReports = curation.processCollectionSet(conf.getDirectory().getIn());

                collectionReports.forEach(collectionReport -> {

                    allCollectionReport.addReport(collectionReport);
                    allLinkcheckerReport.addReport(collectionReport);

                    storage.saveReport(collectionReport, CurationEntityType.COLLECTION, true);

                });
                log.info("finished generating collection reports at time: {}", LocalDateTime.now());

                // remove old reports
                if (conf.getPurgeReportAfter() > 0) {
                    log.info("purging reports older than {} days from path '{}'", conf.getPurgeReportAfter(), conf.getDirectory().getOut());
                    this.storage.purgeReportAfter(conf.getPurgeReportAfter());
                    log.info("done purging reports");
                }

                // create a meta report of all collection reports
                try(Stream<Path> paths = Files.walk(conf.getDirectory().getOut().resolve("html").resolve("collection"))) {
                    paths.forEach(path -> {

                        Matcher matcher;

                        if (!path.getFileName().toString().startsWith("AllCollectionReport") && (matcher = pattern.matcher(path.getFileName().toString())).matches()) {

                            collectionHistoryReport.addReport(matcher.group(1).trim(), matcher.group(2), path.getFileName().toString());
                        }
                    });
                }

                // save reports
                storage.saveReport(allCollectionReport, CurationEntityType.COLLECTION, true);
                storage.saveReport(allLinkcheckerReport, CurationEntityType.LINKCHECKER, true);
                storage.saveReport(collectionHistoryReport, CurationEntityType.COLLECTION, false);

                // create charts
                log.info("start generating linkchecker charts at time: {}", LocalDateTime.now());
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                final Map<String, StackedAreaChart> charts = new HashMap<>();


                // we walk through the AllLinkcheckerReport files that have a timestamp and sort them, oldest first
                try(Stream<Path> paths = Files.walk(conf.getDirectory().getOut().resolve("xml").resolve("linkchecker"))
                        .filter(filePath -> filePath.getFileName().toString().matches("AllLinkcheckerReport_\\d{4}-\\d{2}-\\d{2}.xml"))
                        .sorted(Comparator.naturalOrder())) {
                    paths.forEach(filePath -> {
                        // each file is parsed for the creationDate, the collection-name and the category/number information
                        try {
                            SAXParser parser = saxParserFactory.newSAXParser();

                            parser.parse(Files.newInputStream(filePath), new DefaultHandler() {

                                private Date creationDate;
                                private String collectionName;
                                private Map<String, Double> values;

                                @Override
                                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                                    switch (qName) {
                                        case "allLinkcheckerReport":
                                            try {
                                                this.creationDate = dateFormat.parse(attributes.getValue("creationTime"));
                                            }
                                            catch (ParseException e) {
                                                throw new RuntimeException(e);
                                            }
                                            break;
                                        case "overall":
                                            this.collectionName = "Overall";

                                            this.values = new LinkedHashMap<>();
                                            chartConf.getColors().forEach((k, v) -> this.values.put(k, 0.0));

                                            break;
                                        case "collection":
                                            this.collectionName = attributes.getValue("name");

                                            this.values = new LinkedHashMap<>();
                                            chartConf.getColors().forEach((k, v) -> this.values.put(k, 0.0));

                                            break;
                                        case "statistics":
                                            values.put(attributes.getValue("category"), Double.valueOf(attributes.getValue("count")));
                                            break;
                                        default:
                                    }
                                }

                                @Override
                                public void endElement(String uri, String localName, String qName) {
                                    switch (qName) {
                                        case "overall":
                                        case "collection":
                                            charts.computeIfAbsent(
                                                    this.collectionName,
                                                    k -> chartFactory.createStackedAreaChart()
                                            ).addValues(creationDate, values);

                                    }
                                }
                            });
                        }
                        catch (ParserConfigurationException e) {

                            log.error("can't create SAX parser", e);
                            throw new RuntimeException(e);
                        }
                        catch (SAXException e) {

                            log.error("can't parse file '{}'", filePath);
                        }
                        catch (IOException e) {

                            log.error("can't read file '{}'", filePath);
                        }
                    });
                }

                Path imgDirectory = conf.getDirectory().getOut().resolve("img").resolve("linkchecker");

                if(Files.notExists(imgDirectory)) {

                    Files.createDirectories(imgDirectory);
                }

                charts.forEach((collectionName, chart) -> {
                    try {

                        chart.save(imgDirectory, collectionName);
                    }
                    catch (IOException e) {

                        log.error("can't save chart of collection '{}'", collectionName);
                    }
                });

                log.info("finished generating linkchecker charts at time: {}", LocalDateTime.now());
            }
            // it's important to process profiles after collections, to fill the collection usage section of the profiles
            // before they're printed out
            if ("all".equalsIgnoreCase(conf.getMode()) || "profile".equalsIgnoreCase(conf.getMode())) {

                final AllProfileReport allProfileReport = new AllProfileReport();

                final Cache cache = this.cacheManager.getCache("profileReportCache");

                log.info("start generating profile reports at time: {}", LocalDateTime.now());

                crService.getPublicSchemaLocations().forEach(schemaLocation -> { //this is for the public profiles

                    CMDProfileReport profileReport = curation.processCMDProfile(schemaLocation);

                    allProfileReport.addReport(profileReport);

                    storage.saveReport(profileReport, CurationEntityType.PROFILE, false);

                    assert cache != null;
                    cache.evict(schemaLocation);

                });

                // now we have to do the same for the nonpublic profiles used by CMDI files

                Iterator<javax.cache.Cache.Entry<String, CMDProfileReport>> it = ((javax.cache.Cache) cache.getNativeCache()).iterator();

                it.forEachRemaining(entry -> {

                    allProfileReport.addReport(entry.getValue());

                    storage.saveReport(entry.getValue(), CurationEntityType.PROFILE, false);
                });

                storage.saveReport(allProfileReport, CurationEntityType.PROFILE, false);

                log.info("finished generating profile reports at time: {}", LocalDateTime.now());
            }

            if ("all".equalsIgnoreCase(conf.getMode()) || "linkchecker".equalsIgnoreCase(conf.getMode())) {

                log.info("start generating linkchecker detail reports at time: {}", LocalDateTime.now());
                curation.getLinkcheckerDetailReports().forEach(report -> storage.saveReport(report, CurationEntityType.LINKCHECKER, false));
                log.info("finished writing linkchecker detail reports at time: {}", LocalDateTime.now());
            }

            if ("all".equalsIgnoreCase(conf.getMode())) {
                if (conf.getLinkDeactivationAfter() > 0) {
                    log.info("start deactivating links older than {} days", conf.getLinkDeactivationAfter());
                    linkService.deactivateLinksOlderThan(conf.getLinkDeactivationAfter());
                    log.info("finished deactivating links");
                }
                if (conf.getLinkDeletionAfter() > 0) {
                    log.info("start deleting links older than {} days", conf.getLinkDeletionAfter());
                    linkService.deleteLinksOderThan(conf.getLinkDeletionAfter());
                    log.info("finished deleting links");
                }
                if (conf.getPurgeHistoryAfter() > 0) {
                    log.info("start purging history table from records checked before {} days", conf.getPurgeHistoryAfter());
                    linkService.purgeHistory(conf.getPurgeHistoryAfter());
                    log.info("finished purging history");
                }
                if (conf.getPurgeObsoleteAfter() > 0) {
                    log.info("start purging obsolete table from records checked before {} days", conf.getPurgeObsoleteAfter());
                    linkService.purgeObsolete(conf.getPurgeObsoleteAfter());
                    log.info("finished purging obsolete");
                }
            }

            log.info("end time: {}", LocalDateTime.now());
        };
    }
}
