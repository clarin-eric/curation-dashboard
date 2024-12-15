package eu.clarin.cmdi.curation.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@SpringBootTest
@AutoConfigureMockMvc
class LinkcheckerCtlTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebConfig webConfig;

    @Test
    void getReport() throws Exception {

        // should return a status 403 since the default AllLinkcheckerReport.html is not available
        this.mockMvc.perform(get("/linkchecker")).andDo(print()).andExpect(status().isNotFound());

        // create the html report directory
        Path htmlReportDir = webConfig.getDirectory().getOut().resolve("html").resolve("linkchecker");
        Files.createDirectories(htmlReportDir);
        // copy the TestReport.htlm to the directory
        Files.copy(this.getClass().getResourceAsStream("/testfiles/TestReport.html"), htmlReportDir.resolve("TestReport.html"), StandardCopyOption.REPLACE_EXISTING);
        // and call it
        this.mockMvc.perform(get("/linkchecker/TestReport.html")).andDo(print()).andExpect(status().isOk());

        this.mockMvc.perform(get("/linkchecker/TestReport")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getLatestChecks() throws Exception {

        // copy the object file
        Files.copy(this.getClass().getResourceAsStream("/testfiles/latestChecks.obj"), webConfig.getDirectory().getShare().resolve("latestChecks.obj"), StandardCopyOption.REPLACE_EXISTING);
        // and call it
        this.mockMvc.perform(get("/linkchecker/latestChecks")).andDo(print()).andExpect(status().isOk());
    }
}