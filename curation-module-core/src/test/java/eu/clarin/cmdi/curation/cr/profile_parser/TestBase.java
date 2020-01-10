package eu.clarin.cmdi.curation.cr.profile_parser;


import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import eu.clarin.cmdi.curation.main.Configuration;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class TestBase {

    @BeforeClass
    public static void initClass() {

        try {
            setupTestDatabase();

            Configuration.initDefault();


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
    private static void setupTestDatabase() throws SQLException, IOException, ManagedProcessException {
        DB database = DB.newEmbeddedDB(3308);

        database.start();
        database.createDB("stormychecker");


        //create database and fill it with initDB.sql
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3308/stormychecker?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "");
        ScriptRunner runner = new ScriptRunner(con);
        InputStreamReader reader = new InputStreamReader(new FileInputStream("./src/test/resources/initDB.sql"));
        runner.runScript(reader);
        reader.close();
        con.close();
    }

}
