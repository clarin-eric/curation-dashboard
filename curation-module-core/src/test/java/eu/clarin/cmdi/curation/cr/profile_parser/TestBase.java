package eu.clarin.cmdi.curation.cr.profile_parser;


import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.DAO.LinkToBeChecked;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;


public class TestBase {

    private static DB database;

    @BeforeClass
    public static void initClass() {

        try {
            setupTestDatabase();

            Configuration.initDefault();
            
            Timestamp today = new Timestamp((System.currentTimeMillis()/1000)*1000);
            
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/waiting.html", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/audio_files/EMP1M1B1.mp3", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/audio_files/WBA1M3A2.mp3", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/text_files/WBA1M1A2a.mp3", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/audio_files/KUA2M1A1.mp3", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/text_files/KUA2M1.pdf", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/audio_files/sarixojani.mp3", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/audio_files/TEH11M7A1sa.mp3", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.ailla.org/text_files/TEH11M7.pdf", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://dspin.dwds.de:8088/ddc-sru/dta/", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://dspin.dwds.de:8088/ddc-sru/grenzboten/", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://dspin.dwds.de:8088/ddc-sru/rem/", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M084E-N1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M220P-N1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M119-N1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M171-G1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M185-N1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M048P-N1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("http://www.deutschestextarchiv.de/rem/?d=M112-G1.xml", Configuration.SOURCE, "record", "NotGoogle", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("https://www.google.com", Configuration.SOURCE, "GoogleRecord", "Google", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("https://maps.google.com", Configuration.SOURCE, "GoogleRecord", "Google", null, today));
            Configuration.linkToBeCheckedResource.save(new LinkToBeChecked("https://drive.google.com", Configuration.SOURCE, "GoogleRecord", "Google", null, today));
            
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/waiting.html", "HEAD", 200, "text/html; charset=UTF-8", 100l, 132, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/audio_files/EMP1M1B1.mp3", "GET",  400, "text/html; charset=UTF-8", 0l, 46, today, "Broken", 0, Category.Broken));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/audio_files/WBA1M3A2.mp3", "GET",  400, "text/html; charset=UTF-8", 0l, 46, today, "Broken", 0, Category.Broken));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/text_files/WBA1M1A2a.mp3", "GET",  400, "text/html; charset=UTF-8", 0l, 46, today, "Broken", 0, Category.Broken));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/audio_files/KUA2M1A1.mp3", "GET",  400, "text/html; charset=UTF-8", 0l, 56, today, "Broken", 0, Category.Broken));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/text_files/KUA2M1.pdf", "HEAD",  200, "text/html; charset=UTF-8", 0l, 51, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/audio_files/sarixojani.mp3", "GET",  400, "text/html; charset=UTF-8", 0l, 48, today, "Broken", 0, Category.Broken));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/audio_files/TEH11M7A1sa.mp3", "GET",  400, "text/html; charset=UTF-8", 0l, 48, today, "Broken", 0, Category.Broken));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.ailla.org/text_files/TEH11M7.pdf", "HEAD",  200, "text/html; charset=UTF-8", 0l, 57, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://dspin.dwds.de:8088/ddc-sru/dta/", "HEAD",  200, "application/xml;charset=utf-8", 2094l, 67, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://dspin.dwds.de:8088/ddc-sru/grenzboten/", "HEAD",  200, "application/xml;charset=utf-8", 2273l, 57, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://dspin.dwds.de:8088/ddc-sru/rem/", "HEAD",  200, "application/xml;charset=utf-8", 2497l, 58, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M084E-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 591, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M220P-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 592, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M119-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 602, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M171-G1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 613, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M185-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 605, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M048P-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 599, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "http://www.deutschestextarchiv.de/rem/?d=M112-G1.xml", "HEAD",  200, "text/html; charset=utf-8", 0l, 591, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "https://www.google.com", "HEAD",  200, "text/html; charset=ISO-8859-1", 0l, 222, today, "Ok", 0, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "https://maps.google.com", "HEAD",  200, "text/html; charset=UTF-8", 0l, 440, today, "Ok", 2, Category.Ok));
            Configuration.checkedLinkResource.save(new CheckedLink(null, null, "https://drive.google.com", "HEAD",  200, "text/html; charset=UTF-8", 73232l, 413, today, "Ok", 1, Category.Ok));
            



            if (Configuration.CACHE_DIRECTORY == null) {
                File dir = new File(System.getProperty("java.io.tmpdir"), "private_profiles");
                dir.mkdir();

                Configuration.CACHE_DIRECTORY = dir.getParentFile().toPath();
            }
        } catch (IOException | SQLException | ManagedProcessException ex) {
            ex.printStackTrace();
        }

    }

    //TODO use this database for tests...
    //todo update initDB.sql from rasa
    private static void setupTestDatabase() throws SQLException, IOException, ManagedProcessException {
        database = DB.newEmbeddedDB(3308);

        database.start();
        database.createDB("linkchecker_test");


        //create database and fill it with initDB.sql
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3308/linkchecker_test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "");
        ScriptRunner runner = new ScriptRunner(con);
        InputStreamReader reader = new InputStreamReader(new FileInputStream("./src/test/resources/createDB.sql"));
        runner.runScript(reader);
        reader.close();
        con.close();
        
        
    }

    @AfterClass
    public static void tearDown() throws ManagedProcessException {
        database.stop();
    }

}
