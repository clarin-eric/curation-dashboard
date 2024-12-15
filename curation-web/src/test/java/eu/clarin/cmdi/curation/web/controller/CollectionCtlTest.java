package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CollectionCtlTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebConfig webConfig;

    @Test
    void getCollection() throws Exception {

        // should return a status 403 since the default AllLinkcheckerReport.html is not available
        this.mockMvc.perform(get("/collection")).andDo(print()).andExpect(status().isNotFound());

        // create the html report directory
        Path htmlReportDir = webConfig.getDirectory().getOut().resolve("html").resolve("collection");
        Files.createDirectories(htmlReportDir);
        // copy the TestReport.htlm to the directory
        Files.copy(this.getClass().getResourceAsStream("/testfiles/TestReport.html"), htmlReportDir.resolve("TestReport.html"), StandardCopyOption.REPLACE_EXISTING);
        // and call it
        this.mockMvc.perform(get("/collection/TestReport.html")).andDo(print()).andExpect(status().isOk());

        this.mockMvc.perform(get("/collection/TestReport")).andDo(print()).andExpect(status().isOk());
    }
}